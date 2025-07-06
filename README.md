# EzQuiz Backend Server

A secure backend server for the EzQuiz application with AI-powered quiz generation and robust license management system.

## Features

### 🤖 AI-Powered Quiz Generation

- Generate quizzes from text prompts
- Support for image-based quiz generation
- Multiple choice and single choice questions
- Powered by Google's Gemini AI

### 🔐 Secure Authentication

- JWT-based authentication for admin users
- Password hashing with bcryptjs
- Rate limiting for authentication endpoints
- Token expiration and refresh capabilities

### 📜 License Management

- Generate license keys with configurable limits
- Device fingerprinting and binding
- Activation tracking and management
- License expiration and revocation
- Secure validation for client applications

### 🛡️ Security Features

- Comprehensive rate limiting
- Input validation and sanitization
- Security headers and CORS protection
- Request logging and audit trails
- Database security with parameterized queries

## Quick Start

### Prerequisites

- Node.js 16+
- npm or yarn
- Google Gemini API key

### Installation

1. **Clone and install dependencies**

```bash
npm install
```

2. **Set up environment variables**

```bash
cp .env.template .env
# Edit .env with your configuration
```

3. **Configure your .env file**

```env
# Server Configuration
PORT=3000
NODE_ENV=development
ALLOWED_ORIGINS=http://localhost:3001

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
JWT_EXPIRE=24h

# Gemini AI Configuration
GEMINI_API_KEY=your-gemini-api-key-here

# Admin Configuration
DEFAULT_ADMIN_PASSWORD=admin123
```

4. **Start the server**

```bash
# Development
npm run dev

# Production
npm start
```

The server will start on `http://localhost:3000` (or your configured PORT).

## API Endpoints

### Authentication

- `POST /api/v1/auth/login` - Admin login
- `POST /api/v1/auth/change-password` - Change admin password
- `GET /api/v1/auth/verify` - Verify JWT token

### Quiz Generation

- `POST /api/v1/generate/generate-quiz` - Generate quiz from text/image

### License Management

- `POST /api/v1/license/generate` - Generate license keys (Admin only)
- `POST /api/v1/license/validate` - Validate license key (Public)
- `GET /api/v1/license/list` - List all licenses (Admin only)
- `POST /api/v1/license/revoke` - Revoke license key (Admin only)

### Payment Management

- `GET /api/v1/payment/plans` - Get available payment plans
- `POST /api/v1/payment/create-payment-intent` - Create payment intent
- `POST /api/v1/payment/webhook` - Payment webhook (for testing)

### System

- `GET /health` - Health check endpoint

## Usage Examples

### Admin Login

```bash
curl -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### Generate Quiz

```bash
curl -X POST http://localhost:3000/api/v1/generate/generate-quiz \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "Create 5 questions about JavaScript programming"
  }'
```

### Generate License Keys

```bash
curl -X POST http://localhost:3000/api/v1/license/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "amount": 10,
    "maxActivations": 2,
    "expiresInDays": 30
  }'
```

### Validate License Key

```bash
curl -X POST http://localhost:3000/api/v1/license/validate \
  -H "Content-Type: application/json" \
  -d '{
    "key": "ABCD1234-EFGH5678-IJKL9012-MNOP3456",
    "deviceInfo": {
      "platform": "Windows",
      "version": "10.0.19041"
    },
    "installId": "unique-install-id"
  }'
```

## Security Considerations

### Production Deployment

1. **Change default credentials** - Update admin password immediately
2. **Use strong JWT secrets** - Generate a secure random string (64+ characters)
3. **Enable HTTPS** - Configure SSL/TLS certificates
4. **Set up API keys** - Enable API key validation for public endpoints
5. **Configure CORS** - Restrict origins to your frontend domains

### Payment Integration

For secure payment processing, implement webhooks from your payment provider:

```javascript
// Example payment webhook handler
app.post("/webhooks/payment", (req, res) => {
  // Verify webhook signature
  const signature = req.headers["stripe-signature"];

  try {
    const event = stripe.webhooks.constructEvent(req.body, signature, process.env.STRIPE_WEBHOOK_SECRET);

    if (event.type === "payment_intent.succeeded") {
      const paymentIntent = event.data.object;

      // Generate license keys based on payment
      generateLicenseKeys(paymentIntent.metadata.license_amount);

      // Send keys to customer
      sendLicenseKeysToUser(paymentIntent.receipt_email, licenseKeys);
    }

    res.status(200).json({ received: true });
  } catch (err) {
    res.status(400).send(`Webhook Error: ${err.message}`);
  }
});
```

## Database Schema

The server uses SQLite with the following main tables:

### admin_users

- `id` - Primary key
- `username` - Unique admin username
- `password_hash` - Hashed password
- `created_at` - Account creation timestamp
- `last_login` - Last login timestamp
- `is_active` - Account status

### license_keys

- `id` - Primary key
- `license_key` - Unique license key
- `status` - active/revoked/expired
- `created_at` - Creation timestamp
- `expires_at` - Expiration timestamp
- `max_activations` - Maximum device activations
- `current_activations` - Current activation count
- `created_by` - Admin who generated the key

### license_activations

- `id` - Primary key
- `license_key_id` - Foreign key to license_keys
- `device_fingerprint` - SHA-256 hash of device info
- `device_info` - JSON containing device details
- `install_id` - Client-provided installation ID
- `ip_address` - IP address of activation
- `user_agent` - User agent string
- `activated_at` - Activation timestamp
- `last_seen` - Last validation timestamp
- `is_active` - Activation status

## Development

### Project Structure

```
├── server.js                 # Main server file
├── config/
│   └── database.js          # Database configuration
├── routes/
│   └── api/v1/
│       ├── auth.js          # Authentication routes
│       ├── license.js       # License management routes
│       ├── payment.js       # Payment management routes
│       └── generate/
│           └── generate.js  # Quiz generation routes
├── middleware/
│   └── security.js          # Security middleware
├── package.json
├── .env.template
└── SECURITY.md             # Security documentation
```

### Adding New Features

1. Create new route files in `routes/`
2. Add middleware in `middleware/`
3. Update `server.js` to include new routes
4. Update database schema in `config/database.js`
5. Add tests and documentation

### Testing

```bash
# Run tests (when available)
npm test

# Check for security vulnerabilities
npm audit

# Update dependencies
npm update
```

## Monitoring & Logging

The server includes comprehensive logging:

- Request logging with IP tracking
- Authentication attempt logging
- License validation logging
- Error logging with stack traces

For production, consider integrating with:

- **Winston** for structured logging
- **Sentry** for error tracking
- **New Relic** for performance monitoring
- **Datadog** for infrastructure monitoring

## Deployment

### Docker Deployment

```dockerfile
# Dockerfile example
FROM node:16-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

COPY . .

EXPOSE 3000

CMD ["node", "server.js"]
```

### PM2 Process Manager

```bash
# Install PM2
npm install -g pm2

# Start application
pm2 start server.js --name "ezquiz-backend"

# Monitor
pm2 monit

# Auto-restart on system reboot
pm2 startup
pm2 save
```

## Support

For issues and questions:

1. Check the [Security Documentation](SECURITY.md)
2. Review the API documentation above
3. Check server logs for error details
4. Verify environment configuration

## License

This project is licensed under the ISC License.
