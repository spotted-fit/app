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

The application supports two environment modes for API configuration:

### Debug/Development Mode
In debug/development mode, the application connects to a local backend server:
- Android: `http://10.0.2.2:8080` (special IP that maps to host machine's localhost for Android emulators)
- iOS: `http://localhost:8080`
- WASM/JS: `http://localhost:8080`

### Production Mode
In production mode, the application gets the backend URL from environment variables:
- Environment variable name: `BACKEND_URL`
- Default fallback URL if not set: `https://api.spotted.fit`

### Setting the Environment Mode
You can set the environment mode during application initialization:

```kotlin
// For Android
EnvironmentConfig.setEnvironment(Environment.PROD) // or Environment.DEBUG

// For iOS
EnvironmentConfig.setEnvironment(Environment.PROD) // or Environment.DEBUG

// For WASM/JS
EnvironmentConfig.setEnvironment(Environment.PROD) // or Environment.DEBUG
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
