# CITL NID SDK Integration Guide

This guide describes how to integrate the **CITL NID SDK** into an Android application as an AAR library.

## 1. Prerequisites
- Min SDK Version: 24
- Target SDK Version: 36
- Kotlin/Java version compatible with Android 13+ (Java 17 recommended)

## 2. Include the SDK AAR
Copy the `citl-nid-sdk-release.aar` (or `nid_sdk-release.aar`) to your project's `libs` folder.

In your app-level `build.gradle` (or [build.gradle.kts](file:///D:/StudyLearnProjects/AndroidProjects/OfficeProjects/CITL_NID_SDK/build.gradle.kts)), add the following:

```kotlin
dependencies {
    implementation(files("libs/citl-nid-sdk-release.aar"))
    
    // Required Transitive Dependencies
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.camera:camera-core:1.3.3")
    implementation("androidx.camera:camera-camera2:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    implementation("androidx.camera:camera-view:1.3.3")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.1")
    implementation("com.google.mlkit:face-detection:16.1.7")
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("com.github.yalantis:ucrop:2.2.11")
    implementation("com.google.mlkit:text-recognition-devanagari:16.0.1")
}
```

## 3. Configure AndroidManifest.xml
The SDK handles most of its permissions, but ensure your app has the following permissions if not automatically merged:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 4. Initialization and Usage

To start the NID verification process, call `NIDEnterpriseSDK.startVerification`:

```java
import com.commlink.citl_nid_sdk.NIDEnterpriseSDK;
import com.commlink.citl_nid_sdk.core.NIDCallback;
import com.commlink.citl_nid_sdk.model.NIDError;
import com.commlink.citl_nid_sdk.model.NIDInfo;

// ... Inside an Activity ...

String apiKey = "YOUR_API_KEY";

NIDEnterpriseSDK.startVerification(this, apiKey, new NIDCallback() {
    @Override
    public void onSuccess(NIDInfo nidInfo) {
        // Verification successful
        // Use nidInfo to access extracted data (e.g., nidInfo.getName(), nidInfo.getNidNumber())
    }

    @Override
    public void onError(NIDError error) {
        // Handle error (e.g., error.getMessage())
    }
});
```

## 5. Building the AAR
To generate the AAR file yourself:
1. Open the project in Android Studio.
2. Run `./gradlew :nid_sdk:assembleRelease`.
3. The AAR will be located at `nid_sdk/build/outputs/aar/citl-nid-sdk-release.aar`.
