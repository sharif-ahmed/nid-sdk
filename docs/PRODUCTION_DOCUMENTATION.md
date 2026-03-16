# CITL NID Enterprise SDK - Developer Documentation (v1.0.0)

Welcome to the **CITL NID Enterprise SDK**. This SDK provides a robust, production-ready solution for identity verification and data extraction from Bangladesh National ID (NID) cards. It utilizes advanced OCR and Face Verification technology to automate the e-KYC process.

---

## 🌟 Key Features
- **Smart Data Extraction:** Automatic recognition of Smart and Old NID card formats.
- **Fuzzy OCR Correction:** High-accuracy Bangla text extraction with automated error correction.
- **Face Verification:** Integrated face match technology between NID photo and live selfie.
- **Liveness Detection:** Security checks to ensure the user is present in real-time.
- **Modular Design:** Lightweight AAR integration with comprehensive callback support.

---

## 📋 Technical Requirements
- **Development Environment:** Android Studio Jellyfish or later.
- **Minimum OS:** Android 7.0 (API Level 24) or higher.
- **Target SDK:** API Level 36.
- **Programming Languages:** Java 17+ or Kotlin 1.9+.

---

## 📦 1. Installation

### A. Including the AAR
1. Download the `citl-nid-sdk-v1.0.0-YYYYMMDD-release.aar` file.
2. Place it in your application module's `libs` directory.

### B. Gradle Configuration
Since the SDK is provided as a local binary, you **must** manually declare the required transitive dependencies in your `build.gradle.kts` (or `build.gradle`):

```kotlin
dependencies {
    // 1. Local NID SDK Binary
    implementation(files("libs/citl-nid-sdk-v1.0.0-YYYYMMDD-release.aar"))
    
    // 2. Transitive Dependencies (Required for OCR, Camera, and Network)
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.camera:camera-core:1.3.3")
    implementation("androidx.camera:camera-camera2:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    implementation("androidx.camera:camera-view:1.3.3")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.1")
    implementation("com.google.mlkit:text-recognition-devanagari:16.0.1")
    implementation("com.google.mlkit:face-detection:16.1.7")
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("com.github.yalantis:ucrop:2.2.11")
}
```

---

## ⚙️ 2. Configuration

### Manifest Permissions
The SDK handles internal permissions. Ensure your app does not block the following if you have custom restriction policies:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## 🚀 3. Usage & Implementation

### Initializing the Verification Flow
The entry point for the SDK is the `NIDEnterpriseSDK.startVerification` method.

#### Java Code
```java
import com.commlink.citl_nid_sdk.NIDEnterpriseSDK;
import com.commlink.citl_nid_sdk.core.NIDCallback;
import com.commlink.citl_nid_sdk.model.NIDInfo;
import com.commlink.citl_nid_sdk.model.NIDError;

public void startKycProcess() {
    String apiKey = "YOUR_API_KEY_HERE";

    NIDEnterpriseSDK.startVerification(this, apiKey, new NIDCallback() {
        @Override
        public void onSuccess(NIDInfo nidInfo) {
            // Extraction successful
            String name = nidInfo.getName();
            String nidNumber = nidInfo.getNidNumber();
            String dob = nidInfo.getDateOfBirth();
            
            // Proceed with your business logic
        }

        @Override
        public void onError(NIDError error) {
            // Handle specific error codes
            showToast("Error: " + error.getMessage());
        }
    });
}
```

#### Kotlin Code
```kotlin
import com.commlink.citl_nid_sdk.NIDEnterpriseSDK
import com.commlink.citl_nid_sdk.core.NIDCallback
import com.commlink.citl_nid_sdk.model.NIDInfo
import com.commlink.citl_nid_sdk.model.NIDError

fun startKycProcess() {
    val apiKey = "YOUR_API_KEY_HERE"

    NIDEnterpriseSDK.startVerification(this, apiKey, object : NIDCallback {
        override fun onSuccess(nidInfo: NIDInfo) {
            // Handle success
            val name = nidInfo.name
        }

        override fun onError(error: NIDError) {
            // Handle error
        }
    })
}
```

---

## 📊 4. API Reference

### Key Classes

| Class | Description |
| :--- | :--- |
| `NIDEnterpriseSDK` | Main interface to launch the SDK flow. |
| `NIDInfo` | Data model containing all extracted NID fields. |
| `NIDCallback` | Interface for receiving extraction results or errors. |
| `NIDError` | Object containing error code and human-readable message. |

### Error Codes

| Code | Meaning |
| :--- | :--- |
| `E100` | OCR Failed |
| `E101` | NID / DOB Mismatch |
| `E102` | EC API Timeout |
| `E103` | Liveliness Failed |
| `E104` | Face Match Failed |
| `E105` | Face Match API Timeout |
| `E500` | Unexpected SDK Error |

---

## 🛡️ 5. Security & Best Practices

> [!TIP]
> **Proguard Optimization:** If your app uses Proguard or R8, the SDK includes internal rules. However, we recommend adding this to your `proguard-rules.pro` to avoid accidental stripping:
> ```proguard
> -keep class com.commlink.citl_nid_sdk.** { *; }
> ```

> [!IMPORTANT]
> **Duplicate Class Prevention:** If your application already uses libraries like `Retrofit` or `ML Kit`, ensure that the versions in your `build.gradle` match or are newer than the ones defined in this guide.

---

## 📧 Support
For integration assistance, bug reports, or license renewal, please reach out:

**Technical Support:** support@commlink.com.bd  
**Official Site:** [www.commlink.com.bd](https://www.commlink.com.bd)  
**Location:** Dhaka, Bangladesh

---
*© 2026 Commlink Info Tech Ltd. All rights reserved.*
