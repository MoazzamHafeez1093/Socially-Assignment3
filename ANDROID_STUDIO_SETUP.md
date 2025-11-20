# Android Studio Setup Guide

## Project Configuration Complete ✅

Your Android project is now fully configured for Android Studio. Here's what has been set up:

### 🔧 Configuration Files Created/Updated:

1. **Gradle Configuration**:
   - `gradle.properties` - Optimized with 4GB memory allocation and parallel builds
   - `gradle-wrapper.properties` - Using Gradle 8.13
   - `libs.versions.toml` - Android Gradle Plugin 8.12.2, Kotlin 2.0.21

2. **Android Studio IDE Files**:
   - `.idea/runConfigurations.xml` - Main app run configuration
   - `.idea/workspace.xml` - Workspace settings
   - `.idea/misc.xml` - JDK 11 configuration
   - `.idea/modules.xml` - Module definitions
   - `Assignment1.iml` - Project module file
   - `app/app.iml` - App module file

3. **Build Configuration**:
   - `local.properties` - SDK path set to macOS location
   - All image files corrected (proper extensions)
   - Class name conflicts resolved

## 🚀 How to Open in Android Studio:

### Method 1: Open Existing Project
1. Launch Android Studio
2. Click "Open" or "Open an existing Android Studio project"
3. Navigate to: `/Users/asadmehdi/Downloads/i221120_MuhammadAsadMehdi_Assignment_1/Project_Source`
4. Click "Open"

### Method 2: Import Project
1. Launch Android Studio
2. Click "Import Project (Gradle, Eclipse ADT, etc.)"
3. Select the `Project_Source` folder
4. Click "OK"

## 🎯 Running the App:

### Option 1: Using Android Studio
1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Click the green "Run" button (▶️) or press `Shift+F10`
4. Select your target device (emulator or physical device)

### Option 2: Using Command Line
```bash
cd /Users/asadmehdi/Downloads/i221120_MuhammadAsadMehdi_Assignment_1/Project_Source
./gradlew installDebug
```

## 📱 App Features:

This is a social media-style Android application with the following activities:
- **MainActivity** - Entry point
- **LoginActivity** - User authentication
- **HomeScreen** - Main dashboard
- **UserProfile** - User profile view
- **OwnProfile** - Personal profile management
- **Story Features** - Story upload, view, and highlights
- **Search** - User search functionality
- **Chat/Messaging** - Communication features
- **Notifications** - User notifications

## 🔍 Troubleshooting:

### If Gradle Sync Fails:
1. Go to `File > Sync Project with Gradle Files`
2. Check that Android SDK is properly installed
3. Verify JDK 11 is set as project SDK

### If Build Fails:
1. Clean project: `Build > Clean Project`
2. Rebuild project: `Build > Rebuild Project`
3. Check that all dependencies are resolved

### Device Connection Issues:
1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Accept the debugging prompt on your device

## 📋 System Requirements:

- **Android Studio**: Latest version (recommended)
- **JDK**: Version 11 or higher
- **Android SDK**: API Level 24+ (Android 7.0)
- **Target SDK**: API Level 36 (Android 14)
- **Gradle**: 8.13
- **Kotlin**: 2.0.21

## 🎉 You're Ready to Go!

The project is now fully configured and ready to run in Android Studio. Simply open the project and hit the Run button to see your social media app in action!
