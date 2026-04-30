# 🌿 Arogya Mitra — Indigenous Medicine Portal

> An Android application that bridges **traditional indigenous plant knowledge** with modern AI-powered plant identification, built for rural healthcare accessibility in South and South-East Asia.

---

## 📱 Screenshots

| Splash Screen | Home | Leaf Prediction | Result |
|---|---|---|---|
| *(green gradient with leaf+ECG icon)* | *(plant database grid)* | *(camera / gallery toggle)* | *(AI analysis card)* |

---

## ✨ Features

### 🔬 Dual-Mode Plant Identification
| Mode | Technology | Connectivity |
|---|---|---|
| **AI (Online)** | NVIDIA Nemotron VL via OpenRouter | Requires internet |
| **Offline ML** | TensorFlow Lite MobileNet | No internet needed |

- Switch between modes at runtime with a single tap — persists across restarts
- 30 medicinal plant species supported by the offline model
- AI mode auto-falls back between NVIDIA models if one is rate-limited

### 🌱 Plant Database
- Firebase Realtime Database with full plant catalogue
- Search by plant name or medicinal use
- Tap any prediction result to instantly search drug records

### 🔐 Authentication
- Google Sign-In via Firebase Auth
- Persistent login state across app restarts

### 🎨 UI / UX
- Custom leaf + ECG heartbeat app icon (adaptive, API 26+)
- Animated splash screen with fade + scale entrance
- Material Design 3 cards, pill buttons, segmented toggle
- Multilingual support via `LanguageManager`

---

## 🏗️ Architecture

```
app/
├── java/com/example/indegenousmedicine2/
│   ├── LeafIdentifier.java          # Strategy interface
│   ├── GeminiLeafIdentifier.java    # Google Gemini REST (native format)
│   ├── OpenRouterLeafIdentifier.java# OpenRouter / NVIDIA (OpenAI format)
│   ├── LeafClassifier.java          # TFLite offline inference
│   ├── PredictionResult.java        # Value object (label + confidence)
│   ├── LeafPredictionActivity.java  # Camera → identify → show result
│   ├── HelloActivity.java           # Animated splash screen
│   ├── HomeDummy.java               # Main home with plant grid
│   ├── LoginActivity.java           # Google Sign-In
│   ├── DrugListActivity.java        # Firebase plant/drug search
│   └── LanguageManager.java         # Runtime locale switching
│
├── res/
│   ├── drawable/
│   │   ├── ic_launcher_foreground.xml  # Leaf + ECG vector icon
│   │   ├── ic_launcher_background.xml  # Dark green adaptive background
│   │   ├── btn_pill_primary.xml        # Filled green pill button
│   │   ├── btn_pill_outline.xml        # Outlined pill button
│   │   ├── toggle_track_bg.xml         # Model toggle track
│   │   ├── toggle_selected_bg.xml      # Active toggle state
│   │   └── leaf_scan_placeholder.xml   # Dashed image drop zone
│   ├── layout/
│   │   ├── activity_hello.xml          # Splash screen
│   │   ├── activity_leaf_prediction.xml# Prediction screen
│   │   ├── activity_home_dummy.xml     # Home grid
│   │   └── activity_login.xml          # Sign-in screen
│   └── assets/
│       ├── leaf_classifier.tflite      # 18 MB MobileNet model
│       └── labels.json                 # 30 plant class names
```

### Design Pattern — Strategy
```
LeafIdentifier (interface)
    ├── OpenRouterLeafIdentifier  ← AI button (NVIDIA Nemotron, free)
    ├── GeminiLeafIdentifier      ← fallback (Google Gemini native API)
    └── LeafClassifier            ← Offline ML button (TFLite on-device)
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Platform | Android (Java), minSdk 27, targetSdk 34 |
| Build | Gradle 8.2, Kotlin DSL |
| AI (Online) | OpenRouter API → NVIDIA Nemotron VL (free tier) |
| AI (Fallback) | Google Gemini 2.0 Flash REST API |
| ML (Offline) | TensorFlow Lite 2.16.1 + MobileNet |
| Backend | Firebase Realtime Database, Firebase Auth, Firebase Storage |
| Networking | OkHttp 4.12.0 |
| Image Loading | Glide 4.16.0 |
| UI | Material Design 3, ViewBinding, Navigation Component |

---

## ⚡ Setup

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34

### 1. Clone
```bash
git clone https://github.com/Adityaraj0421/ArogyaMitra.git
cd ArogyaMitra
```

### 2. API Keys
Create `local.properties` in the project root (already gitignored):
```properties
sdk.dir=/path/to/your/android/sdk

# OpenRouter (free) — https://openrouter.ai/keys
OPENROUTER_API_KEY=sk-or-v1-...

# Google Gemini (fallback) — https://aistudio.google.com
GEMINI_API_KEY=AIza...
```

### 3. Firebase
- Place your `google-services.json` in `app/` (already gitignored)
- Enable **Authentication → Google Sign-In** in Firebase console
- Enable **Realtime Database** with your plant data

### 4. Build
```bash
./gradlew assembleDebug
# APK → app/build/outputs/apk/debug/app-debug.apk
```

---

## 🤖 AI Models

### OpenRouter (Primary)
The app uses [OpenRouter](https://openrouter.ai) to access free NVIDIA vision models:

| Model | Type | Vision |
|---|---|---|
| `nvidia/nemotron-nano-12b-v2-vl:free` | Vision-Language | ✅ |
| `nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free` | Reasoning + Vision | ✅ |

Both models are **free**, hosted by NVIDIA, and handle base64 image input through the OpenAI-compatible chat completions API.

### Offline TFLite Model
- **Architecture**: MobileNet (custom trained)
- **Input**: 150×150 RGB, normalised to [0, 1]
- **Output**: Softmax over 30 plant classes
- **Size**: 18 MB (bundled as asset, not compressed)

**Supported plants (30 species):**
Alpinia Galanga, Amaranthus Viridis, Artocarpus Heterophyllus, Azadirachta Indica (Neem), Basella Alba, Brassica Juncea, Carissa Carandas, Citrus Limon, Ficus Auriculata, Ficus Religiosa, Hibiscus Rosa-sinensis, Jasminum, Mangifera Indica, Mentha, Moringa Oleifera, Muntingia Calabura, Murraya Koenigii, Nerium Oleander, Nyctanthes Arbor-tristis, Ocimum Tenuiflorum (Tulsi), Piper Betle, Plectranthus Amboinicus, Pongamia Pinnata, Psidium Guajava, Punica Granatum, Santalum Album, Syzygium Cumini, Syzygium Jambos, Tabernaemontana Divaricata, Trigonella Foenum-graecum

---

## 📂 Project Report

See [`PROJECT_REPORT.md`](PROJECT_REPORT.md) for the full academic submission covering database design, ML pipeline, prediction accuracy, and Firebase backend.

---

## 🔒 Security Notes

- `local.properties` is gitignored — **never commit API keys**
- `google-services.json` is gitignored — **never commit Firebase config**
- Debug APK uses debug signing — generate a release keystore for production

---

## 🧑‍💻 Authors

- **Aditya Raj** — [Adityaraj0421](https://github.com/Adityaraj0421)
- **Vibhav** — Android development & AI integration

---

## 📄 License

This project is for academic and educational purposes.
