const rateLimit = require("express-rate-limit");
const crypto = require("crypto");

// Enhanced rate limiting for license validation
const licenseValidationLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 10, // limit each IP to 10 license validation requests per minute
  message: "Too many license validation attempts, please try again later.",
  standardHeaders: true,
  legacyHeaders: false,
});

// Rate limiting for license generation
const licenseGenerationLimiter = rateLimit({
  windowMs: 60 * 60 * 1000, // 1 hour
  max: 5, // limit each IP to 5 license generation requests per hour
  message: "Too many license generation attempts, please try again later.",
  standardHeaders: true,
  legacyHeaders: false,
});

// Security headers middleware
const securityHeaders = (req, res, next) => {
  res.setHeader("X-Content-Type-Options", "nosniff");
  res.setHeader("X-Frame-Options", "DENY");
  res.setHeader("X-XSS-Protection", "1; mode=block");
  res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
  res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
  res.setHeader("Pragma", "no-cache");
  res.setHeader("Expires", "0");
  next();
};

// Request logging middleware
const requestLogger = (req, res, next) => {
  const timestamp = new Date().toISOString();
  const method = req.method;
  const url = req.url;
  const ip = req.ip || req.connection.remoteAddress;
  const userAgent = req.headers["user-agent"] || "unknown";

  // Log sensitive endpoints
  if (req.url.includes("/auth/") || req.url.includes("/license/")) {
    console.log(`[${timestamp}] ${method} ${url} - IP: ${ip} - UA: ${userAgent}`);
  }

  next();
};

// API key validation middleware (for additional security layer)
const validateApiKey = (req, res, next) => {
  const apiKey = req.headers["x-api-key"];
  const expectedApiKey = process.env.API_KEY;

  // Skip API key validation in development
  if (process.env.NODE_ENV === "development") {
    return next();
  }

  if (!expectedApiKey) {
    return next(); // API key not configured
  }

  if (!apiKey || apiKey !== expectedApiKey) {
    return res.status(401).json({ message: "Invalid API key" });
  }

  next();
};

// Input sanitization middleware
const sanitizeInput = (req, res, next) => {
  const sanitize = (obj) => {
    if (typeof obj === "string") {
      return obj.trim().replace(/[<>]/g, "");
    }
    if (typeof obj === "object" && obj !== null) {
      const sanitized = {};
      for (const key in obj) {
        if (obj.hasOwnProperty(key)) {
          sanitized[key] = sanitize(obj[key]);
        }
      }
      return sanitized;
    }
    return obj;
  };

  if (req.body) {
    req.body = sanitize(req.body);
  }
  if (req.query) {
    req.query = sanitize(req.query);
  }

  next();
};

// License key format validation
const validateLicenseKeyFormat = (req, res, next) => {
  const { key } = req.body;

  if (!key) {
    return next();
  }

  // Expected format: XXXX-XXXX-XXXX-XXXX (hex characters)
  const licenseKeyPattern = /^[A-F0-9]{8}-[A-F0-9]{8}-[A-F0-9]{8}-[A-F0-9]{8}$/i;

  if (!licenseKeyPattern.test(key)) {
    return res.status(400).json({
      message: "Invalid license key format",
      valid: false,
    });
  }

  next();
};

// Device fingerprint validation
const validateDeviceFingerprint = (req, res, next) => {
  const { deviceInfo, installId } = req.body;
  const userAgent = req.headers["user-agent"];

  // Basic validation to ensure we have enough info for fingerprinting
  if (!userAgent && !deviceInfo && !installId) {
    return res.status(400).json({
      message: "Insufficient device information for validation",
      valid: false,
    });
  }

  next();
};

module.exports = {
  licenseValidationLimiter,
  licenseGenerationLimiter,
  securityHeaders,
  requestLogger,
  validateApiKey,
  sanitizeInput,
  validateLicenseKeyFormat,
  validateDeviceFingerprint,
};
