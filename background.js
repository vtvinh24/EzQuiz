// Background script (not used much for this simple extension)
chrome.runtime.onInstalled.addListener(() => {
  console.log("Quiz QR Generator installed.");
});

chrome.runtime.onMessage.addListener((msg, sender, sendResponse) => {
  if (msg.action === "uploadText") {
    (async () => {
      if (msg.service === "ezquiz") {
        try {
          const resp = await fetch("https://server-horusoul.onrender.com/api/paste", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ text: msg.text }),
          });
          const data = await resp.json();
          if (resp.ok && data.url && data.url.startsWith("http")) {
            sendResponse({ url: data.url.trim(), error: null });
          } else {
            sendResponse({ url: null, error: `ezquiz: Upload failed with status ${resp.status}` });
          }
        } catch (e) {
          sendResponse({ url: null, error: "ezquiz error: " + (e && e.message ? e.message : e) });
        }
      }
    })();
    return true;
  }
});
