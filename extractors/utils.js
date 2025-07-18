// utils.js
// Common utility functions for uploading data and creating QR codes

/**
 * Uploads data to external paste services
 * @param {string} data - The data to upload
 * @returns {Promise<string|null>} - The URL of the uploaded data or null if failed
 */
async function uploadData(data) {
  const PASTE_SERVICES = [
    {
      name: "ezquiz",
      upload: async (text) => {
        return new Promise((resolve) => {
          chrome.runtime.sendMessage({ action: "uploadText", service: "ezquiz", text }, (response) => {
            resolve(response || { url: null, error: "No response from background script." });
          });
        });
      },
    },
  ];

  let lastError = null;
  for (const service of PASTE_SERVICES) {
    const result = await service.upload(data);
    if (result && result.url) {
      return result.url;
    }
    if (result && result.error) lastError = result.error;
  }

  // Set global error for debugging
  window._qrUploadWarning = lastError;
  return null;
}

/**
 * Creates QR code data for the app
 * @param {string} dataType - "flashcards" or "quizzes"
 * @param {string} url - The URL containing the data
 * @returns {string} - JSON string for QR code
 */
function createQRData(dataType, url) {
  return JSON.stringify({
    "data-type": dataType,
    "type": "request",
    "url": url,
  });
}

/**
 * Generates QR code using qr-server API
 * @param {string} data - The data to encode
 * @returns {string} - QR code image URL
 */
function generateQRCode(data) {
  const encodedData = encodeURIComponent(data);
  return `https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${encodedData}`;
}

/**
 * Extracts clean text from an HTML element, removing scripts and cleaning up whitespace
 * @param {HTMLElement} element - The element to extract text from
 * @returns {string} - Clean text content
 */
function extractTextFromElement(element) {
  if (!element) return "";

  // Clone the element to avoid modifying the original
  const clone = element.cloneNode(true);

  // Remove script tags and other non-text elements
  const scripts = clone.querySelectorAll("script, style");
  scripts.forEach((script) => script.remove());

  // Get text content and clean it up
  let text = clone.textContent || clone.innerText || "";
  text = text.replace(/\s+/g, " ").trim();

  // Remove common artifacts
  text = text.replace(/\u00A0/g, " "); // Non-breaking spaces
  text = text.replace(/\s+/g, " ").trim();

  return text;
}

/**
 * Waits for an element to appear in the DOM or for a condition to be met
 * @param {Function} condition - Function that returns true when condition is met
 * @param {number} maxWait - Maximum time to wait in milliseconds
 * @param {number} interval - Interval between checks in milliseconds
 * @returns {Promise<boolean>} - True if condition was met, false if timeout
 */
async function waitForCondition(condition, maxWait = 3000, interval = 100) {
  const startTime = Date.now();

  while (Date.now() - startTime < maxWait) {
    if (condition()) {
      return true;
    }
    await new Promise((resolve) => setTimeout(resolve, interval));
  }

  return false;
}

/**
 * Waits for element to appear in DOM
 * @param {HTMLElement} parent - Parent element to search in
 * @param {string} selector - CSS selector to find
 * @param {number} maxWait - Maximum time to wait in milliseconds
 * @returns {Promise<HTMLElement|null>} - Found element or null if timeout
 */
async function waitForElement(parent, selector, maxWait = 3000) {
  const found = await waitForCondition(() => {
    return parent.querySelector(selector) !== null;
  }, maxWait);

  return found ? parent.querySelector(selector) : null;
}

/**
 * Normalizes URL by removing common prefixes and getting current location
 * @returns {string} - Current page URL
 */
function getCurrentURL() {
  return window.location.href;
}

/**
 * Sleeps for a specified amount of time
 * @param {number} ms - Milliseconds to sleep
 * @returns {Promise<void>}
 */
function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * Base extractor class with common functionality
 */
class BaseExtractor {
  constructor() {
    this.isExtracting = false;
    this.extractionComplete = false;
  }

  /**
   * Safe extraction wrapper that prevents duplicate extractions
   * @param {Function} extractionFunc - The extraction function to run
   * @returns {Promise<void>}
   */
  async _safeExtract(extractionFunc) {
    if (this.isExtracting || this.extractionComplete) return;
    this.isExtracting = true;

    try {
      await extractionFunc();
      this.extractionComplete = true;
    } catch (error) {
      console.error(`${this.constructor.name}: Error during extraction:`, error);
    } finally {
      this.isExtracting = false;
    }
  }

  /**
   * Log extraction progress
   * @param {string} message - Message to log
   * @param {any} data - Optional data to log
   */
  _log(message, data = null) {
    const className = this.constructor.name;
    if (data !== null) {
      console.log(`${className}: ${message}`, data);
    } else {
      console.log(`${className}: ${message}`);
    }
  }

  /**
   * Log extraction errors
   * @param {string} message - Error message
   * @param {Error} error - Optional error object
   */
  _logError(message, error = null) {
    const className = this.constructor.name;
    if (error) {
      console.error(`${className}: ${message}`, error);
    } else {
      console.error(`${className}: ${message}`);
    }
  }

  /**
   * Get current URL
   * @returns {string}
   */
  getURL() {
    return getCurrentURL();
  }
}

export { uploadData, createQRData, generateQRCode, extractTextFromElement, waitForCondition, waitForElement, getCurrentURL, sleep, BaseExtractor };
