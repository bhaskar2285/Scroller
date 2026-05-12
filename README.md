# ScrollBot

AI-powered Android app that scrolls Lazada, YouTube, and Instagram for you and surfaces the best results using an on-device vision model (moondream2).

## Features

- Floating search bar overlay — always visible, tap to search
- Scrolls Lazada, YouTube, Instagram like a human using Accessibility Service
- On-device vision model (moondream2 INT4, ~1.1GB) reads screen content
- Full-screen ranked results with clickable deep links
- 🔥 Trending chips from Google Trends (no API key needed)
- Lazada affiliate links — earn commission on purchases
- Freemium: 5 free scans/day, unlimited with Pro

## Device Requirements

- Android 10+ (API 29)
- arm64-v8a (64-bit)
- ~8GB RAM recommended
- ~2GB free storage for model

## Build Requirements

1. Android Studio with NDK installed
2. Clone llama.cpp into `app/src/main/jni/`:
   ```bash
   cd app/src/main/jni
   git clone --depth=1 https://github.com/ggerganov/llama.cpp.git
   ```
3. Build with `./gradlew assembleDebug`

## First Run

1. Grant overlay permission
2. Enable ScrollBot in Accessibility Settings
3. Allow screen capture (MediaProjection)
4. Download moondream2 model (~1.1GB, WiFi recommended)
5. Floating 🔍 button appears — tap to search

## Monetization

- Lazada purchases through ScrollBot earn affiliate commission
- Pro subscription: unlimited scans via RevenueCat
