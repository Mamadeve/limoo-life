# Limoo Life — Android

Repository for the Limoo Life Android app.

## Build an APK

After the Android project source files (including `gradlew`, `gradle/`, `app/`, and the Gradle build files) are committed to `main`:

1. Open the **Actions** tab.
2. Select **Build Android APK**.
3. Click **Run workflow**, or push a new commit.
4. Open the successful run and download the `limoo-life-debug-apk` artifact.

The debug APK is for testing and can be installed on an Android device. A release APK for distribution requires a properly configured release signing key.

> Do not commit API keys, signing keys, passwords, or private environment files.
