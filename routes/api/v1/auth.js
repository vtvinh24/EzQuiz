const express = require("express");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const { body, validationResult } = require("express-validator");
const { db } = require("../../../config/database");

const router = express.Router();

// JWT secret key
const JWT_SECRET = process.env.JWT_SECRET || "your-super-secret-key-change-this-in-production";
const JWT_EXPIRE = process.env.JWT_EXPIRE || "24h";

// Login endpoint
router.post(
  "/login",
  [body("username").notEmpty().withMessage("Username is required"), body("password").isLength({ min: 6 }).withMessage("Password must be at least 6 characters")],
  async (req, res) => {
    try {
      // Check validation errors
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(400).json({
          message: "Validation errors",
          errors: errors.array(),
        });
      }

      const { username, password } = req.body;

      // Find admin user
      db.get("SELECT id, username, password_hash, is_active FROM admin_users WHERE username = ?", [username], async (err, user) => {
        if (err) {
          console.error("Database error:", err);
          return res.status(500).json({ message: "Database error" });
        }

        if (!user || !user.is_active) {
          return res.status(401).json({ message: "Invalid credentials" });
        }

        // Verify password
        const isValidPassword = await bcrypt.compare(password, user.password_hash);
        if (!isValidPassword) {
          return res.status(401).json({ message: "Invalid credentials" });
        }

        // Update last login
        db.run("UPDATE admin_users SET last_login = CURRENT_TIMESTAMP WHERE id = ?", [user.id]);

        // Generate JWT token
        const token = jwt.sign(
          {
            userId: user.id,
            username: user.username,
            role: "admin",
          },
          JWT_SECRET,
          { expiresIn: JWT_EXPIRE }
        );

        res.json({
          message: "Login successful",
          token,
          user: {
            id: user.id,
            username: user.username,
            role: "admin",
          },
        });
      });
    } catch (error) {
      console.error("Login error:", error);
      res.status(500).json({ message: "Server error" });
    }
  }
);

// Change password endpoint
router.post(
  "/change-password",
  authenticateToken,
  [body("currentPassword").notEmpty().withMessage("Current password is required"), body("newPassword").isLength({ min: 6 }).withMessage("New password must be at least 6 characters")],
  async (req, res) => {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(400).json({
          message: "Validation errors",
          errors: errors.array(),
        });
      }

      const { currentPassword, newPassword } = req.body;
      const userId = req.user.userId;

      // Get current password hash
      db.get("SELECT password_hash FROM admin_users WHERE id = ?", [userId], async (err, user) => {
        if (err) {
          console.error("Database error:", err);
          return res.status(500).json({ message: "Database error" });
        }

        if (!user) {
          return res.status(404).json({ message: "User not found" });
        }

        // Verify current password
        const isValidPassword = await bcrypt.compare(currentPassword, user.password_hash);
        if (!isValidPassword) {
          return res.status(401).json({ message: "Current password is incorrect" });
        }

        // Hash new password
        const hashedNewPassword = await bcrypt.hash(newPassword, 10);

        // Update password
        db.run("UPDATE admin_users SET password_hash = ? WHERE id = ?", [hashedNewPassword, userId], (err) => {
          if (err) {
            console.error("Database error:", err);
            return res.status(500).json({ message: "Database error" });
          }

          res.json({ message: "Password changed successfully" });
        });
      });
    } catch (error) {
      console.error("Change password error:", error);
      res.status(500).json({ message: "Server error" });
    }
  }
);

// Verify token endpoint
router.get("/verify", authenticateToken, (req, res) => {
  res.json({
    message: "Token is valid",
    user: req.user,
  });
});

// Middleware to authenticate JWT token
function authenticateToken(req, res, next) {
  const authHeader = req.headers["authorization"];
  const token = authHeader && authHeader.split(" ")[1]; // Bearer TOKEN

  if (!token) {
    return res.status(401).json({ message: "Access token required" });
  }

  jwt.verify(token, JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).json({ message: "Invalid or expired token" });
    }
    req.user = user;
    next();
  });
}

module.exports = router;
module.exports.authenticateToken = authenticateToken;
