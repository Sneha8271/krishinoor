# 🌿 Krishinoor — AI Plant Disease Detection Android App

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-purple?logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2026+-green?logo=android)](https://developer.android.com)
[![PyTorch Mobile](https://img.shields.io/badge/PyTorch%20Mobile-1.13.1-red?logo=pytorch)](https://pytorch.org/mobile)
[![License](https://img.shields.io/badge/License-Educational-lightgrey)](LICENSE)
[![ML Model](https://img.shields.io/badge/ML%20Model-krishinoor--ml--model-blue)](https://github.com/Sneha8271/krishinoor-ml-model)

> **Krishinoor** is an offline AI-powered Android application for real-time plant disease detection.  
> Built using **Kotlin**, **PyTorch Mobile**, and **Google Location Services** — designed for smart farming, precision agriculture, and UAV-assisted crop monitoring.

---

## 📱 App Screenshots

| Login & Language | Dashboard | Scan Plant |
|:---:|:---:|:---:|
| Multilingual login with Hindi, English, Bengali, Urdu support | Real-time scan stats with GPS tracking | Camera + Gallery input with UAV drone support |

| Healthy Detection | Disease Detection | Scan History |
|:---:|:---:|:---:|
| Grape — Healthy (80.1%) | Pepper — Bacterial Spot (100%) | GPS-tagged scan history with confidence |

---

## ✨ Key Features

| Feature | Description |
|---|---|
| 🤖 **Offline AI Inference** | MobileNetV2 model runs fully on-device — no internet required |
| 🌍 **Multilingual UI** | Supports English, Hindi (हिन्दी), Bengali (বাংলা), Urdu (اردو) |
| 📍 **GPS Tagging** | Every scan is tagged with precise GPS coordinates and grid reference |
| 📡 **UAV/Drone Support** | Reads GPS from drone photo EXIF metadata automatically |
| 📊 **Scan Dashboard** | Real-time stats — total scans, diseased vs healthy plant count |
| 🕐 **Scan History** | Complete history with plant name, disease, confidence, location, timestamp |
| 🔔 **Push Notifications** | Instant alert when disease is detected |
| 💊 **Treatment Database** | Offline remedy + severity info for all 38 disease classes |
| 📷 **Camera + Gallery** | Supports both live camera capture and gallery image upload |
| 🔒 **Secure Login** | Phone number + password authentication with register flow |

---

## 🎯 App Flow

```
Splash Screen
    ↓
Login / Register (Phone + Password)
    ↓
Dashboard (Scan stats + Last scan preview)
    ↓
Scan Plant Screen (Camera / Gallery)
    ↓
AI Inference (PyTorch Mobile — offline)
    ↓
Analysis Result
  ├── Plant Name
  ├── Disease / Healthy status
  ├── Confidence score
  ├── Severity (High / Medium / Low / None)
  ├── Treatment & Solution
  ├── GPS Location + Grid coordinate
  └── Timestamp
    ↓
Scan History (all previous scans)
```

---

## 🧠 AI Model

The app uses a fine-tuned **MobileNetV2** model exported as **TorchScript Lite (.ptl)** for on-device inference.

| Property | Value |
|---|---|
| Architecture | MobileNetV2 |
| Classes | 38 disease categories |
| Plants Supported | 14 species |
| Model Size | ~9.3 MB |
| Inference Time | ~200ms on mid-range Android |
| Format | TorchScript Lite (.ptl) |
| Internet Required | ❌ Fully Offline |

🔗 Full ML pipeline: [krishinoor-ml-model](https://github.com/Sneha8271/krishinoor-ml-model)

### Image Preprocessing Pipeline (Android)
```kotlin
Bitmap → ARGB_8888 conversion
→ Resize (smaller side = 256px)
→ Center Crop (224 × 224)
→ Normalize (ImageNet mean/std)
→ TorchScript Lite Inference
→ Argmax → Class Label
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| ML Runtime | PyTorch Mobile (LiteModuleLoader) |
| Location | Google Play Services (FusedLocationProvider) |
| Camera | AndroidX Camera2 + FileProvider |
| UI | Material Design 3, ViewBinding |
| Storage | SharedPreferences (scan history) |
| Notifications | NotificationManager (Android 13+) |
| Image EXIF | AndroidX ExifInterface |
| Build System | Gradle (Kotlin DSL) |
| Min SDK | Android 8.0 (API 26) |
| Target SDK | Android 14 (API 34) |

---

## 📋 Permissions Required

| Permission | Purpose |
|---|---|
| `CAMERA` | Capture leaf images for analysis |
| `ACCESS_FINE_LOCATION` | GPS tagging of scan location |
| `ACCESS_COARSE_LOCATION` | Fallback location |
| `INTERNET` | Optional future cloud sync |
| `POST_NOTIFICATIONS` | Disease detection alerts |

---

## 🗂️ Project Structure

```
app/src/main/
├── java/com/plantdoctor/krishinoor/
│   ├── SplashActivity.kt          # Launch screen
│   ├── LoginActivity.kt           # Phone + password login
│   ├── RegisterActivity.kt        # New user registration
│   ├── MainActivity.kt            # Dashboard with scan stats
│   ├── ScanActivity.kt            # Camera/gallery + AI inference
│   ├── ResultActivity.kt          # Analysis result display
│   ├── HistoryActivity.kt         # Scan history list
│   ├── PlantDiseaseClassifier.kt  # PyTorch Mobile inference engine
│   └── DiseaseDatabase.kt         # Offline remedy + severity data
├── assets/
│   ├── model_mobile.ptl           # TorchScript Lite model (~9.3MB)
│   └── classes.json               # 38-class label mapping
└── res/
    ├── layout/                    # XML UI layouts
    ├── values/                    # Strings (multilingual)
    └── xml/                       # FileProvider paths
```

---

## 🚀 Build & Run

### Prerequisites
- Android Studio Hedgehog or later
- Android device / emulator (API 26+)
- JDK 11+

### Steps

```bash
# Clone the repository
git clone https://github.com/Sneha8271/krishinoor.git
cd krishinoor

# Open in Android Studio
# File → Open → select krishinoor folder

# Build and run
# Click ▶️ Run or press Shift+F10
```

> The model (`model_mobile.ptl`) and class labels (`classes.json`) are bundled in `app/src/main/assets/` — no additional setup needed.

---

## 📊 Sample Inference Results

| Plant | Result | Confidence |
|---|---|---|
| 🍇 Grape | Black Rot — Diseased | 100.0% |
| 🍇 Grape | Esca (Black Measles) | 97.6% |
| 🍇 Grape | Healthy | 99.4% |
| 🫑 Pepper | Bacterial Spot | 100.0% |
| 🍅 Tomato | Early Blight | 85.0% |
| 🍎 Apple | Cedar Apple Rust | 78.4% |

---

## 🌾 Real-World Use Cases

- 🌱 Early disease detection before visible crop damage
- 📵 Offline diagnosis for farmers in low-connectivity rural areas
- 🚜 Precision agriculture — targeted treatment, reduced pesticide use
- 🛸 UAV/drone image analysis via automatic EXIF GPS extraction
- 📱 Accessible smartphone-based screening for small-scale farmers
- 🌍 Multilingual support for regional farmer accessibility

---

## 🔮 Future Improvements

- [ ] Firebase authentication for cross-device sync
- [ ] Cloud-based scan history and farm analytics dashboard
- [ ] Grad-CAM heatmap overlay — show which leaf region triggered detection
- [ ] Model upgrade to EfficientNet-B3 for improved real-world accuracy
- [ ] Offline map integration for field-level disease spread tracking
- [ ] Support for additional plant species beyond current 14
- [ ] Edge AI optimization for UAV onboard inference

---

## 👩‍💻 Author

**Sneha**
B.Tech — Electronics and Computer Science (ECS)
Project: **Krishinoor** — Smart Farming · UAV · AI Platform

🔗 ML Model Repository: [krishinoor-ml-model](https://github.com/Sneha8271/krishinoor-ml-model)

---

## 📄 License

This project is for educational and research purposes.
