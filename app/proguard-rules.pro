# Add project specific ProGuard rules here.

# Keep Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.radiothing.data.api.dto.** { *; }

# Keep Room
-keep class com.radiothing.data.db.entity.** { *; }

# Keep domain models
-keep class com.radiothing.domain.model.** { *; }
