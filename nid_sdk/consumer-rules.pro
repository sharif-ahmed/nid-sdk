# CITL NID SDK Consumer Rules
# These rules are applied to the app that consumes this SDK

# Retrofit 2 rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature, InnerClasses

# GSON rules
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.**Unsafe { *; }

# CITL NID SDK Models - Keep all request/response models in their sub-packages
-keep class com.commlink.nid_sdk_demo.model.** { *; }
-keep class com.commlink.nid_sdk_demo.core.** { *; }

# Keep Callback interface
-keep class com.commlink.citl_nid_sdk.core.NIDCallback { *; }

# Keep BuildConfig for API settings (if needed by consumer)
-keep class com.commlink.nid_sdk_demo.BuildConfig.* { *; }
