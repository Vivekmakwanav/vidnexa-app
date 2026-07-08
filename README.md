# Vidnexa Android Mobile Client

Vidnexa is a premium, high-performance, and world-class native Android video downloader and manager client designed with **Jetpack Compose**, **Kotlin**, and **Material 3**. 

This application strictly acts as a client that communicates with your **FastAPI Backend Service**. All media downloads, platform-specific parsing, and heavy stream processing are offloaded to the server side as requested, keeping the Android app lightweight, secure, and fully compliant with device policies.

---

## 🎨 Visual Aesthetics & Premium Theme

Vidnexa features a sleek, modern, and immersive visual design built on dynamicMaterial 3 values:
- **Cosmic Space Theme**: Rich dark gradients (Cosmic Black, Slate Dark) decorated with ambient canvas brushes.
- **Glassmorphic Cues**: Semi-transparent, layered surface elevations (`tonalElevation`) coupled with polished rounded cards.
- **Micro-Animations**: Staggered list entries, infinite refresh rotations, and smoothly transitioning loading stage indicators.
- **Dynamic Adaptability**: Instantaneous runtime switching between Hand-Crafted Dark Mode and Sleek Light Mode.

---

## 🏗️ Clean Architecture & MVVM Structure

The project strictly follows **Clean Architecture**, the **Repository Pattern**, and **SOLID Design Principles** under a feature-first organization:

```
com.example/
│
├── data/
│   ├── api/
│   │   ├── ApiModels.kt         # Moshi-serializable DTO wrappers for FastAPI requests
│   │   ├── VidnexaApi.kt        # Retrofit endpoints with dynamic URL configurations
│   │   └── RetrofitClient.kt    # OkHttpClient instantiation with logging & timeout values
│   │
│   ├── database/
│   │   ├── AppDatabase.kt       # SQLite Room Database holder (auto fallback migration)
│   │   ├── DatabaseModels.kt    # History & Favorite entity schemas
│   │   └── Daos.kt              # Reactive flows and transactional SQL queries
│   │
│   ├── download/
│   │   └── DownloadManager.kt   # High-fidelity Coroutine Downloader (handles real streams & mock loops)
│   │
│   ├── model/
│   │   └── DomainModels.kt      # Domain representations decoupled from DTOs & DB Schemas (with Mappers)
│   │
│   ├── repository/
│   │   └── VidnexaRepository.kt # Central data repository abstraction (and fallback demo-mode generators)
│   │
│   └── utils/
│       ├── Resource.kt          # Sealed class wrappers representing Loading, Success, and Error states
│       └── SettingsManager.kt   # SharedPreferences manager storing server URL, dark theme, etc.
│
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt        # Modern URL paste, recent download cards, and interactive platforms list
│   │   ├── DownloadScreen.kt    # Stage-based loading extraction, resolution lists, format download triggers
│   │   ├── HistoryScreen.kt     # Log search and filters, native Open File (system players) & Share sheets
│   │   ├── FavoritesScreen.kt   # Bookmarked links with quick pre-loading on home input
│   │   ├── SettingsScreen.kt    # Customizable API endpoint, force dark mode switches, and licenses
│   │   └── HelpScreen.kt        # Interactive expandable FAQs and mail composer support actions
│   │
│   ├── theme/
│   │   ├── Color.kt             # Brand HEX colors (Neon Cyan, Royal Blue, Cyber Purple)
│   │   ├── Theme.kt             # Material Theme builder binding light/dark color schemes
│   │   └── Type.kt              # Custom typography presets
│   │
│   └── viewmodel/
│       └── VidnexaViewModel.kt  # Unified state container orchestrating UI changes, flows, and clicks
│
└── MainActivity.kt              # App entry point initializing dependencies and Scaffold bottom bar navigation
```

---

## ⚡ Key Technical Innovations

1. **Dynamic Backend Routing**: Retrofit utilizes the `@Url` annotation on methods. This allows users to type in any local or remote FastAPI base address in settings (e.g. `http://192.168.1.100:8000`), and the applet routes all calls to it immediately without requiring code changes!
2. **Double-Engine Download Manager**: The download system leverages Coroutines to stream actual network files over OkHttp. For demo and testing, it gracefully triggers a simulation sequence that mimics real-time bytes progress and saves mock files locally so share actions can be verified.
3. **Secure Local File Provider**: Configured with a native Android `FileProvider` so media logs can trigger external views safely without exposing private file system URIs.
4. **Interactive Tester Grids**: Tapping any logo (e.g., YouTube, TikTok) on the home screen automatically populates the input field with a pretty mock video URL from that platform, allowing immediate end-to-end testing.

---

## 🔗 FastAPI Backend Contract

Your FastAPI server should expose these standard JSON endpoints to bind flawlessly with the mobile client:

### 1. Retrieve Available Formats & Info
- **Endpoint**: `GET /api/info?url=<video_url>`
- **Response Format**:
```json
{
  "id": "unique_video_id",
  "title": "Introduction to Jetpack Compose",
  "description": "Learn the fundamentals of building premium Android UI.",
  "thumbnail": "https://example.com/thumbnails/comp.jpg",
  "duration": 184,
  "platform": "YouTube",
  "formats": [
    {
      "id": "format_1080p",
      "resolution": "1080p Full HD",
      "extension": "mp4",
      "file_size": 52428800,
      "url": "https://your-backend-storage.com/downloads/comp_1080p.mp4",
      "type": "video_with_audio"
    },
    {
      "id": "format_audio",
      "resolution": "High Quality Audio (320kbps)",
      "extension": "mp3",
      "file_size": 7340032,
      "url": "https://your-backend-storage.com/downloads/comp_audio.mp3",
      "type": "audio_only"
    }
  ]
}
```

### 2. Retrieve Supported Services
- **Endpoint**: `GET /api/services`
- **Response Format**:
```json
{
  "services": [
    { "name": "YouTube", "icon": "youtube", "url_pattern": "youtube.com|youtu.be" },
    { "name": "Instagram", "icon": "instagram", "url_pattern": "instagram.com" },
    { "name": "TikTok", "icon": "tiktok", "url_pattern": "tiktok.com" },
    { "name": "Facebook", "icon": "facebook", "url_pattern": "facebook.com" },
    { "name": "X (Twitter)", "icon": "twitter", "url_pattern": "twitter.com|x.com" }
  ]
}
```

---

## 🛠️ Testing & Compilation

### Local Verification
To execute local JVM and Robolectric testing suites:
```bash
gradle :app:testDebugUnitTest
```

### Build Debug APK
To compile and assemble a testable debug package:
```bash
gradle :app:assembleDebug
```
The resulting package will be compiled at `/app/build/outputs/apk/debug/app-debug.apk`.

### Build Release App Bundle (AAB)
To package the application for Play Store distribution:
```bash
gradle :app:bundleRelease
```
The resulting bundle will be compiled at `/app/build/outputs/bundle/release/app-release.aib`.
