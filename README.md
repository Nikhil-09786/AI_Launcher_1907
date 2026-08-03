# Bhai OS - Minimalist AI Operating System Mode Launcher

**Bhai OS** (AI Launcher) is a minimalist, distraction-free Android Home Screen Launcher built with **Kotlin** and **Jetpack Compose**. Designed with an OLED pitch-black aesthetic (`#000000`) for maximum power saving, it turns your smartphone into an AI-first launcher with **Google Gemini 1.5 Flash API** and **Native System Tools (Function Calling)**.

---

## 🌟 Key Features

- **Strict Pitch-Black OLED Design (`#000000`)**: Saves battery on OLED screens with high-contrast pure white typography.
- **Real-Time System Metrics**: Displays live clock, formatted date, and real-time battery status with charging indicators.
- **Google Gemini 1.5 Flash AI Engine**: Answers general queries directly on your home screen in minimalist text cards.
- **Native Android System Actions (Function Calling)**:
  - 📞 **Make Calls**: *"Call Mom"* $\rightarrow$ Opens system dialer with contact/number.
  - 💬 **Send SMS**: *"Send SMS to 9876543210"* $\rightarrow$ Opens SMS composer.
  - ⏰ **Set Alarm**: *"Set alarm for 7 AM"* $\rightarrow$ Sets native Android alarm clock.
  - 🚀 **Launch Apps**: *"Open WhatsApp"* or *"Open YouTube"* $\rightarrow$ Launches installed applications.
- **Single-Task Home Launcher Setup**: Configured with `CATEGORY_HOME` and `CATEGORY_DEFAULT` intent filters.

---

## 🛠️ Built With

- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose (Material 3)
- **AI Engine**: Google Generative AI Kotlin SDK (`com.google.ai.client.generativeai`)
- **Min SDK**: API 26 (Android 8.0)
- **Target SDK**: API 35 (Android 15)

---

## 🚀 How to Run Locally

1. Clone this repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/ai-launcher.git
   ```
2. Open the project in **Android Studio**.
3. Insert your Gemini API Key in `GeminiManager.kt`.
4. Connect your Android phone via USB debugging and click **Run ▶** (`Shift + F10`).
