const express = require("express");
const cors = require("cors");
const helmet = require("helmet");
const rateLimit = require("express-rate-limit");
require("dotenv").config();

const app = express();
const PORT = process.env.PORT || 3000;

// Security middleware
app.use(helmet());
app.use(
  cors({
    origin: process.env.ALLOWED_ORIGINS?.split(",") || ["http://localhost:3001"],
    credentials: true,
  })
);

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // limit each IP to 100 requests per windowMs
  message: "Too many requests from this IP, please try again later.",
});
app.use("/api/", limiter);

// Stricter rate limiting for auth endpoints
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 5,
  message: "Too many authentication attempts, please try again later.",
});

// Rate limiting for paste endpoints (to prevent spam)
const pasteLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 10, // limit each IP to 10 pastes per minute
  message: "Too many paste requests, please try again later.",
});

// Body parsing middleware
app.use(express.json({ limit: "10mb" }));
app.use(express.urlencoded({ extended: true, limit: "10mb" }));

// Serve static files
app.use("/public", express.static("public"));

// Initialize database
const { initDB } = require("./config/database");
initDB();

// Routes
const authRoutes = require("./routes/api/v1/auth");
const quizRoutes = require("./routes/api/v1/generate/generate");
const licenseRoutes = require("./routes/api/v1/license");
const paymentRoutes = require("./routes/api/v1/payment");
const pasteRoutes = require("./routes/api/v1/paste");

// Apply routes
app.use("/api/v1/auth", authLimiter, authRoutes);
app.use("/api/v1/generate", quizRoutes);
app.use("/api/v1/license", licenseRoutes);
app.use("/api/v1/payment", paymentRoutes);
app.use("/api/v1/paste", pasteLimiter, pasteRoutes);

// Health check endpoint
app.get("/health", (req, res) => {
  res.status(200).json({
    status: "OK",
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
  });
});

// Serve paste web interface
app.get("/paste", (req, res) => {
  res.sendFile(__dirname + "/public/paste.html");
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error("Error:", err.stack);
  res.status(500).json({
    message: "Something went wrong!",
    error: process.env.NODE_ENV === "production" ? {} : err.message,
  });
});

// 404 handler
app.use("*", (req, res) => {
  res.status(404).json({ message: "Endpoint not found" });
});

app.listen(PORT, () => {
  console.log(`🚀 EzQuiz Server is running on port ${PORT}`);
  console.log(`🌍 Environment: ${process.env.NODE_ENV || "development"}`);
});

module.exports = app;
