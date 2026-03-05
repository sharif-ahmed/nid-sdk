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
-keep class citl_nid_sdk.**Request { *; }
-keep class citl_nid_sdk.**Response { *; }
-keep class citl_nid_sdk.**Response$* { *; }
-keep class citl_nid_sdk.Result { *; }
-keep class citl_nid_sdk.**NidDocumentParser.NidData { *; }

# Keep BuildConfig for API settings
-keep class citl_nid_sdk.**BuildConfig { *; }