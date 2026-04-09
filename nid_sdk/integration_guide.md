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
        // Verification was successful and face matched!
        // Access extracted and verified data:
        String name = nidInfo.getName();
        String nid = nidInfo.getNidNumber();
        // data included in nidInfo.getOcrData(), nidInfo.getEcValidation(), etc.
    }

    @Override
    public void onError(NIDError error) {
        // Handle failure or cancellation
        switch (error.getCode()) {
            case USER_CANCELLED:
                // User explicitly exited the SDK (e.g., back button or close button)
                Log.d("SDK", "User cancelled the process");
                break;
            case FACE_MATCH_FAILED:
                // Verification completed but the face didn't match the NID photo
                Log.e("SDK", "Face mismatch error code: " + error.getCustomErrorCode());
                break;
            case NETWORK_ERROR:
                // API timeout or connectivity issue
                Log.e("SDK", "Network error: " + error.getMessage());
                break;
            case EMPTY_DATA_ERROR:
                // API returned success but data was null/empty (E106)
                Log.e("SDK", "Empty data error: " + error.getCustomErrorCode());
                break;
            default:
                // Other errors (Camera, OCR, etc.)
                Log.e("SDK", "Error: [" + error.getCode() + "] " + error.getMessage());
                break;
        }
    }
});
```

## 5. Error Reference

The `NIDError` object contains a `Code` enum and optionally a `customErrorCode` string for specific API failures.

### When `onError` Is Triggered

The SDK will return an `onError` callback to your host application in the following scenarios:

1.  **User Manual Exit**: Triggered if the user clicks the "Back" button and confirms they want to exit the verification process.
2.  **Face Verification Failure**: Triggered if the final face matching process completes but determines the faces do not match.
3.  **API Failures/Timeouts**: Triggered if the NID EC verification or the Face Match API fails due to network issues, server errors, or timeouts.
4.  **Empty Data from API**: Triggered if the API returns a success status (200 OK) but the internal data payload is empty (E106).
5.  **Hardware/Permission Issues**: Triggered if the user denies camera permissions or if the camera hardware fails to initialize.
6.  **Liveness Verification Failure**: Triggered if the user is unable to successfully complete the required liveness actions (blink, smile, turn).
7.  **OCR Data Extraction Failure**: Triggered if the SDK cannot extract any readable text from the provided NID image.

## 6. Understanding the `NIDError` Object

The `onError(NIDError error)` callback provides a rich error object with several helper methods to help your app decide how to react:

| Method | Return Type | Description |
|:---|:---|:---|
| `getCode()` | `NIDError.Code` | Returns the high-level enum categorizing the error (e.g., `USER_CANCELLED`, `NETWORK_ERROR`). |
| `getCustomErrorCode()` | `String` | Returns a specific string code (e.g., `E104`, `E106`) for precise API failure identification. |
| `getMessage()` | `String` | Returns a human-readable description of the error. |
| `getCause()` | `Throwable` | Returns the underlying exception (if any) that triggered the error (e.g., a `Retrofit` or `Network` exception). |

### Example implementation:
```java
@Override
public void onError(NIDError error) {
    if (error.getCode() == NIDError.Code.EMPTY_DATA_ERROR) {
        // Specifically handle empty response with code E106
        String detail = error.getCustomErrorCode(); // Returns "E106"
        showResult("System encountered an empty response. Please try again.");
    }
}
```

## 7. SDK Exit Flow for Clients
| `USER_CANCELLED` | - | Any | Fired when user selects 'Yes' on the exit confirmation dialog. |
| `FACE_MATCH_FAILED` | `E104` | `ResultActivity` / `VerificationSummaryActivity` | Fired when the face verification process completes but faces do not match. |
| `NETWORK_ERROR` | `E102` | `NidInfoActivity` | Fired when the EC (Election Commission) verification API fails or times out. |
| `NETWORK_ERROR` | `E105` | `VerificationSummaryActivity` | Fired when the final Face Match API fails or times out. |
| `OCR_ERROR` | `E100` | `NidInfoActivity` | Fired if the NID card text extraction (OCR) fails completely. |
| `LIVENESS_FAILED` | `E103` | `SelfieActivity` | Fired if the user fails liveness detection actions (blink, turn, etc.). |
| `CAMERA_ERROR` | - | `SelfieActivity` / `CaptureNIDActivity` | Fired on hardware camera failure or permission denial. |
| `UNKNOWN` | `E500` | Any | Fired on unexpected internal catch-all exceptions. |

## 6. SDK Exit Flow for Clients

The SDK is designed to be self-cleaning. When a terminal state is reached (Success or Error), the SDK will:
1.  Invoke the appropriate method on your `NIDCallback`.
2.  Clear and release its internal callback reference safely (`null`).
3.  Finish all SDK activities and return control to your host application.

### Handling "User Cancelled"
When the user attempts to exit (Back Button), a confirmation dialog appears. If they confirm:
- Your `onError` will be called with `Code.USER_CANCELLED`.
- You should use this callback to reset your UI or navigate the user back as needed.

## 7. Building the AAR
To generate the AAR file yourself:
1. Open the project in Android Studio.
2. Run `./gradlew :nid_sdk:assembleRelease`.
3. The AAR will be located at `nid_sdk/build/outputs/aar/citl-nid-sdk-release.aar`.
