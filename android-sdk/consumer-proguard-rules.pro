# Keep Jackson annotations and model classes for ABSmartly SDK
-keepattributes *Annotation*

# Keep all PublishEvent and ContextData fields
-keepclassmembers class com.absmartly.sdk.json.PublishEvent {
    *;
}

-keepclassmembers class com.absmartly.sdk.json.ContextData {
    *;
}

-keepclassmembers class com.absmartly.sdk.json.Experiment {
    *;
}

# Keep all classes in the json package
-keep class com.absmartly.sdk.json.** { *; }

# Keep Jackson serialization
-keep class com.fasterxml.jackson.** { *; }
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.* <fields>;
    @com.fasterxml.jackson.annotation.* <methods>;
}

# Keep custom exceptions
-keep class com.absmartly.android.sdk.cache.SqliteAndroidLocalCache$CacheOperationException { *; }
-keep class com.absmartly.android.sdk.cache.SqliteAndroidLocalCache$CacheSerializationException { *; }
