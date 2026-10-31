<div align="center">

  # 🌌 STELLARIS — LungCare AI 🫁✨
  ### *Next-Gen AI-Powered Lung Health & Early Detection Companion for Android*

  [![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![Android Studio](https://img.shields.io/badge/IDE-Android%20Studio-3DDC84.svg?style=for-the-badge&logo=androidstudio&logoColor=white)](https://developer.android.com/studio)
  [![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28.svg?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/)
  [![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVI%2FMVVM-00599C.svg?style=for-the-badge)](https://developer.android.com/topic/architecture)
  [![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](LICENSE)

  <br/>

  <p align="center">
    <b>STELLARIS</b> is a futuristic, medical-grade Android application designed to provide intelligent early detection insights, symptom tracking, interactive AI assistant guidance, and smart chest X-ray vision analysis wrapped in a stunning cosmic glassmorphic UI.
  </p>

  ---

  <p align="center">
    <a href="#key-features">Key Features</a> •
    <a href="#tech-stack">Tech Stack</a> •
    <a href="#architecture">Architecture</a> •
    <a href="#getting-started">Getting Started</a> •
    <a href="#ui-design-tokens">Design System</a>
  </p>

</div>

---

## 🌟 Overview

> ⚠️ **Medical Disclaimer**: STELLARIS is an educational and supportive health companion. It does not replace professional clinical evaluation or definitive diagnostic testing.

STELLARIS merges modern AI capabilities with human-centric medical design. Whether you are seeking early risk evaluation, tracking daily respiratory health, or navigating a care journey, STELLARIS delivers actionable, clear, and reassuring insights.

---

<a name="key-features"></a>
## 🚀 Key Features

<table>
  <tr>
    <td width="50%">
      <h3>🤖 AI Health Assistant</h3>
      <ul>
        <li>Interactive, context-aware health assistant for answering lung care queries.</li>
        <li>Instant response streaming & prompt recommendations.</li>
        <li>Safety-first escalation logic for emergency symptoms.</li>
      </ul>
    </td>
    <td width="50%">
      <h3>🩻 Chest X-Ray Vision Lab</h3>
      <ul>
        <li>Secure upload & AI-driven educational chest scan analysis.</li>
        <li>Real-time image quality validation (glare, resolution, cropping checks).</li>
        <li>Detailed educational findings ready to share with doctors.</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>📊 Intelligent Risk Assessment</h3>
      <ul>
        <li>Interactive multi-step evaluation wizard (smoking history, symptoms, environment).</li>
        <li>Automatic pack-year calculations & risk summary generator.</li>
        <li>PDF export & summary sharing directly with medical providers.</li>
      </ul>
    </td>
    <td width="50%">
      <h3>📈 Symptom & Side-Effect Tracker</h3>
      <ul>
        <li>Comprehensive daily log for cough, dyspnea, fatigue, and side effects.</li>
        <li>Interactive severity charts & progression trends.</li>
        <li>Emergency alert banner for red-flag symptoms.</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>🏥 Personal Care Journey & Timeline</h3>
      <ul>
        <li>Tailored dashboards based on user journey state (Screening / Evaluation / Treatment).</li>
        <li>Appointment manager, medication reminders, and treatment schedule.</li>
        <li>Doctor Questions helper for appointment readiness.</li>
      </ul>
    </td>
    <td width="50%">
      <h3>🌌 Stellar Glassmorphism UI</h3>
      <ul>
        <li>Cinematic particle engine & cosmic animated onboarding.</li>
        <li>Full Dark & Light mode support with glow effects.</li>
        <li>Native Arabic (RTL) & English (LTR) localization out of the box.</li>
      </ul>
    </td>
  </tr>
</table>

---

<a name="tech-stack"></a>
## 🛠️ Tech Stack & Libraries

* **Core & Language:**
  * 🎯 **[Kotlin](https://kotlinlang.org/)** — 100% Native Kotlin codebase.
  * ⚡ **Coroutines & Flow** — Asynchronous programming & state streams.

* **UI & Animation:**
  * 🎨 **[Jetpack Compose](https://developer.android.com/jetpack/compose)** — Declarative UI framework.
  * 🎬 **Lottie Compose** — Interactive animations.
  * 🌈 **Material Design 3 & Custom Glassmorphic System** — Modern futuristic UI tokens.
  * 🧭 **Navigation Compose** — Type-safe routing system.

* **Architecture & Data:**
  * 🏛️ **Clean Architecture + MVVM** — Separation of concerns (`presentation`, `data`).
  * 🔒 **Firebase Authentication & Google Sign-In** — Secure user identity management.
  * 💾 **DataStore & Room** — Local persistence, drafts, and offline caching.

---

<a name="architecture"></a>
## 📐 Architecture & Project Structure

```text
H:/STELLARIS/app/src/main/java/com/example/myapplication/
│
├── 📂 data/                    # Data Layer
│   ├── auth/                   # Authentication repositories (Firebase & Local)
│   ├── local/                  # DataStore & local preferences
│   ├── model/                  # Data models (HealthRecords, UserProfile, etc.)
│   └── repository/             # Concrete repository implementations
│
├── 📂 presentation/            # UI Layer (Jetpack Compose)
│   ├── assistant/              # AI Assistant chat screens & viewmodels
│   ├── auth/                   # Login, SignUp, Google Auth, components
│   ├── care/                   # Patient care journey & medical treatment
│   ├── components/             # Reusable UI components (ShootingStar, GlowBottomBar, etc.)
│   ├── health/                 # Health summary & metrics
│   ├── intro/                  # Cinematic particle intro & onboarding flow
│   ├── main/                   # Main container & navigation host
│   ├── myhealth/               # My Health hub & risk history
│   ├── navigation/             # NavGraph & typed routes
│   ├── onboarding/             # Interactive walkthrough screens
│   ├── records/                # Medical document management
│   ├── risk/                   # Multi-step risk assessment flow
│   ├── splash/                 # Particle engine splash screen
│   ├── symptoms/               # Symptom logger & trend analyzer
│   ├── theme/ & ui/theme/      # Stellar colors, dynamic typography, shape design
│   └── vision/                 # X-ray Vision Lab & image upload flow
│
└── 📄 MainActivity.kt          # Single Activity Entry Point
```

---

<a name="getting-started"></a>
## ⚡ Getting Started

### Prerequisites

* **Android Studio:** Ladybug (2024.2.1) or newer.
* **JDK:** Java 17+.
* **Android Device / Emulator:** API Level 26 (Android 8.0) or higher.

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Moaz221/STELLARIS.git
   cd STELLARIS
   ```

2. **Open in Android Studio:**
   * Open Android Studio -> `File` -> `Open` -> Select `STELLARIS` folder.

3. **Configure Google Services:**
   * Place your `google-services.json` inside the `app/` folder.

4. **Sync & Run:**
   * Click **Gradle Sync** 🐘.
   * Run on your device/emulator by pressing `Shift + F10` or clicking **Run** ▶️.

---

<a name="ui-design-tokens"></a>
## 🎨 UI & Design Tokens

| Token | Description | Color Preview |
| :--- | :--- | :--- |
| **Deep Space Background** | Dark background tone for stellar vibe | `#0B0E14` |
| **Neon Cyan Accent** | Primary interactive elements & active states | `#00F5FF` |
| **Cosmic Purple** | Secondary glow & gradient accent | `#7B2CBF` |
| **Glass Surface** | Translucent frosted glass effect | `RGBA(255, 255, 255, 0.08)` |

---

## 🤝 Contributing & License

Contributions, issues, and feature requests are welcome!

Distributed under the **MIT License**. See `LICENSE` for more information.

---

<div align="center">
  <sub>Built with ❤️ and ✨ for better healthcare innovation.</sub>
</div>
