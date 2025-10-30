# A/B Smartly Android SDK

A/B Smartly - Android SDK

## Architecture

The A/B Smartly Android SDK is a **thin wrapper** around the [A/B Smartly Java SDK (core-api v1.6.0)](https://github.com/absmartly/java-sdk). All core functionality—including experiment evaluation, variant assignment, context management, event tracking, and HTTP client operations—is delegated to the Java SDK.

The Android SDK provides only Android-specific components:
- **SqliteAndroidLocalCache**: An Android-optimized local cache implementation using SQLite for persisting context data and publish events
- Android-specific dependency management and configuration

For all core functionality, testing, and API documentation, please refer to the [Java SDK repository](https://github.com/absmartly/java-sdk).

## Compatibility

The A/B Smartly Android SDK is compatible with Android 5 and later (API level 21+).

The `android.permission.INTERNET` permission is required. To add this permission to your application ensure the following line is present in the `AndroidManifest.xml` file:
```xml
    <uses-permission android:name="android.permission.INTERNET"/>
```

If you target Android 6.0 or earlier, a few extra steps are outlined below for installation and initialization.

## Installation

#### Gradle

To install the ABSmartly Android SDK, place the following in your `build.gradle` and replace {VERSION} with the latest Android SDK version available in MavenCentral.

```gradle
dependencies {
  implementation 'com.absmartly.sdk:android-sdk:{VERSION}'
}
```

#### Android 6.0 or earlier
When targeting Android 6.0 or earlier, the default Java Security Provider will not work. Using [Conscrypt](https://github.com/google/conscrypt) is recommended. Follow these [instructions](https://github.com/google/conscrypt/blob/master/README.md) to install it as dependency.

## Usage

The Android SDK follows the same API as the [A/B Smartly Java SDK](https://github.com/absmartly/java-sdk). Please refer to the Java SDK documentation for complete usage instructions, including:
- SDK initialization and configuration
- Creating and managing contexts
- Evaluating treatments and running experiments
- Tracking events and goals
- Publishing events

### Android-Specific Components

#### SqliteAndroidLocalCache

The Android SDK provides an Android-optimized implementation of the `LocalCache` interface using SQLite. This is recommended for Android applications as it provides efficient persistence of context data and publish events.

**Usage:**
```java
import com.absmartly.android.sdk.cache.SqliteAndroidLocalCache;

// Initialize the cache with your Android context
LocalCache cache = new SqliteAndroidLocalCache(context);

// Use the cache when configuring the SDK
SDKConfig config = SDKConfig.create()
    .setLocalCache(cache)
    // ... other configuration
    .build();
```

See [SqliteAndroidLocalCache.java](https://github.com/absmartly/android-sdk/blob/main/android-sdk/src/main/java/com/absmartly/android/sdk/cache/SqliteAndroidLocalCache.java) for implementation details.

#### Alternative: Memory Cache

The `MemoryCache` implementation provided by the Java SDK is also compatible with Android applications if you prefer an in-memory cache instead of SQLite persistence. 

## About A/B Smartly
**A/B Smartly** is the leading provider of state-of-the-art, on-premises, full-stack experimentation platforms for engineering and product teams that want to confidently deploy features as fast as they can develop them.
A/B Smartly's real-time analytics helps engineering and product teams ensure that new features will improve the customer experience without breaking or degrading performance and/or business metrics.

### Have a look at our growing list of clients and SDKs:
- [Java SDK](https://www.github.com/absmartly/java-sdk)
- [JavaScript SDK](https://www.github.com/absmartly/javascript-sdk)
- [PHP SDK](https://www.github.com/absmartly/php-sdk)
- [Swift SDK](https://www.github.com/absmartly/swift-sdk)
- [Vue2 SDK](https://www.github.com/absmartly/vue2-sdk)
- [Android SDK](https://www.github.com/absmartly/android-sdk)
