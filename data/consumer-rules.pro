# Preserve annotations and type signatures for reflection
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

# Retrofit & Kotlin Coroutines (prevents R8 full mode from stripping Continuation generic type)
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Retrofit service interfaces
-keep interface com.radiothing.data.api.** { *; }
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# Gson & DTOs
-keep class com.google.gson.** { *; }
-keep class com.radiothing.data.api.dto.** { *; }
-keepclassmembers class com.radiothing.data.api.dto.** { <fields>; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Room
-keep class com.radiothing.data.db.entity.** { *; }
