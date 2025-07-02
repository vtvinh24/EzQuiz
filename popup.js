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
    name: "Quizziz",
    domain: "quizziz.com",
    features: [{ label: "Quiz", status: "Coming soon" }],
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

function renderSiteList() {
  const container = document.getElementById("site-manager");
  container.innerHTML = "";
  SUPPORTED_SITES.forEach((site) => {
    const siteDiv = document.createElement("div");
    siteDiv.style.marginBottom = "12px";
    const title = document.createElement("div");
    title.textContent = `${site.name} (${site.domain})`;
    title.style.fontWeight = "bold";
    title.style.marginBottom = "2px";
    siteDiv.appendChild(title);
    const ul = document.createElement("ul");
    ul.style.margin = "0 0 0 12px";
    ul.style.padding = "0";
    site.features.forEach((feature) => {
      const li = document.createElement("li");
      li.style.listStyle = "none";
      li.style.display = "flex";
      li.style.alignItems = "center";
      const icon = document.createElement("span");
      icon.style.display = "inline-block";
      icon.style.width = "18px";
      icon.style.marginRight = "6px";
      icon.style.fontSize = "16px";
      icon.style.lineHeight = "1";
      if (feature.status === "Implemented") {
        icon.innerHTML = "&#10003;"; // HTML code for check mark
        icon.style.color = "#0078D4";
      } else {
        icon.innerHTML = "&#9203;"; // HTML code for hourglass
        icon.style.color = "#888";
      }
      li.appendChild(icon);
      const label = document.createElement("span");
      label.textContent = feature.label;
      label.style.color = feature.status === "Implemented" ? "#0078D4" : "#888";
      li.appendChild(label);
      ul.appendChild(li);
    });
    siteDiv.appendChild(ul);
    container.appendChild(siteDiv);
  });
}

// Render the supported sites list immediately on popup open
renderSiteList();
