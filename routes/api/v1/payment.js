const express = require("express");
const { body, validationResult } = require("express-validator");
const { db } = require("../../../config/database");
const { authenticateToken } = require("./auth");

const router = express.Router();

// This is a mock payment system for demonstration
// In production, replace with actual payment gateway (Stripe, PayPal, etc.)

// Payment plans configuration
const PAYMENT_PLANS = {
  basic: {
    name: "Basic Plan",
    price: 9.99,
    currency: "USD",
    licenses: 1,
    maxActivations: 1,
    expiresInDays: 30,
  },
  standard: {
    name: "Standard Plan",
    price: 19.99,
    currency: "USD",
    licenses: 5,
    maxActivations: 2,
    expiresInDays: 90,
  },
  premium: {
    name: "Premium Plan",
    price: 49.99,
    currency: "USD",
    licenses: 20,
    maxActivations: 3,
    expiresInDays: 365,
  },
};

// Get available payment plans
router.get("/plans", (req, res) => {
  res.json({
    message: "Available payment plans",
    plans: PAYMENT_PLANS,
  });
});

// Create payment intent (mock)
router.post(
  "/create-payment-intent",
  [
    body("plan").isIn(Object.keys(PAYMENT_PLANS)).withMessage("Invalid payment plan"),
    body("customerEmail").isEmail().withMessage("Valid email is required"),
    body("customerName").notEmpty().withMessage("Customer name is required"),
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

      const { plan, customerEmail, customerName } = req.body;
      const planDetails = PAYMENT_PLANS[plan];

      // In production, create actual payment intent with payment gateway
      // Example with Stripe:
      /*
    const paymentIntent = await stripe.paymentIntents.create({
      amount: Math.round(planDetails.price * 100), // Convert to cents
      currency: planDetails.currency.toLowerCase(),
      metadata: {
        plan,
        customerEmail,
        customerName,
        licenses: planDetails.licenses.toString(),
        maxActivations: planDetails.maxActivations.toString(),
        expiresInDays: planDetails.expiresInDays.toString()
      }
    });
    */

      // Mock payment intent
      const mockPaymentIntent = {
        id: `pi_mock_${Date.now()}`,
        client_secret: `pi_mock_${Date.now()}_secret`,
        amount: Math.round(planDetails.price * 100),
        currency: planDetails.currency.toLowerCase(),
        status: "requires_payment_method",
        metadata: {
          plan,
          customerEmail,
          customerName,
          licenses: planDetails.licenses.toString(),
          maxActivations: planDetails.maxActivations.toString(),
          expiresInDays: planDetails.expiresInDays.toString(),
        },
      };

      // Store payment intent in database for tracking
      db.run(
        "INSERT INTO payment_intents (payment_intent_id, customer_email, customer_name, plan, amount, currency, status, metadata) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
        [mockPaymentIntent.id, customerEmail, customerName, plan, planDetails.price, planDetails.currency, "created", JSON.stringify(mockPaymentIntent.metadata)],
        function (err) {
          if (err) {
            console.error("Error storing payment intent:", err);
            return res.status(500).json({ message: "Database error" });
          }

          res.json({
            message: "Payment intent created",
            paymentIntent: {
              id: mockPaymentIntent.id,
              client_secret: mockPaymentIntent.client_secret,
              amount: mockPaymentIntent.amount,
              currency: mockPaymentIntent.currency,
            },
            plan: planDetails,
          });
        }
      );
    } catch (error) {
      console.error("Create payment intent error:", error);
      res.status(500).json({ message: "Server error" });
    }
  }
);

// Mock payment webhook (simulate payment success)
router.post("/webhook", async (req, res) => {
  try {
    const { paymentIntentId, status } = req.body;

    // In production, verify webhook signature from payment provider
    // Example with Stripe:
    /*
    const signature = req.headers['stripe-signature'];
    const event = stripe.webhooks.constructEvent(req.body, signature, process.env.STRIPE_WEBHOOK_SECRET);
    */

    if (status === "succeeded") {
      // Get payment intent from database
      db.get("SELECT * FROM payment_intents WHERE payment_intent_id = ?", [paymentIntentId], async (err, payment) => {
        if (err) {
          console.error("Database error:", err);
          return res.status(500).json({ message: "Database error" });
        }

        if (!payment) {
          return res.status(404).json({ message: "Payment intent not found" });
        }

        const metadata = JSON.parse(payment.metadata);

        // Generate license keys based on payment
        const licenseKeys = await generateLicenseKeysForPayment(parseInt(metadata.licenses), parseInt(metadata.maxActivations), parseInt(metadata.expiresInDays));

        // Update payment status
        db.run("UPDATE payment_intents SET status = ?, completed_at = CURRENT_TIMESTAMP WHERE id = ?", ["succeeded", payment.id], (err) => {
          if (err) {
            console.error("Error updating payment status:", err);
          }
        });

        // In production, send license keys to customer via email
        console.log(`Payment succeeded for ${payment.customer_email}`);
        console.log(`Generated license keys:`, licenseKeys);

        // You would integrate with an email service here
        // await sendLicenseKeysToCustomer(payment.customer_email, licenseKeys);

        res.json({
          message: "Payment processed successfully",
          licenseKeys: licenseKeys.map((key) => key.key),
        });
      });
    } else {
      res.json({ message: "Payment status updated", status });
    }
  } catch (error) {
    console.error("Payment webhook error:", error);
    res.status(500).json({ message: "Server error" });
  }
});

// Helper function to generate license keys for payment
async function generateLicenseKeysForPayment(amount, maxActivations, expiresInDays) {
  const crypto = require("crypto");
  const licenseKeys = [];

  // Calculate expiration date
  let expiresAt = null;
  if (expiresInDays) {
    const expDate = new Date();
    expDate.setDate(expDate.getDate() + expiresInDays);
    expiresAt = expDate.toISOString();
  }

  // Generate license keys
  for (let i = 0; i < amount; i++) {
    const licenseKey = generateLicenseKey();

    await new Promise((resolve, reject) => {
      db.run(
        "INSERT INTO license_keys (license_key, max_activations, expires_at, created_by) VALUES (?, ?, ?, ?)",
        [licenseKey, maxActivations, expiresAt, 1], // System user ID = 1
        function (err) {
          if (err) {
            console.error("Error inserting license key:", err);
            reject(err);
          } else {
            licenseKeys.push({
              id: this.lastID,
              key: licenseKey,
              maxActivations,
              expiresAt,
            });
            resolve();
          }
        }
      );
    });
  }

  return licenseKeys;
}

// Helper function to generate license key
function generateLicenseKey() {
  const crypto = require("crypto");
  const segments = [];
  for (let i = 0; i < 4; i++) {
    segments.push(crypto.randomBytes(4).toString("hex").toUpperCase());
  }
  return segments.join("-");
}

module.exports = router;
