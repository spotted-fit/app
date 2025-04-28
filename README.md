# ⚫ Spotted: A Social Network for Fitness Enthusiasts

## 🚀 Idea
A **modern** and **engaging** social network designed for fitness enthusiasts! **Share** your active lifestyle with friends!
- 📸 **Capture moments** of your fitness journey, share achievements, and challenge friends.
- Inspired by: 
  - 📱 **Instagram:** Photo sharing and social interactions.
  - 🏆 **Strava:** Activity tracking and friend connections.
  - 🔥 **BeReal:** Authentic, in-the-moment content sharing.

---

## 🤔 The Problem
Maintaining fitness motivation can be challenging without social support and recognition.
- Many people struggle to stay consistent with their fitness routines due to lack of **accountability**, **social engagement**, and **visual progress tracking**.

---

## 🎯 Key Features
- **Activity Photo Sharing:** Capture and share photos of your fitness activities in real-time.
- **Social Engagement:** React to friends' posts with emojis and comments to provide encouragement.
- **Challenge System:** Send challenges to friends based on specific activity types.
- **Visual Progress Tracking:** View statistics and progress over time through an intuitive interface.
- **Cross-Platform Sharing:** Create compilations of your fitness journey to share on other social platforms.

---

## 📚 Learning Value
- 🛠️ **Frontend:** Modern mobile development with cross-platform capabilities
- 🔗 **Backend:** Scalable API development with secure data handling
- 🗂️ **Database:** Efficient storage and retrieval of user data and media
- 🔐 **Authentication:** Secure multi-platform login options
- 📊 **Analytics:** Implementation of user activity tracking and statistics generation

---

## ⚠️ Potential Problems
📱 **User Engagement:** Maintaining active user participation and content creation. \
🔒 **Privacy Concerns:** Balancing social sharing with user privacy and data protection. \
🌐 **Content Moderation:** Ensuring appropriate content while maintaining a positive community. \
⚡ **Performance:** Handling media uploads and processing efficiently across different devices and network conditions. \

---

## 🎉 Conclusion
A **social**, **motivating**, and **visually engaging** platform for fitness enthusiasts! 💪
- Transforming fitness activities into shareable moments that inspire and connect people.

---

## 🌐 API Configuration

The application supports configuring the API base URL through environment variables:

### Environment Variables

#### `LOCAL` (boolean)
- Controls whether to use localhost IPs for API connections
- Default: `true` (if not set)
- When `true`:
  - Android: Uses `http://10.0.2.2:8080` (special IP that maps to host machine's localhost for Android emulators)
  - iOS: Uses `http://localhost:8080`
  - WASM/JS: Uses `http://localhost:8080`

#### `BASE_URL` (string)
- Custom base URL for API connections when `LOCAL` is set to `false`
- No default value
- If not set when `LOCAL=false`, falls back to the platform-specific localhost URL

### Platform-Specific Configuration

#### Android
Environment variables can be set through:
- System properties: `System.setProperty("local", "false")`
- Environment variables: `LOCAL=false` and `BASE_URL=https://api.example.com`

#### iOS
Environment variables are set through:
- NSProcessInfo environment: Set `LOCAL` and `BASE_URL` in the app's environment

#### WASM/JS
Configuration is done through URL parameters:
- `?local=false&baseUrl=https://api.example.com`

### Example Usage

```bash
# Android/JVM
export LOCAL=false
export BASE_URL=https://api.spotted.fit

# iOS (in Xcode scheme)
LOCAL=false
BASE_URL=https://api.spotted.fit

# WASM/JS (in browser URL)
https://app.spotted.fit/?local=false&baseUrl=https://api.spotted.fit
```

## 🐳 Docker for WASM

### Prerequisites
- Docker

### Usage
To build and serve the WASM application, run:

```bash
docker build -t spotted-app .
docker run -p 80:80 spotted-app
```

This will:
1. Build the WASM application using Gradle inside a Docker container
2. Serve the WASM application on http://localhost:8080

### How it works
The Dockerfile uses a multi-stage build approach:

1. **Build stage**: Uses Gradle to generate the WASM artifacts
2. **Serve stage**: Uses Nginx to serve the generated WASM artifacts

The WASM artifacts are generated in `composeApp/build/dist/wasmJs/productionExecutable` and served from there.

### Development
For development, you can run the Gradle task directly:

```bash
./gradlew wasmJsBrowserDistribution
```

And then serve the artifacts using any web server, for example:

```bash
cd composeApp/build/dist/wasmJs/productionExecutable
python -m http.server 8080
```
