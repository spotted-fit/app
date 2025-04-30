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

## 📱 Android APK Downloads

### Latest Release
The latest Android APK is always available at a constant URL:
```
https://github.com/super-duper-gym/mobile-app/releases/download/latest/composeApp-release.apk
```

## 🌐 API Configuration

The application supports two environment modes for API configuration:

### Debug/Development Mode
In debug/development mode, the application connects to a local backend server:
- Android: `http://10.0.2.2:8080` (special IP that maps to host machine's localhost for Android emulators)
- iOS: `http://localhost:8080`
- WASM/JS: Uses a relative URL (`""`) to avoid CORS issues. This means the backend should be served from the same origin as the WASM application.

### Production Mode
In production mode, the application gets the backend URL from environment variable:
- Environment variable name: `BASE_URL`
- This is configured in the build.gradle.kts file using BuildKonfig

#### GitHub Actions Configuration
For GitHub Actions workflow, the `BASE_URL` can be set in two ways:
- As a GitHub Variable (recommended): Set a repository or organization variable named `BASE_URL`
- As a GitHub Secret (legacy): Set a repository or organization secret named `BASE_URL`

If both are set, the GitHub Variable takes precedence. This allows for more flexibility and visibility in your CI/CD pipeline.

## 🐳 Docker for WASM

### Prerequisites
- Docker

### Usage
To build and serve the WASM application, you have two options:

#### Option 1: Using build arguments
```bash
# Build the Docker image with a specific BASE_URL
docker build -t --build-arg BASE=URL=https://your-api-url.com spotted-app .

# Run the Docker container
docker run -p 80:80 spotted-app
```

#### Option 2: Using a .env file
You can also use a .env file to provide the BASE_URL:

1. Create a .env file in the project root with the following content:
```
BASE_URL=https://your-api-url.com
```

2. Build and run the Docker image without specifying the build argument:
```bash
# Build the Docker image (will use BASE_URL from .env file)
docker build -t spotted-app .

# Run the Docker container
docker run -p 80:80 spotted-app
```

Note: If both a .env file exists and the BASE_URL build argument is provided, the build argument takes precedence.

This will:
1. Build the WASM application using Gradle inside a Docker container
2. Serve the WASM application on http://localhost:80

### How it works
The Dockerfile uses a multi-stage build approach:

1. **Build stage**: Uses Gradle to generate the WASM artifacts
2. **Serve stage**: Uses Nginx to serve the generated WASM artifacts

The WASM artifacts are generated in `composeApp/build/dist/wasmJs/developmentExecutable` and served from there.

### Development
For development, you can run the Gradle task directly with the BASE_URL environment variable:

```bash
# Set the BASE_URL environment variable
export BASE_URL=http://localhost:8080

# Run the Gradle task
./gradlew wasmJsBrowserDevelopmentExecutableDistribution
```

And then serve the artifacts using any web server, for example:

```bash
cd composeApp/build/dist/wasmJs/developmentExecutable
python3 -m http.server 80
```

Note: Make sure to set the BASE_URL environment variable before running the Gradle task, as it's required by the build.gradle.kts file.

#### Avoiding CORS Issues in WASM/JS Development
To avoid CORS issues when developing the WASM/JS application, you have two options:

1. **Serve the backend and frontend from the same origin:**
   - Configure your backend to serve the WASM application's static files
   - Or use a reverse proxy (like Nginx) to route requests to both the backend and frontend

2. **Enable CORS on your backend server:**
   - Configure your backend server to include the appropriate CORS headers:
     ```
     Access-Control-Allow-Origin: *
     Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
     Access-Control-Allow-Headers: Content-Type, Authorization
     ```
   - In this case, you'll need to set the BASE_URL environment variable to the absolute URL of your backend server
