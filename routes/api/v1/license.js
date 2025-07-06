const express = require("express");
const { body, validationResult } = require("express-validator");
const { v4: uuidv4 } = require("uuid");
const crypto = require("crypto");
const DeviceDetector = require("node-device-detector");
const { db } = require("../../../config/database");
const { authenticateToken } = require("./auth");
const {
  licenseValidationLimiter,
  licenseGenerationLimiter,
  securityHeaders,
  requestLogger,
  validateApiKey,
  sanitizeInput,
  validateLicenseKeyFormat,
  validateDeviceFingerprint,
} = require("../../../middleware/security");

const router = express.Router();
const detector = new DeviceDetector();

// Apply security middleware to all routes
router.use(securityHeaders);
router.use(requestLogger);
router.use(sanitizeInput);

// Generate license keys (Admin only)
router.post(
  "/generate",
  licenseGenerationLimiter,
  authenticateToken,
  [
    body("amount").isInt({ min: 1, max: 1000 }).withMessage("Amount must be between 1 and 1000"),
    body("maxActivations").optional().isInt({ min: 1, max: 10 }).withMessage("Max activations must be between 1 and 10"),
    body("expiresInDays").optional().isInt({ min: 1, max: 365 }).withMessage("Expiry must be between 1 and 365 days"),
  ],
  async (req, res) => {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(400).json({
          message: "Validation errors",
          errors: errors.array(),
        });
      }

      const { amount, maxActivations = 1, expiresInDays } = req.body;
      const createdBy = req.user.userId;

      // Calculate expiration date if provided
      let expiresAt = null;
      if (expiresInDays) {
        const expDate = new Date();
        expDate.setDate(expDate.getDate() + expiresInDays);
        expiresAt = expDate.toISOString();
      }

      const generatedKeys = [];

      // Generate license keys
      for (let i = 0; i < amount; i++) {
        const licenseKey = generateLicenseKey();

        await new Promise((resolve, reject) => {
          db.run("INSERT INTO license_keys (license_key, max_activations, expires_at, created_by) VALUES (?, ?, ?, ?)", [licenseKey, maxActivations, expiresAt, createdBy], function (err) {
            if (err) {
              console.error("Error inserting license key:", err);
              reject(err);
            } else {
              generatedKeys.push({
                id: this.lastID,
                key: licenseKey,
                maxActivations,
                expiresAt,
              });
              resolve();
            }
          });
        });
      }

      res.json({
        message: `${amount} license keys generated successfully`,
        keys: generatedKeys,
        summary: {
          total: amount,
          maxActivations,
          expiresAt,
        },
      });
    } catch (error) {
      console.error("Generate license error:", error);
      res.status(500).json({ message: "Server error" });
    }
  }
);

// Validate license key (Public endpoint)
router.post(
  "/validate",
  licenseValidationLimiter,
  validateApiKey,
  validateLicenseKeyFormat,
  validateDeviceFingerprint,
  [
    body("key").notEmpty().withMessage("License key is required"),
    body("deviceInfo").optional().isObject().withMessage("Device info must be an object"),
    body("installId").optional().isString().withMessage("Install ID must be a string"),
  ],
  async (req, res) => {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(400).json({
          message: "Validation errors",
          errors: errors.array(),
        });
      }

      const { key, deviceInfo = {}, installId } = req.body;
      const userAgent = req.headers["user-agent"] || "";
      const ipAddress = req.ip || req.connection.remoteAddress || "unknown";

      // Parse device info from user agent
      const deviceDetails = detector.detect(userAgent);

      // Create device fingerprint
      const deviceFingerprint = createDeviceFingerprint({
        userAgent,
        ...deviceInfo,
        installId,
      });

      // Check if license key exists and is valid
      db.get("SELECT id, license_key, status, expires_at, max_activations, current_activations FROM license_keys WHERE license_key = ?", [key], async (err, license) => {
        if (err) {
          console.error("Database error:", err);
          return res.status(500).json({ message: "Database error" });
        }

        if (!license) {
          return res.status(404).json({
            message: "Invalid license key",
            valid: false,
          });
        }

        // Check if license is active
        if (license.status !== "active") {
          return res.status(400).json({
            message: "License key is not active",
            valid: false,
            status: license.status,
          });
        }

        // Check if license has expired
        if (license.expires_at && new Date(license.expires_at) < new Date()) {
          return res.status(400).json({
            message: "License key has expired",
            valid: false,
            expiredAt: license.expires_at,
          });
        }

        // Check existing activation for this device
        db.get("SELECT id, is_active FROM license_activations WHERE license_key_id = ? AND device_fingerprint = ?", [license.id, deviceFingerprint], async (err, activation) => {
          if (err) {
            console.error("Database error:", err);
            return res.status(500).json({ message: "Database error" });
          }

          if (activation) {
            // Update existing activation
            db.run("UPDATE license_activations SET last_seen = CURRENT_TIMESTAMP, is_active = 1 WHERE id = ?", [activation.id], (err) => {
              if (err) {
                console.error("Database error:", err);
                return res.status(500).json({ message: "Database error" });
              }

              res.json({
                message: "License key validated successfully",
                valid: true,
                license: {
                  key: license.license_key,
                  status: license.status,
                  expiresAt: license.expires_at,
                  maxActivations: license.max_activations,
                  currentActivations: license.current_activations,
                },
                activation: {
                  existing: true,
                  deviceFingerprint,
                },
              });
            });
          } else {
            // Check if max activations reached
            if (license.current_activations >= license.max_activations) {
              return res.status(400).json({
                message: "Maximum activations reached for this license key",
                valid: false,
                maxActivations: license.max_activations,
                currentActivations: license.current_activations,
              });
            }

            // Create new activation
            db.run(
              "INSERT INTO license_activations (license_key_id, device_fingerprint, device_info, install_id, ip_address, user_agent) VALUES (?, ?, ?, ?, ?, ?)",
              [license.id, deviceFingerprint, JSON.stringify({ ...deviceDetails, ...deviceInfo }), installId, ipAddress, userAgent],
              function (err) {
                if (err) {
                  console.error("Database error:", err);
                  return res.status(500).json({ message: "Database error" });
                }

                // Update current activations count
                db.run("UPDATE license_keys SET current_activations = current_activations + 1 WHERE id = ?", [license.id], (err) => {
                  if (err) {
                    console.error("Database error:", err);
                    return res.status(500).json({ message: "Database error" });
                  }

                  res.json({
                    message: "License key validated and activated successfully",
                    valid: true,
                    license: {
                      key: license.license_key,
                      status: license.status,
                      expiresAt: license.expires_at,
                      maxActivations: license.max_activations,
                      currentActivations: license.current_activations + 1,
                    },
                    activation: {
                      new: true,
                      deviceFingerprint,
                      activationId: this.lastID,
                    },
                  });
                });
              }
            );
          }
        });
      });
    } catch (error) {
      console.error("Validate license error:", error);
      res.status(500).json({ message: "Server error" });
    }
  }
);

// Get all license keys (Admin only)
router.get("/list", authenticateToken, async (req, res) => {
  try {
    const { page = 1, limit = 50, status } = req.query;
    const offset = (page - 1) * limit;

    let query = `
      SELECT 
        lk.id,
        lk.license_key,
        lk.status,
        lk.created_at,
        lk.expires_at,
        lk.max_activations,
        lk.current_activations,
        au.username as created_by_username
      FROM license_keys lk
      LEFT JOIN admin_users au ON lk.created_by = au.id
    `;

    const params = [];

    if (status) {
      query += " WHERE lk.status = ?";
      params.push(status);
    }

    query += " ORDER BY lk.created_at DESC LIMIT ? OFFSET ?";
    params.push(limit, offset);

    db.all(query, params, (err, licenses) => {
      if (err) {
        console.error("Database error:", err);
        return res.status(500).json({ message: "Database error" });
      }

      // Get total count
      let countQuery = "SELECT COUNT(*) as total FROM license_keys";
      const countParams = [];

      if (status) {
        countQuery += " WHERE status = ?";
        countParams.push(status);
      }

      db.get(countQuery, countParams, (err, result) => {
        if (err) {
          console.error("Database error:", err);
          return res.status(500).json({ message: "Database error" });
        }

        res.json({
          licenses,
          pagination: {
            page: parseInt(page),
            limit: parseInt(limit),
            total: result.total,
            pages: Math.ceil(result.total / limit),
          },
        });
      });
    });
  } catch (error) {
    console.error("List licenses error:", error);
    res.status(500).json({ message: "Server error" });
  }
});

// Revoke license key (Admin only)
router.post("/revoke", authenticateToken, [body("key").notEmpty().withMessage("License key is required")], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        message: "Validation errors",
        errors: errors.array(),
      });
    }

    const { key } = req.body;

    db.run('UPDATE license_keys SET status = "revoked" WHERE license_key = ?', [key], function (err) {
      if (err) {
        console.error("Database error:", err);
        return res.status(500).json({ message: "Database error" });
      }

      if (this.changes === 0) {
        return res.status(404).json({ message: "License key not found" });
      }

      // Deactivate all activations for this key
      db.run("UPDATE license_activations SET is_active = 0 WHERE license_key_id = (SELECT id FROM license_keys WHERE license_key = ?)", [key], (err) => {
        if (err) {
          console.error("Database error:", err);
        }
      });

      res.json({ message: "License key revoked successfully" });
    });
  } catch (error) {
    console.error("Revoke license error:", error);
    res.status(500).json({ message: "Server error" });
  }
});

// Helper function to generate license key
function generateLicenseKey() {
  const segments = [];
  for (let i = 0; i < 4; i++) {
    segments.push(crypto.randomBytes(4).toString("hex").toUpperCase());
  }
  return segments.join("-");
}

// Helper function to create device fingerprint
function createDeviceFingerprint(deviceInfo) {
  const fingerprint = crypto.createHash("sha256").update(JSON.stringify(deviceInfo)).digest("hex");
  return fingerprint;
}

module.exports = router;
