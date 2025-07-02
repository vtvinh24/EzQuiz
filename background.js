// Background script (not used much for this simple extension)
chrome.runtime.onInstalled.addListener(() => {
  console.log("Quiz QR Generator installed.");
});

chrome.runtime.onMessage.addListener((msg, sender, sendResponse) => {
  if (msg.action === "uploadText") {
    (async () => {
      if (msg.service === "paste.rs") {
        try {
          const resp = await fetch("https://paste.rs", {
            method: "POST",
            headers: { "Content-Type": "text/plain" },
            body: msg.text,
          });
          const url = await resp.text();
          if (resp.status === 201 && url.startsWith("http")) {
            sendResponse({ url: url.trim(), error: null });
          } else if (resp.status === 206 && url.startsWith("http")) {
            sendResponse({ url: url.trim(), error: "Warning: Only part of the data was uploaded to paste.rs (206 Partial)." });
          } else {
            sendResponse({ url: null, error: `paste.rs: Upload failed with status ${resp.status}` });
          }
        } catch (e) {
          sendResponse({ url: null, error: "paste.rs error: " + (e && e.message ? e.message : e) });
        }
      } else if (msg.service === "0x0.st") {
        try {
          let form = new FormData();
          // Set a custom filename and expiration (e.g., 30 days = 720 hours)
          form.append("file", new Blob([msg.text], { type: "text/plain" }), "flashcards.json");
          form.append("expires", "720"); // 30 days
          // Optionally, add a 'secret' field for a harder-to-guess URL
          // form.append("secret", "1");
          const resp = await fetch("https://0x0.st", { method: "POST", body: form });
          const url = await resp.text();
          if (resp.ok && url.startsWith("http")) {
            sendResponse({ url: url.trim(), error: null });
          } else {
            sendResponse({ url: null, error: `0x0.st: Upload failed with status ${resp.status}` });
          }
        } catch (e) {
          sendResponse({ url: null, error: "0x0.st error: " + (e && e.message ? e.message : e) });
        }
      }
    })();
    return true; // Keep the message port open for async sendResponse
  }
});
