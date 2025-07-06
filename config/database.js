const sqlite3 = require("sqlite3").verbose();
const path = require("path");

const DB_PATH = path.join(__dirname, "../database.sqlite");

// Create database connection
const db = new sqlite3.Database(DB_PATH, (err) => {
  if (err) {
    console.error("Error opening database:", err.message);
  } else {
    console.log("✅ Connected to SQLite database");
  }
});

// Initialize database tables
function initDB() {
  // Admin users table
  db.serialize(() => {
    db.run(`
      CREATE TABLE IF NOT EXISTS admin_users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT UNIQUE NOT NULL,
        password_hash TEXT NOT NULL,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        last_login DATETIME,
        is_active BOOLEAN DEFAULT 1
      )
    `);

    // License keys table
    db.run(`
      CREATE TABLE IF NOT EXISTS license_keys (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        license_key TEXT UNIQUE NOT NULL,
        status TEXT DEFAULT 'active' CHECK(status IN ('active', 'revoked', 'expired')),
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        expires_at DATETIME,
        max_activations INTEGER DEFAULT 1,
        current_activations INTEGER DEFAULT 0,
        created_by INTEGER,
        FOREIGN KEY (created_by) REFERENCES admin_users (id)
      )
    `);

    // License activations table (for tracking device bindings)
    db.run(`
      CREATE TABLE IF NOT EXISTS license_activations (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        license_key_id INTEGER NOT NULL,
        device_fingerprint TEXT NOT NULL,
        device_info TEXT,
        install_id TEXT,
        ip_address TEXT,
        user_agent TEXT,
        activated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        last_seen DATETIME DEFAULT CURRENT_TIMESTAMP,
        is_active BOOLEAN DEFAULT 1,
        FOREIGN KEY (license_key_id) REFERENCES license_keys (id),
        UNIQUE(license_key_id, device_fingerprint)
      )
    `);

    // Payment intents table (for tracking payments)
    db.run(`
      CREATE TABLE IF NOT EXISTS payment_intents (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        payment_intent_id TEXT UNIQUE NOT NULL,
        customer_email TEXT NOT NULL,
        customer_name TEXT NOT NULL,
        plan TEXT NOT NULL,
        amount REAL NOT NULL,
        currency TEXT NOT NULL,
        status TEXT DEFAULT 'created',
        metadata TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        completed_at DATETIME
      )
    `);

    // Create default admin user if not exists
    const bcrypt = require("bcryptjs");
    const defaultPassword = process.env.DEFAULT_ADMIN_PASSWORD || "admin123";

    db.get("SELECT id FROM admin_users WHERE username = ?", ["admin"], (err, row) => {
      if (err) {
        console.error("Error checking admin user:", err);
      } else if (!row) {
        const hashedPassword = bcrypt.hashSync(defaultPassword, 10);
        db.run("INSERT INTO admin_users (username, password_hash) VALUES (?, ?)", ["admin", hashedPassword], (err) => {
          if (err) {
            console.error("Error creating default admin:", err);
          } else {
            console.log("✅ Default admin user created (username: admin, password: admin123)");
            console.log("⚠️  Please change the default password after first login!");
          }
        });
      }
    });

    console.log("✅ Database tables initialized");
  });
}

module.exports = { db, initDB };
