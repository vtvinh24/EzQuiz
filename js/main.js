// Enhanced media gallery functionality with smooth transitions
let currentMediaIndex = 0;
const mediaItems = document.querySelectorAll(".media-item");
const thumbnails = document.querySelectorAll(".thumbnail");

function showMedia(index) {
  mediaItems.forEach((item, i) => {
    if (i === index) {
      item.classList.add("active");
      item.style.display = "block";
      item.style.transform = "scale(0.95)";
      item.style.opacity = "0";
      setTimeout(() => {
        item.style.transform = "scale(1)";
        item.style.opacity = "1";
      }, 100);
    } else {
      item.classList.remove("active");
      item.style.display = "none";
      item.style.opacity = "0";
    }
  });

  thumbnails.forEach((thumb, i) => {
    thumb.classList.toggle("active", i === index);
    if (i === index) {
      thumb.style.transform = "translateY(-5px) scale(1.05)";
      setTimeout(() => {
        thumb.style.transform = "translateY(0) scale(1)";
      }, 300);
    } else {
      thumb.style.transform = "translateY(0) scale(1)";
    }
  });

  currentMediaIndex = index;
}

function scrollThumbnails(direction) {
  const thumbnailScroll = document.querySelector(".thumbnail-scroll");
  const scrollAmount = 140; // Adjusted for new thumbnail width + gap
  const currentScroll = thumbnailScroll.scrollLeft;

  thumbnailScroll.scrollTo({
    left: currentScroll + direction * scrollAmount,
    behavior: "smooth",
  });
}

function playVideo() {
  // Enhanced video play functionality
  const videoContainer = document.querySelector(".video-container");
  const playButton = document.querySelector(".play-button");

  if (playButton) {
    playButton.style.transform = "translate(-50%, -50%) scale(1.2)";
    setTimeout(() => {
      playButton.style.transform = "translate(-50%, -50%) scale(1)";
      alert("Demo video would play here in a real implementation!");
    }, 200);
  }
}

function toggleMobileMenu() {
  const nav = document.querySelector(".nav");
  const toggle = document.querySelector(".mobile-menu-toggle");

  nav.classList.toggle("nav-open");
  toggle.classList.toggle("active");

  // Add smooth animation
  if (nav.classList.contains("nav-open")) {
    nav.style.transform = "translateY(-10px)";
    nav.style.opacity = "0";
    setTimeout(() => {
      nav.style.transform = "translateY(0)";
      nav.style.opacity = "1";
    }, 50);
  }
}

// Enhanced smooth scrolling with momentum and mobile menu auto-close
function smoothScrollTo(targetId) {
  const target = document.getElementById(targetId);
  if (target) {
    // Close mobile menu if open
    const nav = document.querySelector(".nav");
    const toggle = document.querySelector(".mobile-menu-toggle");
    if (nav.classList.contains("nav-open")) {
      nav.classList.remove("nav-open");
      toggle.classList.remove("active");
    }

    // Add slight delay and easing for smoother experience
    const targetPosition = target.offsetTop - 70; // Account for header height
    window.scrollTo({
      top: targetPosition,
      behavior: "smooth",
    });

    // Add visual feedback
    target.style.transform = "scale(1.01)";
    setTimeout(() => {
      target.style.transform = "scale(1)";
    }, 300);
  }
}

// Enhanced section snap scrolling with fluid motion
let isScrolling = false;
let scrollTimeout;

function handleSectionSnap() {
  if (isScrolling) return;

  const sections = document.querySelectorAll(".section");
  const scrollPosition = window.scrollY;
  const windowHeight = window.innerHeight;

  let targetSection = null;
  let minDistance = Infinity;

  sections.forEach((section) => {
    const sectionTop = section.offsetTop;
    const distance = Math.abs(scrollPosition - sectionTop);

    if (distance < minDistance) {
      minDistance = distance;
      targetSection = section;
    }
  });

  if (targetSection && minDistance > 80) {
    // Increased threshold for smoother experience
    isScrolling = true;

    // Add smooth transition effect
    targetSection.style.transform = "translateY(10px)";
    targetSection.style.opacity = "0.9";

    targetSection.scrollIntoView({
      behavior: "smooth",
      block: "start",
    });

    setTimeout(() => {
      targetSection.style.transform = "translateY(0)";
      targetSection.style.opacity = "1";
      isScrolling = false;
    }, 800);
  }
}

// Enhanced auto-advance through media with pause on hover
let mediaInterval;

function startMediaAutoAdvance() {
  mediaInterval = setInterval(() => {
    if (mediaItems.length > 0) {
      const nextIndex = (currentMediaIndex + 1) % mediaItems.length;
      showMedia(nextIndex);
    }
  }, 5000); // Reduced to 5 seconds for better engagement
}

function pauseMediaAutoAdvance() {
  clearInterval(mediaInterval);
}

function resumeMediaAutoAdvance() {
  startMediaAutoAdvance();
}

// Enhanced initialization with smooth interactions
document.addEventListener("DOMContentLoaded", function () {
  showMedia(0);
  startMediaAutoAdvance();

  // Add scroll event listener for section snapping
  window.addEventListener("scroll", () => {
    clearTimeout(scrollTimeout);
    scrollTimeout = setTimeout(handleSectionSnap, 150);
  });

  // Enhanced wheel event handling for better snapping
  let wheelTimeout;
  window.addEventListener(
    "wheel",
    (e) => {
      clearTimeout(wheelTimeout);
      wheelTimeout = setTimeout(() => {
        if (!isScrolling) {
          handleSectionSnap();
        }
      }, 100);
    },
    { passive: true }
  );

  // Enhanced navbar transparency with smooth transitions
  const header = document.querySelector(".header.afloat");
  window.addEventListener("scroll", function () {
    if (window.scrollY > 30) {
      header.classList.add("scrolled");
      header.style.transform = "translateY(0)";
    } else {
      header.classList.remove("scrolled");
    }
  });

  // Add hover effects for media gallery
  const steamGallery = document.querySelector(".steam-gallery");
  if (steamGallery) {
    steamGallery.addEventListener("mouseenter", pauseMediaAutoAdvance);
    steamGallery.addEventListener("mouseleave", resumeMediaAutoAdvance);
  }

  // Add enhanced interactions for feature cards
  const featureTexts = document.querySelectorAll(".feature-text.blur-bg");
  featureTexts.forEach((feature) => {
    feature.addEventListener("mouseenter", () => {
      feature.style.transform = "translateY(-8px) scale(1.02)";
    });

    feature.addEventListener("mouseleave", () => {
      feature.style.transform = "translateY(0) scale(1)";
    });
  });

  // Add smooth hover effects for buttons
  const buttons = document.querySelectorAll(".app-store-btn, .extension-btn");
  buttons.forEach((button) => {
    button.addEventListener("mouseenter", () => {
      button.style.transform = "translateY(-3px) scale(1.02)";
    });

    button.addEventListener("mouseleave", () => {
      button.style.transform = "translateY(0) scale(1)";
    });
  });

  // Add loading animation for images
  const images = document.querySelectorAll("img");
  images.forEach((img) => {
    img.addEventListener("load", () => {
      img.style.opacity = "0";
      img.style.transform = "scale(0.95)";
      setTimeout(() => {
        img.style.transition = "opacity 0.4s ease, transform 0.4s ease";
        img.style.opacity = "1";
        img.style.transform = "scale(1)";
      }, 100);
    });
  });

  // Add intersection observer for scroll animations
  const observerOptions = {
    threshold: 0.1,
    rootMargin: "0px 0px -50px 0px",
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.style.animation = "fadeInUp 0.8s ease-out forwards";
      }
    });
  }, observerOptions);

  // Observe all sections for smooth entrance animations
  const sections = document.querySelectorAll(".section");
  sections.forEach((section) => {
    observer.observe(section);
  });

  // Add click outside handler for mobile menu
  document.addEventListener("click", (e) => {
    const nav = document.querySelector(".nav");
    const toggle = document.querySelector(".mobile-menu-toggle");
    const header = document.querySelector(".header");

    if (nav.classList.contains("nav-open") && !header.contains(e.target)) {
      nav.classList.remove("nav-open");
      toggle.classList.remove("active");
    }
  });

  // Add escape key handler for mobile menu
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
      const nav = document.querySelector(".nav");
      const toggle = document.querySelector(".mobile-menu-toggle");

      if (nav.classList.contains("nav-open")) {
        nav.classList.remove("nav-open");
        toggle.classList.remove("active");
      }
    }
  });
});
