# CITL NID SDK - Integration Guide (v1.0.0)

This document provides professional guidance on integrating the **CITL NID SDK** into your Android application. The SDK provides robust character recognition and identity validation features for Bangladesh NID cards.

---

## 🚀 1. Quick Start

### Prerequisites
- **Android Studio:** Jellyfish | 2023.3.1 or higher
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 36
- **Build System:** Gradle (Kotlin or Groovy DSL)

> [!IMPORTANT]
> Ensure your project is configured with **Java 17** compatibility in `build.gradle`.

---

## 📦 2. Library Installation

### Step 1: Add the AAR
Copy `citl-nid-sdk-release.aar` to your app-level `/libs` directory.

### Step 2: Configure Dependencies
In your app-level `build.gradle.kts` (or `build.gradle`), include the following required transitive dependencies:

```kotlin
dependencies {
    // SDK Binary
    implementation(files("libs/citl-nid-sdk-release.aar"))
    
    // Core Android Libraries
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    
    // UI & Layout
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    
    // Camera & OCR (Machine Learning)
    implementation("androidx.camera:camera-core:1.3.3")
    implementation("androidx.camera:camera-camera2:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    implementation("androidx.camera:camera-view:1.3.3")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.1")
    implementation("com.google.mlkit:text-recognition-devanagari:16.0.1")
    implementation("com.google.mlkit:face-detection:16.1.7")
    
    // Data & Networking
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")
    implementation("androidx.room:room-runtime:2.8.4")
    
    // Image Processing
    implementation("com.github.yalantis:ucrop:2.2.11")
}
```

---

## 🛠️ 3. SDK Configuration

### Permissions
The SDK includes the necessary permissions in its manifest. However, ensure your application handles runtime permission requests for the **Camera** if you trigger the SDK flow.

### Manifest Merging
The following activities are automatically merged into your `AndroidManifest.xml`. No manual activity declaration is required unless you need to override themes.

---

## 💻 4. Implementation

To launch the NID verification flow, use the `NIDEnterpriseSDK` entry point.

### Java Implementation
```java
String apiKey = "YOUR_PROVIDED_API_KEY";

NIDEnterpriseSDK.startVerification(this, apiKey, new NIDCallback() {
    @Override
    public void onSuccess(NIDInfo nidInfo) {
        // Logic for successful verification
        String name = nidInfo.getName();
        String nidNumber = nidInfo.getNidNumber();
        android.util.Log.d("SDK_SUCCESS", "Verified: " + name);
    }

    @Override
    public void onError(NIDError error) {
        // Logic for error handling
        android.util.Log.e("SDK_ERROR", "Error: " + error.getMessage());
    }
});
```

### Kotlin Implementation
```kotlin
val apiKey = "YOUR_PROVIDED_API_KEY"

NIDEnterpriseSDK.startVerification(this, apiKey, object : NIDCallback {
    override fun onSuccess(nidInfo: NIDInfo) {
        // Handle success
    }

    override fun onError(error: NIDError) {
        // Handle error
    }
})
```

---

## ⚠️ 5. Best Practices & Troubleshooting

> [!TIP]
> **Proguard/R8:** If you use code shrinking, the SDK includes its own Proguard rules. If you face issues, ensure `-keep` rules for `com.commlink.citl_nid_sdk.**` are checked.

> [!WARNING]
> **Duplicate Class Error:** If you encounter `BuildConfig` conflicts, ensure your App's package name (namespace) is unique and does not match `com.commlink.citl_nid_sdk`.

---

## 📧 Support
For technical support or license inquiries, contact:
**Support Team:** support@commlink.com.bd
**Website:** [www.commlink.com.bd](https://www.commlink.com.bd)
