// --- Site management logic ---
const SUPPORTED_SITES = [
  {
    name: "Quizlet",
    domain: "quizlet.com",
    features: [
      { label: "Flashcards", status: "Implemented" },
      { label: "Quiz", status: "Coming soon" },
    ],
  },
  {
    name: "Kahoot",
    domain: "kahoot.it",
    features: [{ label: "Quiz", status: "Coming soon" }],
  },
  {
    name: "Socrative",
    domain: "socrative.com",
    features: [{ label: "Quiz", status: "Coming soon" }],
  },
  {
    name: "Google Forms",
    domain: "forms.google.com",
    features: [{ label: "Quiz", status: "Coming soon" }],
  },
  {
    name: "Quizizz",
    domain: "quizizz.com / wayground.com",
    features: [{ label: "Quiz", status: "Implemented" }],
  },
  {
    name: "Blooket",
    domain: "blooket.com",
    features: [{ label: "Quiz", status: "Coming soon" }],
  },
  {
    name: "Gimkit",
    domain: "gimkit.com",
    features: [{ label: "Quiz", status: "Coming soon" }],
  },
];

// Collapsible implemented sites list
function renderSiteList() {
  const container = document.getElementById("site-manager");
  container.innerHTML = "";

  // Create collapse button
  const collapseBtn = document.createElement("button");
  collapseBtn.textContent = "Show Supported Sites";
  collapseBtn.className = "ezquiz-btn";
  collapseBtn.style.width = "100%";
  collapseBtn.style.marginBottom = "10px";
  collapseBtn.style.fontWeight = "bold";
  collapseBtn.style.fontSize = "15px";
  collapseBtn.style.background = "#0078D4";
  collapseBtn.style.color = "#fff";
  collapseBtn.style.border = "none";
  collapseBtn.style.borderRadius = "6px";
  collapseBtn.style.padding = "10px 0";
  collapseBtn.style.cursor = "pointer";
  collapseBtn.style.transition = "background 0.2s";
  collapseBtn.addEventListener("mouseenter", () => (collapseBtn.style.background = "#005fa3"));
  collapseBtn.addEventListener("mouseleave", () => (collapseBtn.style.background = "#0078D4"));

  let expanded = false;
  const sitesWrapper = document.createElement("div");
  sitesWrapper.style.display = "none";

  collapseBtn.addEventListener("click", () => {
    expanded = !expanded;
    sitesWrapper.style.display = expanded ? "block" : "none";
    collapseBtn.textContent = expanded ? "Hide Supported Sites" : "Show Supported Sites";
  });

  container.appendChild(collapseBtn);
  container.appendChild(sitesWrapper);

  SUPPORTED_SITES.forEach((site) => {
    const implementedFeatures = site.features.filter((f) => f.status === "Implemented");
    if (implementedFeatures.length === 0) return;

    const siteDiv = document.createElement("div");
    siteDiv.style.marginBottom = "18px";
    siteDiv.style.background = "#f8fafd";
    siteDiv.style.borderRadius = "8px";
    siteDiv.style.boxShadow = "0 2px 8px rgba(0,0,0,0.06)";
    siteDiv.style.padding = "14px 18px";
    siteDiv.style.border = "1px solid #e3e8ee";

    const title = document.createElement("div");
    title.textContent = `${site.name}`;
    title.style.fontWeight = "600";
    title.style.fontSize = "16px";
    title.style.marginBottom = "4px";
    title.style.color = "#1a237e";
    siteDiv.appendChild(title);

    const domain = document.createElement("div");
    domain.textContent = site.domain;
    domain.style.fontSize = "12px";
    domain.style.color = "#607d8b";
    domain.style.marginBottom = "8px";
    siteDiv.appendChild(domain);

    const ul = document.createElement("ul");
    ul.style.margin = "0 0 0 8px";
    ul.style.padding = "0";
    ul.style.listStyle = "none";
    implementedFeatures.forEach((feature) => {
      const li = document.createElement("li");
      li.style.display = "flex";
      li.style.alignItems = "center";
      li.style.marginBottom = "2px";
      const icon = document.createElement("span");
      icon.innerHTML = "&#10003;";
      icon.style.color = "#43a047";
      icon.style.fontWeight = "bold";
      icon.style.fontSize = "18px";
      icon.style.marginRight = "8px";
      li.appendChild(icon);
      const label = document.createElement("span");
      label.textContent = feature.label;
      label.style.color = "#222";
      label.style.fontSize = "15px";
      li.appendChild(label);
      ul.appendChild(li);
    });
    siteDiv.appendChild(ul);
    sitesWrapper.appendChild(siteDiv);
  });
}

// Import utilities for popup
async function importUtils() {
  try {
    const src = chrome.runtime.getURL("extractors/utils.js");
    const mod = await import(src);
    return {
      uploadData: mod.uploadData,
      createQRData: mod.createQRData,
      generateQRCode: mod.generateQRCode,
    };
  } catch (e) {
    console.error("Failed to import utils:", e);
    return null;
  }
}

// Extract data from current tab
async function extractFromCurrentTab() {
  try {
    const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
    const hostname = new URL(tab.url).hostname.replace(/^www\./, "");

    // Check if it's a supported site
    if (hostname.endsWith("quizizz.com") || hostname.endsWith("wayground.com")) {
      return new Promise((resolve) => {
        chrome.tabs.sendMessage(tab.id, { action: "extractQuiz" }, (response) => {
          resolve(response);
        });
      });
    }

    return null;
  } catch (e) {
    console.error("Failed to extract from current tab:", e);
    return null;
  }
}

// Add extraction button
function addExtractionButton() {
  const container = document.getElementById("site-manager");

  // Add a separator
  const separator = document.createElement("div");
  separator.style.borderTop = "1px solid #ccc";
  separator.style.margin = "16px 0";
  container.appendChild(separator);

  // Add extraction section
  const extractSection = document.createElement("div");
  extractSection.style.marginBottom = "12px";

  const extractTitle = document.createElement("div");
  extractTitle.textContent = "Extract from Current Tab";
  extractTitle.style.fontWeight = "bold";
  extractTitle.style.marginBottom = "8px";
  extractSection.appendChild(extractTitle);

  const extractButton = document.createElement("button");
  extractButton.textContent = "Extract & Generate QR";
  extractButton.style.padding = "8px 16px";
  extractButton.style.backgroundColor = "#0078D4";
  extractButton.style.color = "white";
  extractButton.style.border = "none";
  extractButton.style.borderRadius = "4px";
  extractButton.style.cursor = "pointer";
  extractButton.style.fontSize = "14px";
  extractButton.style.width = "100%";

  extractButton.addEventListener("click", async () => {
    extractButton.disabled = true;
    extractButton.textContent = "Extracting...";

    try {
      const results = await extractFromCurrentTab();
      const utils = await importUtils();

      if (results && results.quiz && results.quiz.length > 0 && utils) {
        const uploadUrl = await utils.uploadData(JSON.stringify(results.quiz));
        if (uploadUrl) {
          const qrData = utils.createQRData("quizzes", uploadUrl);
          showQRPopup(utils.generateQRCode(qrData));
        } else {
          showPopupMessage("Failed to upload quiz data to paste services.", "error");
        }
      } else {
        showPopupMessage("No quiz data found on current tab or page not supported.", "error");
      }
    } catch (e) {
      showPopupMessage("Error extracting data: " + e.message, "error");
    } finally {
      extractButton.disabled = false;
      extractButton.textContent = "Extract & Generate QR";
    }
  });

  extractSection.appendChild(extractButton);
  container.appendChild(extractSection);
}

// Show QR code popup
function showQRPopup(qrCodeUrl) {
  const popup = document.createElement("div");
  popup.style.position = "fixed";
  popup.style.top = "0";
  popup.style.left = "0";
  popup.style.width = "100%";
  popup.style.height = "100%";
  popup.style.backgroundColor = "rgba(0,0,0,0.5)";
  popup.style.display = "flex";
  popup.style.alignItems = "center";
  popup.style.justifyContent = "center";
  popup.style.zIndex = "9999";

  const content = document.createElement("div");
  content.style.backgroundColor = "white";
  content.style.padding = "20px";
  content.style.borderRadius = "8px";
  content.style.textAlign = "center";
  content.style.boxShadow = "0 4px 12px rgba(0,0,0,0.3)";

  const title = document.createElement("h3");
  title.textContent = "Quiz QR Code";
  title.style.marginBottom = "16px";
  content.appendChild(title);

  const qrImg = document.createElement("img");
  qrImg.src = qrCodeUrl;
  qrImg.style.maxWidth = "200px";
  qrImg.style.height = "auto";
  qrImg.style.marginBottom = "16px";
  content.appendChild(qrImg);

  const closeBtn = document.createElement("button");
  closeBtn.textContent = "Close";
  closeBtn.style.padding = "8px 16px";
  closeBtn.style.backgroundColor = "#666";
  closeBtn.style.color = "white";
  closeBtn.style.border = "none";
  closeBtn.style.borderRadius = "4px";
  closeBtn.style.cursor = "pointer";
  closeBtn.addEventListener("click", () => {
    document.body.removeChild(popup);
  });
  content.appendChild(closeBtn);

  popup.appendChild(content);
  document.body.appendChild(popup);
}

// Utility to show a message div in the popup
function showPopupMessage(message, type = "info") {
  let msgDiv = document.getElementById("ezquiz-popup-message");
  if (!msgDiv) {
    msgDiv = document.createElement("div");
    msgDiv.id = "ezquiz-popup-message";
    msgDiv.style.position = "fixed";
    msgDiv.style.top = "16px";
    msgDiv.style.left = "50%";
    msgDiv.style.transform = "translateX(-50%)";
    msgDiv.style.zIndex = "10000";
    msgDiv.style.minWidth = "180px";
    msgDiv.style.maxWidth = "90vw";
    msgDiv.style.padding = "10px 18px";
    msgDiv.style.borderRadius = "6px";
    msgDiv.style.fontSize = "15px";
    msgDiv.style.fontWeight = "bold";
    msgDiv.style.boxShadow = "0 2px 8px rgba(0,0,0,0.13)";
    msgDiv.style.textAlign = "center";
    msgDiv.style.pointerEvents = "none";
    document.body.appendChild(msgDiv);
  }
  msgDiv.textContent = message;
  msgDiv.style.background = type === "error" ? "#ffebee" : "#e3f2fd";
  msgDiv.style.color = type === "error" ? "#c62828" : "#1565c0";
  msgDiv.style.border = type === "error" ? "1.5px solid #c62828" : "1.5px solid #1565c0";
  msgDiv.style.opacity = "1";
  setTimeout(() => {
    msgDiv.style.transition = "opacity 0.5s";
    msgDiv.style.opacity = "0";
    setTimeout(() => {
      if (msgDiv.parentNode) msgDiv.parentNode.removeChild(msgDiv);
    }, 600);
  }, 2200);
}

// Style all popup buttons to match the main style
function styleAllPopupButtons() {
  const btns = document.querySelectorAll("button");
  btns.forEach((btn) => {
    btn.classList.add("ezquiz-btn");
    btn.style.background = "#0078D4";
    btn.style.color = "#fff";
    btn.style.border = "none";
    btn.style.borderRadius = "6px";
    btn.style.padding = "10px 0";
    btn.style.fontWeight = "bold";
    btn.style.fontSize = "15px";
    btn.style.cursor = "pointer";
    btn.style.transition = "background 0.2s";
    btn.addEventListener("mouseenter", () => (btn.style.background = "#005fa3"));
    btn.addEventListener("mouseleave", () => (btn.style.background = "#0078D4"));
  });
}

// Render the supported sites list immediately on popup open
renderSiteList();

// Add extraction button
addExtractionButton();

// Style all popup buttons
styleAllPopupButtons();
