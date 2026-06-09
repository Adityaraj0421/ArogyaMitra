// ── Arogya Mitra Academic PPT Generator — v2 (QA fixes applied) ──────────────
"use strict";

const PPTX_PATH  = "/Users/adityaraj0421/.nvm/versions/node/v25.3.0/lib/node_modules/pptxgenjs";
const ICONS_PATH = "/Users/adityaraj0421/.nvm/versions/node/v25.3.0/lib/node_modules/react-icons";
const SHARP_PATH = "/Users/adityaraj0421/.nvm/versions/node/v25.3.0/lib/node_modules/sharp";
const REACT_PATH = "/Users/adityaraj0421/.nvm/versions/node/v25.3.0/lib/node_modules/react";
const RDS_PATH   = "/Users/adityaraj0421/.nvm/versions/node/v25.3.0/lib/node_modules/react-dom";

const pptxgen        = require(PPTX_PATH);
const React          = require(REACT_PATH);
const ReactDOMServer = require(RDS_PATH + "/server");
const sharp          = require(SHARP_PATH);
const { FaLeaf, FaDatabase, FaMobileAlt, FaLock, FaUsers, FaBrain, FaCloudUploadAlt,
        FaSearch, FaCreditCard, FaLanguage, FaCheckCircle, FaFlask,
        FaCogs, FaShieldAlt } = require(ICONS_PATH + "/fa");

// ── Palette ───────────────────────────────────────────────────────────────────
const C = {
  darkGreen  : "1B4D1C",
  forestGreen: "2C5F2D",
  moss       : "6B9E3B",
  lightMoss  : "97BC62",
  sageBg     : "EEF5E8",
  white      : "FFFFFF",
  charcoal   : "1C2B1D",
  midGrey    : "4A6741",
  lineGrey   : "C5D9BF",
  statBg     : "163B17",
  accentAmber: "E6A817",
};

// ── Icon helper ───────────────────────────────────────────────────────────────
async function iconB64(Component, color, size = 256) {
  const svg = ReactDOMServer.renderToStaticMarkup(
    React.createElement(Component, { color: `#${color}`, size: String(size) })
  );
  const buf = await sharp(Buffer.from(svg)).png().toBuffer();
  return "image/png;base64," + buf.toString("base64");
}

// Shadow factory — MUST return a new object each call (PptxGenJS mutates in-place)
const makeShadow = (opacity = 0.12) => ({
  type: "outer", color: "000000", blur: 8, offset: 3, angle: 135, opacity
});

// ── Shared helpers ────────────────────────────────────────────────────────────
function lightBg(slide) { slide.background = { color: C.white }; }
function sageBg(slide)  { slide.background = { color: C.sageBg }; }

/** Left accent bar + title for content slides */
function addHeader(slide, title, subtitle = "") {
  slide.addShape("rect", {
    x: 0, y: 0, w: 0.18, h: 5.625,
    fill: { color: C.forestGreen }, line: { color: C.forestGreen }
  });
  slide.addText(title, {
    x: 0.35, y: 0.22, w: 9.3, h: 0.55,
    fontSize: 26, bold: true, color: C.forestGreen,
    fontFace: "Calibri", margin: 0
  });
  if (subtitle) {
    slide.addText(subtitle, {
      x: 0.35, y: 0.78, w: 9.3, h: 0.3,
      fontSize: 12, color: C.midGrey, fontFace: "Calibri", margin: 0
    });
  }
}

function addCard(slide, x, y, w, h, fillColor = C.white) {
  slide.addShape("rect", {
    x, y, w, h,
    fill: { color: fillColor },
    line: { color: C.lineGrey, width: 0.5 },
    shadow: makeShadow(0.1)
  });
}

// ═════════════════════════════════════════════════════════════════════════════
//  MAIN
// ═════════════════════════════════════════════════════════════════════════════
async function main() {
  const pres = new pptxgen();
  pres.layout = "LAYOUT_16x9";
  pres.title  = "Arogya Mitra – Indigenous Medicine Portal";
  pres.author = "Vibhu et al.";

  // Pre-render all icons
  const iLeaf    = await iconB64(FaLeaf,           C.white);
  const iLeafG   = await iconB64(FaLeaf,           C.forestGreen);
  const iBrain   = await iconB64(FaBrain,          C.white);
  const iBrainG  = await iconB64(FaBrain,          C.moss);
  const iDB      = await iconB64(FaDatabase,       C.white);
  const iDBG     = await iconB64(FaDatabase,       C.moss);
  const iMobile  = await iconB64(FaMobileAlt,      C.white);
  const iMobileG = await iconB64(FaMobileAlt,      C.moss);
  const iLock    = await iconB64(FaLock,           C.white);
  const iLockG   = await iconB64(FaLock,           C.moss);
  const iSearchG = await iconB64(FaSearch,         C.moss);
  const iUsers   = await iconB64(FaUsers,          C.white);
  const iUsersG  = await iconB64(FaUsers,          C.moss);
  const iCloud   = await iconB64(FaCloudUploadAlt, C.white);
  const iCloudG  = await iconB64(FaCloudUploadAlt, C.moss);
  const iPay     = await iconB64(FaCreditCard,     C.white);
  const iPayG    = await iconB64(FaCreditCard,     C.moss);
  const iLang    = await iconB64(FaLanguage,       C.white);
  const iLangG   = await iconB64(FaLanguage,       C.moss);
  const iCheck   = await iconB64(FaCheckCircle,    C.lightMoss);
  const iCogs    = await iconB64(FaCogs,           C.white);
  const iCogsG   = await iconB64(FaCogs,           C.moss);
  const iShield  = await iconB64(FaShieldAlt,      C.white);
  const iFlaskW  = await iconB64(FaFlask,          C.white);

  // ──────────────────────────────────────────────────────────────────────────
  // SLIDE 1 — TITLE
  // FIX: removed leaf icon that overlapped "Developed by" text;
  //      raised contrast on footer + eyebrow text; moved team line to clear area
  // ──────────────────────────────────────────────────────────────────────────
  {
    const s = pres.addSlide();
    s.background = { color: C.darkGreen };

    // Large leaf watermark top-right (decorative only)
    s.addImage({ data: iLeaf, x: 6.8, y: -0.6, w: 3.8, h: 3.8, transparency: 82 });

    // Bottom-left decorative circle
    s.addShape("ellipse", {
      x: -1.2, y: 3.5, w: 3.5, h: 3.5,
      fill: { color: C.forestGreen, transparency: 60 },
      line: { color: C.forestGreen, transparency: 60 }
    });

    // Eyebrow — higher contrast (was 97BC62, now FFFFFF light)
    s.addText("ACADEMIC PROJECT  •  ANDROID APPLICATION", {
      x: 0.6, y: 0.42, w: 8.8, h: 0.3,
      fontSize: 9, color: "D4EDCE", fontFace: "Calibri",
      charSpacing: 3, align: "left"
    });

    // App name
    s.addText("Arogya Mitra", {
      x: 0.6, y: 1.05, w: 9, h: 1.1,
      fontSize: 58, bold: true, color: C.white,
      fontFace: "Georgia", align: "left"
    });

    // Subtitle
    s.addText("Indigenous Medicine Portal", {
      x: 0.6, y: 2.18, w: 9, h: 0.55,
      fontSize: 26, color: C.lightMoss, fontFace: "Calibri", align: "left"
    });

    // Thin divider
    s.addShape("rect", {
      x: 0.6, y: 2.85, w: 3.2, h: 0.04,
      fill: { color: C.lightMoss }, line: { color: C.lightMoss }
    });

    // Tagline
    s.addText("Bridging Traditional Ayurvedic Wisdom with Modern AI Technology", {
      x: 0.6, y: 3.05, w: 8.5, h: 0.45,
      fontSize: 14, color: "AEDBA0", fontFace: "Calibri",
      italic: true, align: "left"
    });

    // Team line — FIX: removed small leaf icon that overlapped here; now plain text only
    s.addText("Developed by: Vibhu et al.", {
      x: 0.6, y: 4.38, w: 5, h: 0.28,
      fontSize: 11, color: "AEDBA0", fontFace: "Calibri", align: "left"
    });

    // Footer — FIX: brighter colour for legibility
    s.addText("Platform: Android (Java)   •   AI: TensorFlow Lite   •   Backend: Firebase   •   Version: 1.0   •   April 2026", {
      x: 0.6, y: 4.75, w: 8.8, h: 0.3,
      fontSize: 10, color: "AEDBA0", fontFace: "Calibri", align: "left"
    });
  }

  // ──────────────────────────────────────────────────────────────────────────
  // SLIDE 2 — PROBLEM STATEMENT
  // ──────────────────────────────────────────────────────────────────────────
  {
    const s = pres.addSlide();
    lightBg(s);
    addHeader(s, "Problem Statement", "Why does Arogya Mitra need to exist?");

    const problems = [
      { icon: iLeafG,  color: "E8F5E2", title: "Vanishing Traditional Knowledge",
        body: "Indigenous medicinal plant knowledge from North-East India is passed down orally. With each generation, critical information about plant identification, preparation, and uses is permanently lost." },
      { icon: iSearchG, color: "FFF8E1", title: "No Accessible Digital Repository",
        body: "Practitioners and learners lack a unified, searchable database of indigenous drugs. Existing resources are scattered across textbooks, not optimised for mobile access." },
      { icon: iUsersG, color: "E8EAF6", title: "No Community Contribution Pipeline",
        body: "Local communities hold valuable regional knowledge that is never captured. There is no peer-validated mechanism for community members to contribute and verify new drug entries." },
    ];

    const startY = 1.22;
    const cardH  = 1.18;
    const gap    = 0.22;

    problems.forEach((p, i) => {
      const y = startY + i * (cardH + gap);
      addCard(s, 0.35, y, 9.3, cardH, p.color);
      s.addImage({ data: p.icon, x: 0.55, y: y + 0.35, w: 0.46, h: 0.46 });
      s.addText(p.title, {
        x: 1.2, y: y + 0.13, w: 8.1, h: 0.35,
        fontSize: 13, bold: true, color: C.forestGreen, fontFace: "Calibri", margin: 0
      });
      s.addText(p.body, {
        x: 1.2, y: y + 0.52, w: 8.1, h: 0.6,
        fontSize: 11, color: C.charcoal, fontFace: "Calibri", margin: 0
      });
    });
  }

  // ──────────────────────────────────────────────────────────────────────────
  // SLIDE 3 — OBJECTIVES
  // FIX: tightened row gap to match header-to-content gap; reduced dead space
  // ──────────────────────────────────────────────────────────────────────────
  {
    const s = pres.addSlide();
    sageBg(s);
    addHeader(s, "Project Objectives", "Four core goals driving the application");

    const objs = [
      { icon: iBrainG, title: "AI Leaf Identification",
        body: "Enable users to photograph a leaf and instantly identify the medicinal plant using an on-device TFLite CNN model — no internet required for inference." },
      { icon: iDBG, title: "Curated Drug Database",
        body: "Provide a searchable, Firebase-backed database of 31+ indigenous plant species with scientific names, medicinal uses, and preparation methods." },
      { icon: iCloudG, title: "Community Submission & Validation",
        body: "Allow community members to submit new drug entries, which undergo an admin peer-review pipeline before being published to the live database." },
      { icon: iLangG, title: "Multi-Language Accessibility",
        body: "Serve North-East Indian users in English and Assamese (অসমীয়া) with dynamic runtime locale switching via LanguageManager." },
    ];

    // FIX: rows closer together so inter-row gap ≈ header-to-content gap
    const cols = [0.35, 5.15];
    const rows = [1.15, 3.0];  // was [1.2, 3.15]
    const cW = 4.55, cH = 1.68;  // was 1.75

    objs.forEach((o, i) => {
      const x = cols[i % 2];
      const y = rows[Math.floor(i / 2)];
      addCard(s, x, y, cW, cH, C.white);
      s.addShape("rect", {
        x, y, w: cW, h: 0.07,
        fill: { color: C.forestGreen }, line: { color: C.forestGreen }
      });
      s.addImage({ data: o.icon, x: x + 0.22, y: y + 0.2, w: 0.48, h: 0.48 });
      s.addText(o.title, {
        x: x + 0.85, y: y + 0.17, w: cW - 1.0, h: 0.38,
        fontSize: 13, bold: true, color: C.forestGreen, fontFace: "Calibri", margin: 0
      });
      s.addText(o.body, {
        x: x + 0.22, y: y + 0.75, w: cW - 0.38, h: 0.86,
        fontSize: 10.5, color: C.charcoal, fontFace: "Calibri", margin: 0
      });
    });
  }

  // ──────────────────────────────────────────────────────────────────────────
  // SLIDE 4 — TECHNOLOGY STACK
  // FIX: rows centered vertically to eliminate large blank bottom zone
  // ──────────────────────────────────────────────────────────────────────────
  {
    const s = pres.addSlide();
    lightBg(s);
    addHeader(s, "Technology Stack", "Tools, languages, and frameworks used");

    const techs = [
      { label: "Android Studio",       sub: "Hedgehog / Iguana IDE",    color: "3DDC84", icon: iMobileG },
      { label: "Java 8",               sub: "Source language",           color: "F89820", icon: iCogsG   },
      { label: "Kotlin DSL",           sub: "Gradle build scripts",      color: "7F52FF", icon: iCogsG   },
      { label: "TensorFlow Lite",      sub: "v2.16.1 — On-device AI",   color: "FF6F00", icon: iBrainG  },
      { label: "Firebase Auth",        sub: "Email + Google OAuth 2.0", color: "FFCA28", icon: iLockG   },
      { label: "Firebase Realtime DB", sub: "NoSQL JSON tree",          color: "039BE5", icon: iDBG     },
      { label: "Firebase Storage",     sub: "Images & media files",     color: "00897B", icon: iCloudG  },
      { label: "View Binding",         sub: "Type-safe view access",    color: "5E35B1", icon: iCogsG   },
    ];

    const cols2 = [0.35, 2.85, 5.35, 7.85];
    // FIX: rows centered — was [1.2, 3.0], now [1.3, 3.2] with taller cards to fill slide
    const rows2  = [1.3, 3.2];
    const tW = 2.3, tH = 1.72;

    techs.forEach((t, i) => {
      const x = cols2[i % 4];
      const y = rows2[Math.floor(i / 4)];
      addCard(s, x, y, tW, tH, "F9FDF6");
      // Coloured band (FIX: taller band for better visual association — 0.32 was 0.25)
      s.addShape("rect", {
        x, y, w: tW, h: 0.32,
        fill: { color: t.color }, line: { color: t.color }
      });
      s.addImage({ data: t.icon, x: x + 0.12, y: y + 0.45, w: 0.42, h: 0.42 });
      s.addText(t.label, {
        x: x + 0.66, y: y + 0.4, w: tW - 0.78, h: 0.38,
        fontSize: 11, bold: true, color: C.charcoal, fontFace: "Calibri", margin: 0
      });
      s.addText(t.sub, {
        x: x + 0.12, y: y + 0.98, w: tW - 0.18, h: 0.65,
        fontSize: 9.5, color: C.midGrey, fontFace: "Calibri", margin: 0
      });
    });
  }

  // ──────────────────────────────────────────────────────────────────────────
  // SLIDE 5 — SYSTEM ARCHITECTURE
  // FIX: increased gap between tier boxes; matched Firebase box height to tiers
  // ──────────────────────────────────────────────────────────────────────────
  {
    const s = pres.addSlide();
    sageBg(s);
    addHeader(s, "System Architecture", "Three-tier design: Public → Prediction → Admin");

    const tiers = [
      { label: "TIER 1 — Public Layer",        color: C.lightMoss,   textColor: C.charcoal,
        items: ["Plant / Drug Database browsing", "Search & filter medicines", "Multilingual UI (EN / Assamese)"] },
      { label: "TIER 2 — AI Prediction Layer", color: C.forestGreen, textColor: C.white,
        items: ["Leaf photo → TFLite CNN", "31 species, on-device inference", "< 500 ms response time"] },
      { label: "TIER 3 — Admin / Backend",     color: C.darkGreen,   textColor: C.white,
        items: ["Drug submission validation", "User management", "Subscription control"] },
    ];

    const bX = 0.9, bW = 4.8, bH = 1.22;
    // FIX: gap increased from 0.28 → 0.44 so arrow is visible and boxes don't merge
    const gap = 0.44;
    const startY = 1.18;
    // Total tier height: 3*1.22 + 2*0.44 = 3.66 + 0.88 = 4.54
    const totalTierH = 3 * bH + 2 * gap;

    tiers.forEach((t, i) => {
      const y = startY + i * (bH + gap);
      s.addShape("rect", {
        x: bX, y, w: bW, h: bH,
        fill: { color: t.color }, line: { color: t.color },
        shadow: makeShadow(0.15)
      });
      s.addText(t.label, {
        x: bX + 0.18, y: y + 0.1, w: bW - 0.3, h: 0.38,
        fontSize: 12, bold: true, color: t.textColor, fontFace: "Calibri", margin: 0
      });
      t.items.forEach((item, j) => {
        s.addText(`• ${item}`, {
          x: bX + 0.18, y: y + 0.5 + j * 0.24, w: bW - 0.3, h: 0.22,
          fontSize: 10, color: t.textColor, fontFace: "Calibri", margin: 0
        });
      });

      // Arrow connector between boxes
      if (i < 2) {
        const aY = y + bH + 0.04;
        const aH  = gap - 0.1;
        s.addShape("rect", {
          x: bX + bW / 2 - 0.04, y: aY, w: 0.08, h: aH - 0.12,
          fill: { color: C.moss }, line: { color: C.moss }
        });
        // Arrowhead triangle
        s.addShape("ellipse", {
          x: bX + bW / 2 - 0.1, y: aY + aH - 0.2,
          w: 0.2, h: 0.2,
          fill: { color: C.moss }, line: { color: C.moss }
        });
      }
    });

    // FIX: Firebase box height = totalTierH to match tier column exactly
    const fbX = 6.2, fbY = startY;
    s.addShape("rect", {
      x: fbX, y: fbY, w: 3.45, h: totalTierH,
      fill: { color: "FFF8E1" }, line: { color: "FFCA28", width: 1.5 },
      shadow: makeShadow(0.1)
    });
    s.addText("Firebase Backend", {
      x: fbX + 0.15, y: fbY + 0.14, w: 3.12, h: 0.35,
      fontSize: 12, bold: true, color: "E65100", fontFace: "Calibri", margin: 0
    });
    const fbItems = [
      "🔐  Firebase Authentication",
      "📊  Realtime Database (JSON)",
      "☁️   Cloud Storage (media)",
      "📈  Firebase Analytics",
    ];
    // FIX: spacing items evenly within the taller box
    const itemSpacing = (totalTierH - 0.6) / fbItems.length;
    fbItems.forEach((item, i) => {
      s.addText(item, {
        x: fbX + 0.2, y: fbY + 0.6 + i * itemSpacing, w: 3.0, h: 0.55,
        fontSize: 11, color: C.charcoal, fontFace: "Calibri", margin: 0
      });
    });

    // Connector lines to firebase
    [startY + bH / 2, startY + bH + gap + bH / 2].forEach(cy => {
      s.addShape("rect", {
        x: bX + bW, y: cy, w: fbX - (bX + bW), h: 0.03,
        fill: { color: C.lineGrey }, line: { color: C.lineGrey }
      });
    });
  }

  // ──────────────────────────────────────────────────────────────────────────
  // SLIDE 6 — AI: LEAF PREDICTION MODULE
  // FIX: subtitle text to white for contrast; added divider between spec cols
  // ──────────────────────────────────────────────────────────────────────────
  {
    const s = pres.addSlide();
    s.background = { color: "0F2D10" };

    // Left sidebar
    s.addShape("rect", {
      x: 0, y: 0, w: 3.5, h: 5.625,
      fill: { color: C.forestGreen }, line: { color: C.forestGreen }
    });
    s.addImage({ data: iBrain, x: 1.3, y: 0.4, w: 0.9, h: 0.9 });
    s.addText("AI / ML Module", {
      x: 0.15, y: 1.4, w: 3.2, h: 0.45,
      fontSize: 18, bold: true, color: C.white, fontFace: "Calibri", align: "center"
    });
    // FIX: changed subtitle from lightMoss to white for contrast on green bg
    s.addText("Leaf Prediction Engine", {
      x: 0.15, y: 1.88, w: 3.2, h: 0.38,
      fontSize: 13, color: C.white, fontFace: "Calibri", align: "center"
    });

    const specs = [
      ["Framework", "TensorFlow Lite 2.16.1"],
      ["Input",     "150 × 150 px RGB"],
      ["Output",    "Softmax / 31 classes"],
      ["Threads",   "4 CPU (parallel)"],
      ["Inference", "< 500 ms"],
      ["Accuracy",  "85–95% (top-1)"],
    ];

    // FIX: thin separator line between label and value column
    s.addShape("rect", {
      x: 1.28, y: 2.62, w: 0.02, h: 2.7,
      fill: { color: "4A8C4B" }, line: { color: "4A8C4B" }
    });

    specs.forEach(([k, v], i) => {
      const rowY = 2.65 + i * 0.46;  // was 0.44 — slightly more breathing room
      s.addText(`${k}:`, {
        x: 0.18, y: rowY, w: 1.08, h: 0.38,
        fontSize: 9.5, bold: true, color: C.lightMoss, fontFace: "Calibri", margin: 0
      });
      s.addText(v, {
        x: 1.36, y: rowY, w: 2.0, h: 0.38,
        fontSize: 9.5, color: C.white, fontFace: "Calibri", margin: 0
      });
    });

    // Right: inference pipeline
    s.addText("Inference Pipeline", {
      x: 3.7, y: 0.22, w: 6.1, h: 0.45,
      fontSize: 20, bold: true, color: C.white, fontFace: "Calibri", margin: 0
    });

    const steps = [
      { n: "1", label: "Capture / Select Leaf Image",               color: "2D6A4F" },
      { n: "2", label: "Scale Bitmap to 150 × 150 px",              color: "1B7A4A" },
      { n: "3", label: "Normalize pixels ÷ 255 → [0.0, 1.0]",      color: "1E8449" },
      { n: "4", label: "ByteBuffer → TFLite Interpreter (4 threads)", color: "196F3D" },
      { n: "5", label: "Softmax output → ArgMax → Top class",       color: "145A32" },
      { n: "6", label: "Display: Plant Name + Confidence %",        color: "0D6E2A" },
    ];

    const pX = 3.7, pW = 6.0, pH = 0.65, pGap = 0.1;
    steps.forEach((st, i) => {
      const y = 0.88 + i * (pH + pGap);
      s.addShape("rect", {
        x: pX, y, w: pW, h: pH,
        fill: { color: st.color }, line: { color: st.color },
        shadow: makeShadow(0.2)
      });
      s.addShape("ellipse", {
        x: pX + 0.12, y: y + 0.13, w: 0.38, h: 0.38,
        fill: { color: C.accentAmber }, line: { color: C.accentAmber }
      });
      s.addText(st.n, {
        x: pX + 0.12, y: y + 0.13, w: 0.38, h: 0.38,
        fontSize: 11, bold: true, color: C.white, fontFace: "Calibri",
        align: "center", valign: "middle", margin: 0
      });
      s.addText(st.label, {
        x: pX + 0.65, y: y + 0.13, w: pW - 0.78, h: 0.38,
        fontSize: 11, color: C.white, fontFace: "Calibri", margin: 0
      });
    });
  }

  // ──────────────────────────────────────────────────────────────────────────
  // SLIDE 7 — FIREBASE BACKEND
  // ──────────────────────────────────────────────────────────────────────────
  {
    const s = pres.addSlide();
    lightBg(s);
    addHeader(s, "Firebase Backend (BaaS)", "No custom server — all managed by Google Firebase");

    const services = [
      { icon: iLockG,  title: "Firebase Authentication",
        color: "FFF3E0", border: "FFCA28",
        points: ["Email & Password login", "Google Sign-In (OAuth 2.0)", "Auto user profile creation", "Session persistence"] },
      { icon: iDBG,    title: "Realtime Database",
        color: "E3F2FD", border: "039BE5",
        points: ["Cloud-hosted JSON tree", "drugs/ — master records", "drug_to_be_validated/ — pending", "Peoples/ — user profiles"] },
      { icon: iCloudG, title: "Cloud Storage",
        color: "E8F5E9", border: "00897B",
        points: ["Leaf prediction images", "Drug / plant photos", "Profile pictures", "URLs stored in DB records"] },
      { icon: iCogsG,  title: "Drug Validation Pipeline",
        color: "F3E5F5", border: "7B1FA2",
        points: ["User submits → pending node", "Admin reviews in-app", "Approved → moved to drugs/", "Rejected → entry deleted"] },
    ];

    const cols3 = [0.35, 5.15];
    const rows3  = [1.2, 3.12];
    const sW = 4.55, sH = 1.78;

    services.forEach((sv, i) => {
      const x = cols3[i % 2];
      const y = rows3[Math.floor(i / 2)];
      addCard(s, x, y, sW, sH, sv.color);
      s.addShape("rect", {
        x, y, w: sW, h: 0.08,
        fill: { color: sv.border }, line: { color: sv.border }
      });
      s.addImage({ data: sv.icon, x: x + 0.18, y: y + 0.18, w: 0.44, h: 0.44 });
      s.addText(sv.title, {
        x: x + 0.76, y: y + 0.17, w: sW - 0.9, h: 0.38,
        fontSize: 12.5, bold: true, color: C.forestGreen, fontFace: "Calibri", margin: 0
      });
      sv.points.forEach((pt, j) => {
        s.addText([{ text: "  " + pt, options: { bullet: true } }], {
          x: x + 0.18, y: y + 0.68 + j * 0.26, w: sW - 0.32, h: 0.26,
          fontSize: 10, color: C.charcoal, fontFace: "Calibri", margin: 0
        });
      });
    });
  }

  // ──────────────────────────────────────────────────────────────────────────
  // SLIDE 8 — KEY FEATURES & SCREENS
  // FIX: all 6 cards share the same fixed height to avoid uneven grid;
  //      shifted rows up to reduce blank bottom zone
  // ──────────────────────────────────────────────────────────────────────────
  {
    const s = pres.addSlide();
    sageBg(s);
    addHeader(s, "Key Features & App Screens", "19 activities across 11 core screens");

    const features = [
      { icon: iBrainG, title: "AI Leaf Scanner",      color: "E8F8F0",
        body: "Snap a leaf — get plant name + confidence % in < 500 ms. On-device CNN, works offline without internet." },
      { icon: iDBG,    title: "Medicine Library",     color: "E3F2FD",
        body: "RecyclerView of all plants with Glide-cached images, scientific names, and medicinal uses from Firebase." },
      { icon: iUsersG, title: "Community Submission", color: "FFF8E1",
        body: "Submit new drug entries via a form. Admin validates the entry before it is published to the live database." },
      { icon: iLangG,  title: "Bilingual Interface",  color: "F3E5F5",
        body: "Toggle between English and Assamese (অসমীয়া) at runtime using LanguageManager locale switching." },
      { icon: iPayG,   title: "Subscription & Payment", color: "FCE4EC",
        body: "Three tiers — Free, Basic, Premium — with a mock card payment gateway and Firebase subscription update." },
      { icon: iCogsG,  title: "Admin Panel",          color: "E8EAF6",
        body: "Admin accounts access UsersActivity to view all registered users, subscription status, and drug validation queue." },
    ];

    const cols4 = [0.35, 3.52, 6.69];
    // FIX: moved top row up from 1.2 → 1.15; consistent fH for both rows
    const rows4  = [1.15, 3.1];
    const fW = 2.98, fH = 1.82;  // uniform height

    features.forEach((f, i) => {
      const x = cols4[i % 3];
      const y = rows4[Math.floor(i / 3)];
      addCard(s, x, y, fW, fH, f.color);
      s.addShape("rect", {
        x, y, w: fW, h: 0.07,
        fill: { color: C.forestGreen }, line: { color: C.forestGreen }
      });
      s.addImage({ data: f.icon, x: x + 0.15, y: y + 0.18, w: 0.4, h: 0.4 });
      s.addText(f.title, {
        x: x + 0.68, y: y + 0.16, w: fW - 0.8, h: 0.38,
        fontSize: 11.5, bold: true, color: C.forestGreen, fontFace: "Calibri", margin: 0
      });
      s.addText(f.body, {
        x: x + 0.15, y: y + 0.68, w: fW - 0.25, h: 1.08,
        fontSize: 9.5, color: C.charcoal, fontFace: "Calibri", margin: 0
      });
    });
  }

  // ──────────────────────────────────────────────────────────────────────────
  // SLIDE 9 — RESULTS & METRICS
  // FIX: stat cards wider gap (was 0.15 → 0.28); APK box border brighter
  // ──────────────────────────────────────────────────────────────────────────
  {
    const s = pres.addSlide();
    s.background = { color: C.statBg };

    s.addText("Results & Project Metrics", {
      x: 0.5, y: 0.22, w: 9, h: 0.52,
      fontSize: 24, bold: true, color: C.white, fontFace: "Calibri", margin: 0
    });
    s.addText("At a glance — what was delivered", {
      x: 0.5, y: 0.76, w: 9, h: 0.28,
      fontSize: 12, color: C.lightMoss, fontFace: "Calibri", italic: true, margin: 0
    });

    const stats = [
      { num: "19",     label: "Activities\n(Screens)",     icon: iMobile },
      { num: "43",     label: "Java Source\nFiles",        icon: iCogs   },
      { num: "31",     label: "Plant Species\n(AI Model)", icon: iLeaf   },
      { num: "2",      label: "Languages\nSupported",      icon: iLang   },
      { num: "85–95%", label: "AI Top-1\nAccuracy",        icon: iBrain  },
      { num: "3",      label: "Firebase\nServices",        icon: iDB     },
    ];

    // FIX: narrower cards + larger gaps so cards don't crowd each other
    const sW2 = 1.32, sH2 = 1.62, sGap = 0.28;
    const totalW = stats.length * sW2 + (stats.length - 1) * sGap;
    const startX = (10 - totalW) / 2;

    stats.forEach((st, i) => {
      const x = startX + i * (sW2 + sGap);
      const y = 1.15;
      s.addShape("rect", {
        x, y, w: sW2, h: sH2,
        fill: { color: C.forestGreen },
        line: { color: C.lightMoss, width: 0.8 },
        shadow: makeShadow(0.3)
      });
      s.addImage({ data: st.icon, x: x + (sW2 - 0.38) / 2, y: y + 0.12, w: 0.38, h: 0.38 });
      s.addText(st.num, {
        x, y: y + 0.55, w: sW2, h: 0.52,
        fontSize: st.num.length > 3 ? 18 : 26, bold: true,
        color: C.white, fontFace: "Georgia", align: "center", margin: 0
      });
      s.addText(st.label, {
        x, y: y + 1.1, w: sW2, h: 0.45,
        fontSize: 8.5, color: C.lightMoss, fontFace: "Calibri",
        align: "center", margin: 0
      });
    });

    // Completed deliverables checklist
    s.addText("All Deliverables — Completed", {
      x: 0.5, y: 3.05, w: 4.5, h: 0.38,
      fontSize: 13, bold: true, color: C.lightMoss, fontFace: "Calibri", margin: 0
    });

    const completed = [
      "Firebase Auth (Email + Google OAuth)",
      "Realtime Database + Cloud Storage",
      "TFLite Leaf Classifier (31 species)",
      "Drug submission & admin validation",
      "Subscription tiers + mock payment",
      "Multi-language (English + Assamese)",
    ];
    completed.forEach((item, i) => {
      s.addImage({ data: iCheck, x: 0.5, y: 3.52 + i * 0.33, w: 0.24, h: 0.24 });
      s.addText(item, {
        x: 0.84, y: 3.51 + i * 0.33, w: 4.1, h: 0.28,
        fontSize: 10.5, color: C.white, fontFace: "Calibri", margin: 0
      });
    });

    // APK info box — FIX: brighter border (lightMoss not moss)
    s.addShape("rect", {
      x: 5.5, y: 3.05, w: 4.1, h: 2.38,
      fill: { color: "0E3D0F" }, line: { color: C.lightMoss, width: 1.2 },
      shadow: makeShadow(0.2)
    });
    s.addText("APK Build Info", {
      x: 5.68, y: 3.15, w: 3.7, h: 0.35,
      fontSize: 12, bold: true, color: C.lightMoss, fontFace: "Calibri", margin: 0
    });
    const apk = [
      ["Build Type",     "Debug"],
      ["Size",           "~311 MB (debug)"],
      ["Min Android",    "API 27 (Android 8.1)"],
      ["Target Android", "API 34 (Android 14)"],
      ["Build Date",     "April 23, 2026"],
    ];
    apk.forEach(([k, v], i) => {
      s.addText(`${k}:`, {
        x: 5.68, y: 3.6 + i * 0.35, w: 1.55, h: 0.3,
        fontSize: 9.5, bold: true, color: C.lightMoss, fontFace: "Calibri", margin: 0
      });
      s.addText(v, {
        x: 7.25, y: 3.6 + i * 0.35, w: 2.2, h: 0.3,
        fontSize: 9.5, color: C.white, fontFace: "Calibri", margin: 0
      });
    });
  }

  // ──────────────────────────────────────────────────────────────────────────
  // SLIDE 10 — FUTURE WORK
  // ──────────────────────────────────────────────────────────────────────────
  {
    const s = pres.addSlide();
    lightBg(s);
    addHeader(s, "Future Work & Enhancements", "Roadmap for the next phase of development");

    const roadmap = [
      { num: "01", title: "Training Accuracy Report",
        body: "Export precision, recall, F1-score and confusion matrix from the Python training notebook for all 31 classes.", icon: iFlaskW },
      { num: "02", title: "Release APK Build",
        body: "Enable ProGuard / R8 code shrinking to reduce the APK from ~311 MB to an estimated 40–60 MB.", icon: iMobileG },
      { num: "03", title: "Expand Plant Database",
        body: "Scale the TFLite model to 50+ species with training data from North-East Indian indigenous flora.", icon: iLeafG },
      { num: "04", title: "Offline Caching (Room DB)",
        body: "Cache Firebase drug records locally using Room so the app remains usable without internet.", icon: iDBG },
      { num: "05", title: "Firebase Security Rules",
        body: "Harden database access rules to restrict unauthorized reads/writes and protect community data.", icon: iShield },
      { num: "06", title: "AI Feedback Loop",
        body: "Allow users to flag incorrect predictions, building a feedback dataset to retrain and improve accuracy.", icon: iUsersG },
    ];

    const cols5 = [0.35, 3.52, 6.69];
    const rows5  = [1.15, 3.1];
    const rW = 2.98, rH = 1.78;

    roadmap.forEach((r, i) => {
      const x = cols5[i % 3];
      const y = rows5[Math.floor(i / 3)];
      addCard(s, x, y, rW, rH, "F9FDF6");
      s.addShape("ellipse", {
        x: x + 0.15, y: y + 0.16, w: 0.46, h: 0.46,
        fill: { color: C.forestGreen }, line: { color: C.forestGreen }
      });
      s.addText(r.num, {
        x: x + 0.15, y: y + 0.16, w: 0.46, h: 0.46,
        fontSize: 11, bold: true, color: C.white, fontFace: "Calibri",
        align: "center", valign: "middle", margin: 0
      });
      s.addText(r.title, {
        x: x + 0.74, y: y + 0.18, w: rW - 0.88, h: 0.4,
        fontSize: 11, bold: true, color: C.forestGreen, fontFace: "Calibri", margin: 0
      });
      s.addText(r.body, {
        x: x + 0.15, y: y + 0.72, w: rW - 0.25, h: 0.96,
        fontSize: 9.5, color: C.charcoal, fontFace: "Calibri", margin: 0
      });
    });
  }

  // ──────────────────────────────────────────────────────────────────────────
  // SLIDE 11 — CONCLUSION
  // FIX: bullet text width trimmed to avoid leaf overlap; footer brighter;
  //      leaf moved right and smaller so it doesn't obscure content
  // ──────────────────────────────────────────────────────────────────────────
  {
    const s = pres.addSlide();
    s.background = { color: C.darkGreen };

    // Leaf — FIX: moved further right, smaller, so content area is clear
    s.addImage({ data: iLeaf, x: 7.2, y: 0.8, w: 3.2, h: 3.2, transparency: 80 });

    s.addShape("ellipse", {
      x: -1.5, y: 3.2, w: 4, h: 4,
      fill: { color: C.forestGreen, transparency: 65 },
      line: { color: C.forestGreen, transparency: 65 }
    });

    s.addText("Conclusion", {
      x: 0.6, y: 0.45, w: 6, h: 0.5,
      fontSize: 14, color: C.lightMoss, fontFace: "Calibri",
      charSpacing: 3, margin: 0
    });

    s.addText("Arogya Mitra", {
      x: 0.6, y: 1.0, w: 7.5, h: 0.95,
      fontSize: 48, bold: true, color: C.white, fontFace: "Georgia", margin: 0
    });

    s.addText("demonstrates how modern mobile technology can preserve and democratise\nindigenous medicinal plant knowledge for the next generation.", {
      x: 0.6, y: 2.0, w: 6.8, h: 0.75,
      fontSize: 14, color: "B8D8B9", fontFace: "Calibri", margin: 0
    });

    const bullets = [
      "On-device AI leaf identification (31 species, 85–95% accuracy)",
      "Firebase-powered community drug database with validation pipeline",
      "Multi-language support (English + Assamese) for North-East India",
      "Complete subscription & admin system — 19 screens, 43 source files",
    ];
    bullets.forEach((b, i) => {
      s.addImage({ data: iCheck, x: 0.6, y: 2.95 + i * 0.42, w: 0.25, h: 0.25 });
      // FIX: width trimmed from 7.4 → 6.4 to stay clear of leaf watermark
      s.addText(b, {
        x: 1.0, y: 2.94 + i * 0.42, w: 6.4, h: 0.32,
        fontSize: 11.5, color: C.white, fontFace: "Calibri", margin: 0
      });
    });

    // FIX: footer raised contrast from "5A8A5B" to "AEDBA0"
    s.addText("Thank You  •  Vibhu et al.  •  April 2026", {
      x: 0.6, y: 5.0, w: 8.8, h: 0.28,
      fontSize: 10, color: "AEDBA0", fontFace: "Calibri", margin: 0
    });
  }

  // ── Write ──────────────────────────────────────────────────────────────────
  const outPath = "/Users/adityaraj0421/Vibhu/indegeneousMedicine/ArogyaMitra_Presentation.pptx";
  await pres.writeFile({ fileName: outPath });
  console.log("✅  Saved:", outPath);
}

main().catch(err => { console.error("❌", err); process.exit(1); });
