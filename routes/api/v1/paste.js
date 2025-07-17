const express = require("express");
const router = express.Router();
const { v4: uuidv4 } = require("uuid");
const { db } = require("../../../config/database");

// Utility function to generate short paste ID
function generatePasteId() {
  const chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  let result = "";
  for (let i = 0; i < 8; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

// Utility function to detect content type
function detectContentType(content, ext = null) {
  if (ext) {
    const extMap = {
      md: "text/markdown",
      mdown: "text/markdown",
      markdown: "text/markdown",
      html: "text/html",
      htm: "text/html",
      js: "text/javascript",
      json: "application/json",
      xml: "application/xml",
      css: "text/css",
      py: "text/x-python",
      java: "text/x-java",
      cpp: "text/x-c++src",
      c: "text/x-csrc",
      php: "text/x-php",
      rb: "text/x-ruby",
      go: "text/x-go",
      rs: "text/x-rust",
      sh: "text/x-shellscript",
      sql: "text/x-sql",
      yaml: "text/yaml",
      yml: "text/yaml",
      toml: "text/x-toml",
      csv: "text/csv",
      txt: "text/plain",
    };
    return extMap[ext.toLowerCase()] || "text/plain";
  }

  // Try to auto-detect based on content
  if (content.startsWith("{") || content.startsWith("[")) {
    try {
      JSON.parse(content);
      return "application/json";
    } catch (e) {
      // Not valid JSON
    }
  }

  if (content.includes("<!DOCTYPE html") || content.includes("<html")) {
    return "text/html";
  }

  if (content.includes("# ") || content.includes("## ") || content.includes("### ")) {
    return "text/markdown";
  }

  return "text/plain";
}

// Utility function to render markdown to HTML
function renderMarkdown(content) {
  // Simple markdown renderer (you could use a library like marked for more features)
  return content
    .replace(/^### (.*$)/gim, "<h3>$1</h3>")
    .replace(/^## (.*$)/gim, "<h2>$1</h2>")
    .replace(/^# (.*$)/gim, "<h1>$1</h1>")
    .replace(/\*\*(.*)\*\*/gim, "<strong>$1</strong>")
    .replace(/\*(.*)\*/gim, "<em>$1</em>")
    .replace(/!\[([^\]]*)\]\(([^\)]+)\)/gim, '<img alt="$1" src="$2" />')
    .replace(/\[([^\]]+)\]\(([^\)]+)\)/gim, '<a href="$2">$1</a>')
    .replace(/\n/gim, "<br>");
}

// Utility function to syntax highlight code
function highlightCode(content, ext) {
  // Simple syntax highlighting (you could use a library like highlight.js for better results)
  const escaped = content.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");

  return `<!DOCTYPE html>
<html>
<head>
    <title>Code Paste - ${ext}</title>
    <style>
        body { 
            font-family: 'Courier New', monospace; 
            margin: 0; 
            padding: 20px; 
            background: #1e1e1e; 
            color: #d4d4d4; 
        }
        .code-container { 
            background: #2d2d30; 
            padding: 15px; 
            border-radius: 5px; 
            overflow-x: auto; 
        }
        .line-numbers {
            color: #858585;
            margin-right: 15px;
            user-select: none;
        }
        pre { margin: 0; white-space: pre-wrap; }
    </style>
</head>
<body>
    <div class="code-container">
        <pre><code>${escaped}</code></pre>
    </div>
</body>
</html>`;
}

// POST /api/v1/paste - Create a new paste
router.post("/", async (req, res) => {
  try {
    const content = req.body;
    const rawContent = typeof content === "string" ? content : Buffer.isBuffer(content) ? content.toString() : JSON.stringify(content);

    if (!rawContent || rawContent.trim().length === 0) {
      return res.status(400).json({
        error: "Content cannot be empty",
      });
    }

    // Check size limits (10MB from server.js config)
    const maxSize = 10 * 1024 * 1024; // 10MB
    const contentSize = Buffer.byteLength(rawContent, "utf8");

    if (contentSize > maxSize) {
      // Return partial content with 206 status
      const truncatedContent = rawContent.substring(0, maxSize);
      const pasteId = generatePasteId();

      db.run(
        `INSERT INTO pastes (id, content, content_type, size, ip_address, user_agent, created_at) 
         VALUES (?, ?, ?, ?, ?, ?, datetime('now'))`,
        [pasteId, truncatedContent, detectContentType(truncatedContent), Buffer.byteLength(truncatedContent, "utf8"), req.ip, req.get("User-Agent")],
        function (err) {
          if (err) {
            console.error("Database error:", err);
            return res.status(500).json({ error: "Failed to create paste" });
          }

          const baseUrl = `${req.protocol}://${req.get("host")}`;
          return res.status(206).send(`${baseUrl}/api/v1/paste/${pasteId}\n`);
        }
      );
      return;
    }

    // Create paste with full content
    const pasteId = generatePasteId();

    // Check if paste ID already exists (very unlikely but just in case)
    db.get("SELECT id FROM pastes WHERE id = ?", [pasteId], (err, row) => {
      if (err) {
        console.error("Database error:", err);
        return res.status(500).json({ error: "Failed to create paste" });
      }

      if (row) {
        // Generate a new ID if collision occurs
        return res.status(500).json({ error: "ID collision, please try again" });
      }

      db.run(
        `INSERT INTO pastes (id, content, content_type, size, ip_address, user_agent, created_at) 
         VALUES (?, ?, ?, ?, ?, ?, datetime('now'))`,
        [pasteId, rawContent, detectContentType(rawContent), contentSize, req.ip, req.get("User-Agent")],
        function (err) {
          if (err) {
            console.error("Database error:", err);
            return res.status(500).json({ error: "Failed to create paste" });
          }

          const baseUrl = `${req.protocol}://${req.get("host")}`;
          return res.status(201).send(`${baseUrl}/api/v1/paste/${pasteId}\n`);
        }
      );
    });
  } catch (error) {
    console.error("Error creating paste:", error);
    res.status(500).json({ error: "Internal server error" });
  }
});

// GET /api/v1/paste/:id - Retrieve a paste as plain text
router.get("/:id", (req, res) => {
  const { id } = req.params;

  if (!id || id.length !== 8) {
    return res.status(400).json({ error: "Invalid paste ID" });
  }

  db.get("SELECT * FROM pastes WHERE id = ? AND is_deleted = 0", [id], (err, row) => {
    if (err) {
      console.error("Database error:", err);
      return res.status(500).json({ error: "Failed to retrieve paste" });
    }

    if (!row) {
      return res.status(404).json({ error: "Paste not found" });
    }

    // Update view count
    db.run("UPDATE pastes SET view_count = view_count + 1 WHERE id = ?", [id]);

    // Check if paste has expired
    if (row.expires_at && new Date(row.expires_at) < new Date()) {
      return res.status(410).json({ error: "Paste has expired" });
    }

    // Check view limits
    if (row.max_views && row.view_count >= row.max_views) {
      return res.status(410).json({ error: "Paste view limit exceeded" });
    }

    res.set("Content-Type", "text/plain");
    res.send(row.content);
  });
});

// GET /api/v1/paste/:id.:ext - Retrieve a paste with format/rendering
router.get("/:id.:ext", (req, res) => {
  const { id, ext } = req.params;

  if (!id || id.length !== 8) {
    return res.status(400).json({ error: "Invalid paste ID" });
  }

  db.get("SELECT * FROM pastes WHERE id = ? AND is_deleted = 0", [id], (err, row) => {
    if (err) {
      console.error("Database error:", err);
      return res.status(500).json({ error: "Failed to retrieve paste" });
    }

    if (!row) {
      return res.status(404).json({ error: "Paste not found" });
    }

    // Update view count
    db.run("UPDATE pastes SET view_count = view_count + 1 WHERE id = ?", [id]);

    // Check if paste has expired
    if (row.expires_at && new Date(row.expires_at) < new Date()) {
      return res.status(410).json({ error: "Paste has expired" });
    }

    // Check view limits
    if (row.max_views && row.view_count >= row.max_views) {
      return res.status(410).json({ error: "Paste view limit exceeded" });
    }

    // Handle different extensions
    const lowerExt = ext.toLowerCase();

    if (["md", "mdown", "markdown"].includes(lowerExt)) {
      // Render as markdown
      const html = `<!DOCTYPE html>
<html>
<head>
    <title>Markdown Paste</title>
    <style>
        body { 
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; 
            max-width: 800px; 
            margin: 0 auto; 
            padding: 20px; 
            line-height: 1.6; 
        }
        h1, h2, h3 { color: #333; }
        code { 
            background: #f4f4f4; 
            padding: 2px 4px; 
            border-radius: 3px; 
        }
        pre { 
            background: #f4f4f4; 
            padding: 10px; 
            border-radius: 5px; 
            overflow-x: auto; 
        }
    </style>
</head>
<body>
    ${renderMarkdown(row.content)}
</body>
</html>`;
      res.set("Content-Type", "text/html");
      return res.send(html);
    }

    // Check if it's a code file extension
    const codeExtensions = ["js", "py", "java", "cpp", "c", "php", "rb", "go", "rs", "sh", "sql", "html", "css", "json", "xml", "yaml", "yml"];
    if (codeExtensions.includes(lowerExt)) {
      const html = highlightCode(row.content, lowerExt);
      res.set("Content-Type", "text/html");
      return res.send(html);
    }

    // Return with appropriate content type
    const contentType = detectContentType(row.content, lowerExt);
    res.set("Content-Type", contentType);
    res.send(row.content);
  });
});

// DELETE /api/v1/paste/:id - Delete a paste
router.delete("/:id", (req, res) => {
  const { id } = req.params;

  if (!id || id.length !== 8) {
    return res.status(400).json({ error: "Invalid paste ID" });
  }

  db.get("SELECT * FROM pastes WHERE id = ? AND is_deleted = 0", [id], (err, row) => {
    if (err) {
      console.error("Database error:", err);
      return res.status(500).json({ error: "Failed to delete paste" });
    }

    if (!row) {
      return res.status(404).json({ error: "Paste not found" });
    }

    // Soft delete the paste
    db.run("UPDATE pastes SET is_deleted = 1 WHERE id = ?", [id], function (err) {
      if (err) {
        console.error("Database error:", err);
        return res.status(500).json({ error: "Failed to delete paste" });
      }

      res.status(200).json({ message: "Paste deleted successfully" });
    });
  });
});

// GET /api/v1/paste/:id/info - Get paste metadata
router.get("/:id/info", (req, res) => {
  const { id } = req.params;

  if (!id || id.length !== 8) {
    return res.status(400).json({ error: "Invalid paste ID" });
  }

  db.get("SELECT id, content_type, size, created_at, expires_at, view_count, max_views FROM pastes WHERE id = ? AND is_deleted = 0", [id], (err, row) => {
    if (err) {
      console.error("Database error:", err);
      return res.status(500).json({ error: "Failed to retrieve paste info" });
    }

    if (!row) {
      return res.status(404).json({ error: "Paste not found" });
    }

    res.json({
      id: row.id,
      contentType: row.content_type,
      size: row.size,
      createdAt: row.created_at,
      expiresAt: row.expires_at,
      viewCount: row.view_count,
      maxViews: row.max_views,
      isExpired: row.expires_at && new Date(row.expires_at) < new Date(),
      viewLimitReached: row.max_views && row.view_count >= row.max_views,
    });
  });
});

module.exports = router;
