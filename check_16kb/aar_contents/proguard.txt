# Retrofit 2 rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature, InnerClasses

# GSON rules
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.**Unsafe { *; }

# CITL NID SDK Models - Keep all request/response models
-keep class com.commlink.citl_nid_sdk.model.** { *; }
-keep class com.commlink.citl_nid_sdk.network.** { *; }
-keep class com.commlink.citl_nid_sdk.utils.CallbackHolder { *; }
-keep class com.commlink.citl_nid_sdk.ui.NidInfoActivity { *; }

# Keep BuildConfig for API settings
-keep class com.commlink.citl_nid_sdk.BuildConfig { *; }

# External Libraries
-keep class org.tensorflow.** { *; }
-keep class com.google.mlkit.** { *; }
