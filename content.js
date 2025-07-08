// Use dynamic import for ES modules in content script
async function getExtractorForSite(hostname) {
  if (hostname.endsWith("quizlet.com")) {
    const src = chrome.runtime.getURL("extractors/quizlet.js");
    const mod = await import(src);
    return new mod.default();
  } else if (hostname.endsWith("quizizz.com") || hostname.endsWith("wayground.com")) {
    const src = chrome.runtime.getURL("extractors/quizizz.js");
    const mod = await import(src);
    return new mod.default();
  }
  // Add more site extractors here as needed
  return null;
}

// Import common utilities
async function importUtils() {
  const src = chrome.runtime.getURL("extractors/utils.js");
  const mod = await import(src);
  return {
    uploadData: mod.uploadData,
    createQRData: mod.createQRData,
    generateQRCode: mod.generateQRCode,
  };
}

// Utility: Upload text to a list of public paste services via background script
const PASTE_SERVICES = [
  {
    name: "paste.rs",
    upload: async (text) => {
      return new Promise((resolve) => {
        chrome.runtime.sendMessage({ action: "uploadText", service: "paste.rs", text }, (response) => {
          resolve(response || { url: null, error: "No response from background script." });
        });
      });
    },
  },
  {
    name: "0x0.st",
    upload: async (text) => {
      return new Promise((resolve) => {
        chrome.runtime.sendMessage({ action: "uploadText", service: "0x0.st", text }, (response) => {
          resolve(response || { url: null, error: "No response from background script." });
        });
      });
    },
  },
  // Add more services here if needed
];

async function uploadToTextHosting(text) {
  let lastError = null;
  for (const service of PASTE_SERVICES) {
    const result = await service.upload(text);
    if (result && result.url) {
      if (result.error) window._qrUploadWarning = result.error;
      else window._qrUploadWarning = null;
      return result.url;
    }
    if (result && result.error) lastError = result.error;
  }
  window._qrUploadWarning = lastError;
  return null;
}

// Utility: Upload to your own backend as fallback
async function uploadToOwnServer(text) {
  try {
    const resp = await fetch("https://your-backend.example.com/api/flashcards", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ data: text }),
    });
    if (resp.ok) {
      const result = await resp.json();
      if (result && result.url) return result.url;
    }
  } catch {}
  return null;
}

// Main function to get a retrieval URL for large data
async function getRetrievalUrl(text) {
  let url = await uploadToTextHosting(text);
  if (url) return url;
  url = await uploadToOwnServer(text);
  if (url) return url;
  return null;
}

// Update insertSendToAppDiv to fallback to direct JSON QR if all text hosting fails
async function insertSendToAppDiv() {
  if (document.getElementById("quiz-qr-send-to-app")) return;

  // Only the button initially
  const btn = document.createElement("button");
  btn.id = "quiz-qr-send-to-app";
  btn.style.fontSize = "15px";
  btn.style.padding = "8px 16px";
  btn.style.background = "linear-gradient(90deg, #0078D4 0%, #00B4D8 100%)";
  btn.style.color = "#fff";
  btn.style.border = "none";
  btn.style.borderRadius = "6px";
  btn.style.cursor = "pointer";
  btn.style.boxShadow = "0 1px 4px rgba(0,0,0,0.10)";
  btn.style.transition = "background 0.3s, transform 0.2s, box-shadow 0.2s, opacity 0.5s";
  btn.style.display = "flex";
  btn.style.alignItems = "center";
  btn.style.gap = "8px";
  btn.style.position = "fixed";
  btn.style.bottom = "24px";
  btn.style.right = "24px";
  btn.style.zIndex = "9999";
  btn.style.opacity = "0";
  setTimeout(() => {
    btn.style.opacity = "1";
  }, 10);

  const icon = document.createElement("img");
  icon.src = chrome.runtime.getURL("icon256.png");
  icon.style.width = "20px";
  icon.style.height = "20px";
  icon.style.borderRadius = "4px";
  icon.style.display = "inline-block";
  btn.appendChild(document.createTextNode("Send to "));
  btn.appendChild(icon);
  btn.appendChild(document.createTextNode("EzQuiz"));

  document.body.appendChild(btn);

  // QR and label container (hidden initially)
  const qrDiv = document.createElement("div");
  qrDiv.style.position = "fixed";
  qrDiv.style.bottom = "24px";
  qrDiv.style.right = "24px";
  qrDiv.style.background = "white";
  qrDiv.style.border = "1px solid #ccc";
  qrDiv.style.borderRadius = "12px";
  qrDiv.style.boxShadow = "0 2px 8px rgba(0,0,0,0.15)";
  qrDiv.style.padding = "12px";
  qrDiv.style.zIndex = "9999";
  qrDiv.style.display = "flex";
  qrDiv.style.flexDirection = "column";
  qrDiv.style.alignItems = "center";
  qrDiv.style.minWidth = "180px";
  qrDiv.style.maxWidth = "220px";
  qrDiv.style.opacity = "0";
  qrDiv.style.transition = "opacity 0.5s";
  qrDiv.style.pointerEvents = "none";

  // Spinner
  const spinner = document.createElement("div");
  spinner.style.display = "none";
  spinner.style.justifyContent = "center";
  spinner.style.alignItems = "center";
  spinner.style.margin = "10px 0";
  spinner.innerHTML = `
    <div style=\"position: relative; width: 64px; height: 64px;\">
      <img src=\"${chrome.runtime.getURL("icon256.png")}\" style=\"width: 48px; height: 48px; position: absolute; top: 8px; left: 8px; z-index: 2; border-radius: 8px;\" />
      <div style=\"position: absolute; top: 0; left: 0; width: 64px; height: 64px; z-index: 1; display: flex; justify-content: center; align-items: center;\">
        <svg style=\"animation: spin 1s linear infinite;\" width=\"64\" height=\"64\" viewBox=\"0 0 64 64\">
          <circle cx=\"32\" cy=\"32\" r=\"28\" stroke=\"#0078D4\" stroke-width=\"6\" fill=\"none\" stroke-linecap=\"round\" stroke-dasharray=\"44 60\"/>
        </svg>
      </div>
    </div>
    <style>@keyframes spin { 100% { transform: rotate(360deg); } }</style>
  `;
  qrDiv.appendChild(spinner);

  // QR image
  const qrImg = document.createElement("img");
  qrImg.style.display = "none";
  qrImg.style.marginBottom = "10px";
  qrImg.style.borderRadius = "8px";
  qrImg.style.boxShadow = "0 1px 4px rgba(0,0,0,0.10)";
  qrImg.style.transition = "box-shadow 0.3s, transform 0.3s, opacity 0.5s";
  qrImg.style.opacity = "0";
  qrDiv.appendChild(qrImg);

  // Scan label
  const scanLabel = document.createElement("div");
  scanLabel.style.display = "none";
  scanLabel.style.fontWeight = "bold";
  scanLabel.style.fontSize = "15px";
  scanLabel.style.letterSpacing = "0.5px";
  scanLabel.style.marginBottom = "4px";
  scanLabel.style.color = "#0078D4";
  scanLabel.style.display = "flex";
  scanLabel.style.alignItems = "center";
  scanLabel.style.gap = "6px";
  const scanIcon = document.createElement("img");
  scanIcon.src = chrome.runtime.getURL("icon256.png");
  scanIcon.style.width = "18px";
  scanIcon.style.height = "18px";
  scanIcon.style.borderRadius = "4px";
  scanLabel.appendChild(document.createTextNode("Scan with "));
  scanLabel.appendChild(scanIcon);
  scanLabel.appendChild(document.createTextNode("EzQuiz"));
  qrDiv.appendChild(scanLabel);

  // Error message
  const errorDiv = document.createElement("div");
  errorDiv.style.color = "#b00020";
  errorDiv.style.fontSize = "13px";
  errorDiv.style.margin = "8px 0 0 0";
  errorDiv.style.maxWidth = "180px";
  errorDiv.style.wordBreak = "break-word";
  qrDiv.appendChild(errorDiv);

  document.body.appendChild(qrDiv);

  btn.onclick = async function () {
    btn.style.display = "none";
    qrDiv.style.opacity = "1";
    qrDiv.style.pointerEvents = "auto";
    spinner.style.display = "flex";
    qrImg.style.display = "none";
    scanLabel.style.display = "none";
    errorDiv.textContent = "";

    let errorMsg = "";
    const hostname = window.location.hostname.replace(/^www\./, "");
    const extractor = await getExtractorForSite(hostname);
    const utils = await importUtils();

    let dataToUpload = null;
    let dataType = null;

    if (extractor) {
      const flashcards = extractor.getFlashcards();
      const quizzes = await extractor.getQuizzes();

      if (flashcards.length > 0) {
        dataToUpload = JSON.stringify(flashcards);
        dataType = "flashcards";
      } else if (quizzes.length > 0) {
        dataToUpload = JSON.stringify(quizzes);
        dataType = "quizzes";
      }
    }

    spinner.style.display = "none";

    if (dataToUpload && dataType) {
      try {
        const uploadUrl = await utils.uploadData(dataToUpload);
        if (uploadUrl) {
          const qrData = utils.createQRData(dataType, uploadUrl);
          const qrCodeUrl = utils.generateQRCode(qrData);

          qrImg.src = qrCodeUrl;
          qrImg.alt = `QR code for ${dataType} data`;
          qrImg.style.display = "block";
          setTimeout(() => {
            qrImg.style.opacity = "1";
          }, 50);
          scanLabel.style.display = "flex";
        } else {
          errorMsg = "Failed to upload data to paste services.";
          if (window._qrUploadWarning) errorMsg += " " + window._qrUploadWarning;
        }
      } catch (e) {
        errorMsg = "Error processing data: " + e.message;
      }
    } else {
      errorMsg = "No quiz or flashcard data found on this page.";
    }

    if (errorMsg) {
      errorDiv.textContent = errorMsg;
    }
  };
}

// Only show the QR code on supported quiz sites
const DEFAULT_SITES = ["quizlet.com", "kahoot.it", "socrative.com", "forms.google.com", "quizizz.com", "wayground.com", "blooket.com", "gimkit.com"];

function getSupportedSites(callback) {
  if (chrome && chrome.storage && chrome.storage.local) {
    chrome.storage.local.get(["supportedSites"], (result) => {
      callback(result.supportedSites || DEFAULT_SITES);
    });
  } else {
    callback(DEFAULT_SITES);
  }
}

function isSupportedSite(url, sites) {
  try {
    const hostname = new URL(url).hostname.replace(/^www\./, "");
    return sites.some((site) => hostname.endsWith(site));
  } catch {
    return false;
  }
}

getSupportedSites((sites) => {
  if (isSupportedSite(window.location.href, sites)) {
    insertSendToAppDiv();
  }
});

chrome.runtime.onMessage.addListener((msg, sender, sendResponse) => {
  if (msg.action === "extractQuiz") {
    (async () => {
      try {
        const hostname = window.location.hostname.replace(/^www\./, "");
        const extractor = await getExtractorForSite(hostname);

        if (extractor) {
          const quizzes = await extractor.getQuizzes();
          const flashcards = extractor.getFlashcards();

          sendResponse({
            quiz: quizzes.length > 0 ? quizzes : flashcards,
            success: true,
          });
        } else {
          sendResponse({ quiz: [], success: false, error: "No extractor found for this site" });
        }
      } catch (e) {
        sendResponse({ quiz: [], success: false, error: e.message });
      }
    })();
    return true; // Keep the message port open for async response
  }
});
