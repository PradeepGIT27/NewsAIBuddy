# NewsAIBuddy 📰🤖

**NewsAIBuddy** is an intelligent, modern Android application built with **Jetpack Compose** that combines real-time news aggregation with on-device and cloud-based **AI chat & article summarization**. Powering next-generation news consumption, NewsAIBuddy allows users to read top headlines, get instant AI summaries of lengthy articles, and converse with an AI chatbot about current events.

---

## 🌟 Key Features

### 📰 Real-Time News Aggregation
- **Category Feeds**: Browse curated news across categories including Technology, Business, Sports, Entertainment, Health, Science, and World News.
- **Global Search**: Instantly search millions of global news articles by keywords.
- **Country Filtering**: Tailor headlines by region (US, India, UK, Canada, Australia, etc.).
- **Article Details & In-App Reader**: Read full article summaries, view original publisher details, and open source links directly in-app.

### 🤖 Dual AI Chatbot & On-Device LLM (LiteRT-LM & Gemini)
- **On-Device LLM Engine (LiteRT-LM)**: Run lightweight open-weights models (Gemma, Qwen, Llama) directly on the user's device via `com.google.ai.edge.litertlm` with GPU/CPU acceleration.
- **Cloud Gemini Integration**: Seamless fallback to Google's Gemini AI for deep contextual analysis.
- **Article Context Integration**: Tap "Discuss with AI" on any article to automatically send its headline and content into the chatbot for instant Q&A and deeper analysis.

### 🧠 Smart AI Article Summarizer
- Generate quick, bullet-point AI summaries of long articles to capture key takeaways in seconds.

### 🔖 Saved Articles & Offline Reading
- Bookmark favorite articles to read offline anytime.
- Persistent local storage using Jetpack DataStore.

### 🔐 User Authentication & Session Management
- **Google Sign-In**: Integrated with Android Credential Manager API & Google ID.
- **Guest Mode**: Instant one-tap access without mandatory account creation.
- Persistent session management across app restarts.

### 🎨 Material 3 & Modern UI
- 100% **Jetpack Compose** single-activity architecture.
- Support for Dynamic Colors, Dark Theme, and fluid transitions.

---

## 🛠️ Technology Stack

| Layer | Technologies Used |
| :--- | :--- |
| **Language** | Kotlin 100% |
| **UI Framework** | Jetpack Compose, Material 3, Compose Navigation |
| **Architecture** | MVVM (Model-View-ViewModel) + Clean Architecture |
| **Dependency Injection** | Hilt (Dagger-Hilt) + KSP |
| **On-Device AI Engine** | Google LiteRT-LM (`com.google.ai.edge.litertlm`) |
| **Cloud AI** | Google Generative AI (Gemini SDK) |
| **Networking** | Retrofit 2, OkHttp 4, Gson, Moshi |
| **Image Loading** | Coil Compose |
| **Storage / Persistence** | Jetpack DataStore Preferences |
| **Authentication** | Credential Manager API, Google ID |
| **Async / Streams** | Kotlin Coroutines & StateFlow |

---

## 📁 Architecture Overview

NewsAIBuddy follows Google's recommended Android app architecture guidelines based on **Clean Architecture**:

```text
com.support.tech.newsaibuddy
├── data/
│   ├── api/                 # Retrofit API interface & response states
│   ├── datasource/          # Remote & Local DataSources
│   ├── entity/              # Data entities & ChatBot models
│   ├── litertlm/            # LiteRT-LM engine, model manager & service
│   ├── local/               # DataStore & SavedArticles manager
│   ├── remote/              # DTOs, mappers & API interceptors
│   └── repository/          # Repository implementations
├── di/                      # Hilt DI Modules (Network, Local, AI, Repository)
├── domain/
│   ├── model/               # Domain data models (Article, NewsCategory)
│   ├── repository/          # Repository interfaces
│   └── usecase/             # Business use cases (GetNewsByCategory, GetNewsByCountry)
└── ui/
    ├── components/          # Reusable Compose UI components & cards
    ├── navigation/          # Compose Navigation Graph
    ├── screens/             # UI Screens (News, ChatBot, Login, Profile, Reference)
    ├── theme/               # Material 3 Color palette, Typography & Themes
    └── viewmodel/           # ViewModels & UI State wrappers
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug / 2024.2+ or Android Studio Jellyfish+
- **JDK**: Java 11 or Java 17
- **Android Device / Emulator**: Running Android 7.0 (API Level 24) or higher (API Level 31+ recommended for LiteRT GPU acceleration).

### Setup & API Keys

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/NewsAIBuddy.git
   cd NewsAIBuddy
   ```

2. **Configure API Keys**:
   Create a `local.properties` file in the root directory (if not present) and add your API keys:
   ```properties
   newsKey="YOUR_NEWS_API_KEY"
   botKey="YOUR_GEMINI_AI_API_KEY"
   ```
   *Get a free News API key from [NewsAPI.org](https://newsapi.org/) and a Gemini API key from [Google AI Studio](https://aistudio.google.com/).*

3. **Build & Run**:
   Open the project in Android Studio and run on your device or emulator:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 📱 App Screens

- **News Feed Screen**: Real-time articles with category filters, search bar, and country selection.
- **AI Chatbot Screen**: Interactive AI chat supporting prompt queries and article context discussion.
- **Login / Guest Screen**: Google Sign-In & Guest authentication options.
- **Profile & Settings Screen**: Manage user profile, saved articles, theme preferences, and LiteRT AI model settings.
- **Reference Screen**: Overview of third-party libraries, APIs, and tech stack details.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
