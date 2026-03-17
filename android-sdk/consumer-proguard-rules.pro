# Keep Jackson annotations and model classes for ABSmartly SDK
-keepattributes *Annotation*

# Keep all classes in the json package
-keep class com.absmartly.sdk.json.** { *; }

# Keep Jackson serialization
-keep class com.fasterxml.jackson.** { *; }
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.* <fields>;
    @com.fasterxml.jackson.annotation.* <methods>;
}
