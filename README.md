# ABsmartly Android SDK

A/B Smartly - Android SDK

## Architecture

The A/B Smartly Android SDK is a **thin wrapper** around the [A/B Smartly Java SDK](https://github.com/absmartly/java-sdk). All core functionality -- including experiment evaluation, variant assignment, context management, event tracking, and HTTP client operations -- is delegated to the Java SDK.

The Android SDK provides Android-specific components:
- **SqliteAndroidLocalCache**: An Android-optimized local cache implementation using SQLite for persisting context data and publish events
- Android-specific dependency management, ProGuard/R8 consumer rules, and configuration

For the full core API reference, please refer to the [Java SDK documentation](https://github.com/absmartly/java-sdk).

## Compatibility

The A/B Smartly Android SDK is compatible with Android 5.0 and later (API level 21+).

The `android.permission.INTERNET` permission is required. To add this permission to your application ensure the following line is present in the `AndroidManifest.xml` file:
```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

If you target Android 6.0 or earlier, a few extra steps are outlined below for installation and initialization.

## Installation

#### Gradle

To install the ABsmartly Android SDK, place the following in your `build.gradle` and replace `{VERSION}` with the latest SDK version available in MavenCentral.

```gradle
dependencies {
    implementation 'com.absmartly.sdk:android-sdk:{VERSION}'
}
```

#### Android 6.0 or earlier

When targeting Android 6.0 or earlier, the default Java Security Provider will not work. Using [Conscrypt](https://github.com/google/conscrypt) is recommended. Follow these [instructions](https://github.com/google/conscrypt/blob/master/README.md) to install it as a dependency.

#### ProGuard / R8 Rules

The Android SDK ships with consumer ProGuard rules that are automatically applied when building your release APK. These rules ensure the JSON model classes used for serialization/deserialization are preserved. If you encounter issues, verify the following rules are active:

```proguard
-keep class com.absmartly.sdk.json.** { *; }
-keep class com.fasterxml.jackson.** { *; }
```

## Getting Started

Please follow the [installation](#installation) instructions before trying the following code.

### Initialization

This example assumes an API Key, an Application, and an Environment have been created in the A/B Smartly web console.

#### Recommended: ABSmartlyAndroid Wrapper (Android Entry Point)

`ABSmartlyAndroid` is the recommended Android entry point. It wires the Java SDK and the `SqliteAndroidLocalCache` together in a single step, so you don't need to configure each component separately.

```java
import com.absmartly.android.sdk.ABSmartlyAndroid;

final ABSmartlyAndroid sdk = ABSmartlyAndroid.builder()
    .endpoint("https://your-company.absmartly.io/v1")
    .apiKey("YOUR-API-KEY")
    .application("android-app")
    .environment("production")
    .context(getApplicationContext())
    .build();
```

Or using the static factory method:

```java
final ABSmartlyAndroid sdk = ABSmartlyAndroid.create(
    "https://your-company.absmartly.io/v1",
    "YOUR-API-KEY",
    "android-app",
    "production",
    getApplicationContext()
);
```

`ABSmartlyAndroid` delegates `createContext`, `createContextWith`, and `close` to the underlying Java SDK and exposes `getCache()` to access the `SqliteAndroidLocalCache` directly. Call `getSdk()` to access the underlying `ABsmartly` Java SDK instance for advanced use cases.

#### Advanced Configuration (Java SDK directly)

For advanced use cases where you need full control over the Client and configuration:

```java
import com.absmartly.sdk.*;

final ClientConfig clientConfig = ClientConfig.create()
    .setEndpoint("https://your-company.absmartly.io/v1")
    .setAPIKey("YOUR-API-KEY")
    .setApplication("android-app")
    .setEnvironment("production");

final Client absmartlyClient = Client.create(clientConfig);

final ABsmartlyConfig sdkConfig = ABsmartlyConfig.create()
    .setClient(absmartlyClient);

final ABsmartly sdk = ABsmartly.create(sdkConfig);
```

#### Initializing with SqliteAndroidLocalCache manually

If you are using the Java SDK directly and want to wire the SQLite cache yourself:

```java
import com.absmartly.sdk.*;
import com.absmartly.android.sdk.cache.SqliteAndroidLocalCache;

SqliteAndroidLocalCache cache = new SqliteAndroidLocalCache(getApplicationContext());

final ClientConfig clientConfig = ClientConfig.create()
    .setEndpoint("https://your-company.absmartly.io/v1")
    .setAPIKey("YOUR-API-KEY")
    .setApplication("android-app")
    .setEnvironment("production");

final Client absmartlyClient = Client.create(clientConfig);

final ABsmartlyConfig sdkConfig = ABsmartlyConfig.create()
    .setClient(absmartlyClient);

final ABsmartly sdk = ABsmartly.create(sdkConfig);
```

#### Android 6.0 or earlier

When targeting Android 6.0 or earlier, set the default Java Security Provider for SSL to *Conscrypt* by creating the *Client* instance as follows:

```java
import com.absmartly.sdk.*;
import org.conscrypt.Conscrypt;

final ClientConfig clientConfig = ClientConfig.create()
    .setEndpoint("https://your-company.absmartly.io/v1")
    .setAPIKey("YOUR-API-KEY")
    .setApplication("android-app")
    .setEnvironment("production");

final DefaultHTTPClientConfig httpClientConfig = DefaultHTTPClientConfig.create()
    .setSecurityProvider(Conscrypt.newProvider());

final DefaultHTTPClient httpClient = DefaultHTTPClient.create(httpClientConfig);

final Client absmartlyClient = Client.create(clientConfig, httpClient);

final ABsmartlyConfig sdkConfig = ABsmartlyConfig.create()
    .setClient(absmartlyClient);

final ABsmartly sdk = ABsmartly.create(sdkConfig);
```

**SDK Options**

| Config                  | Type                              | Required? |   Default   | Description                                                                                                                                                                   |
| :---------------------- | :-------------------------------- | :-------: | :---------: | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| endpoint                | `String`                          |  &#9989;  | `null`      | The URL to your API endpoint. Most commonly `"https://your-company.absmartly.io/v1"`                                                                                          |
| apiKey                  | `String`                          |  &#9989;  | `null`      | Your API key which can be found on the Web Console.                                                                                                                           |
| environment             | `String`                          |  &#9989;  | `null`      | The environment of the platform where the SDK is installed. Environments are created on the Web Console and should match the available environments in your infrastructure.   |
| application             | `String`                          |  &#9989;  | `null`      | The name of the application where the SDK is installed. Applications are created on the Web Console and should match the applications where your experiments will be running. |
| timeout                 | `int`                             |  &#10060; | `3000`      | HTTP connection timeout in milliseconds                                                                                                                                        |
| retries                 | `int`                             |  &#10060; | `5`         | Maximum number of retry attempts for failed HTTP requests                                                                                                                      |
| contextEventLogger      | `ContextEventLogger`              |  &#10060; | `null`      | Callback to handle SDK events (ready, exposure, goal, etc.)                                                                                                                   |
| contextDataProvider     | `ContextDataProvider`             |  &#10060; | auto        | Custom provider for context data (advanced usage)                                                                                                                             |
| contextEventHandler     | `ContextEventHandler`             |  &#10060; | auto        | Custom handler for publishing events (advanced usage)                                                                                                                         |
| variableParser          | `VariableParser`                  |  &#10060; | auto        | Custom parser for experiment variables (advanced usage)                                                                                                                       |
| audienceDeserializer    | `AudienceDeserializer`            |  &#10060; | auto        | Custom deserializer for audience data (advanced usage)                                                                                                                        |

## Creating a New Context

### Synchronously

```java
final ContextConfig contextConfig = ContextConfig.create()
    .setUnit("device_id", "device-unique-id");

final Context context = sdk.createContext(contextConfig)
    .waitUntilReady();
```

### Asynchronously

On Android, context creation must happen off the main thread. Use the asynchronous API to avoid blocking the UI:

```java
final ContextConfig contextConfig = ContextConfig.create()
    .setUnit("device_id", "device-unique-id");

sdk.createContext(contextConfig)
    .waitUntilReadyAsync()
    .thenAccept(ctx -> {
        runOnUiThread(() -> {
            // safe to update UI here
        });
    });
```

### With Pre-fetched Data

Creating a context involves a round-trip to the A/B Smartly event collector. You can avoid repeating the round-trip by re-using data previously retrieved, for example from a server-side SDK:

```java
final ContextConfig contextConfig = ContextConfig.create()
    .setUnit("device_id", "device-unique-id");

final Context context = sdk.createContextWith(contextConfig, prefetchedContextData);
assert(context.isReady()); // no need to wait
```

### Refreshing the Context with Fresh Experiment Data

For long-running contexts, the context is usually created once when the application is first started. However, any experiments started after the context was created will not be triggered. To mitigate this, use `setRefreshInterval()` on the context config.

```java
final ContextConfig contextConfig = ContextConfig.create()
    .setUnit("device_id", "device-unique-id")
    .setRefreshInterval(TimeUnit.HOURS.toMillis(4)); // every 4 hours
```

Alternatively, call `refresh()` manually:

```java
context.refresh();
```

### Setting Extra Units

You can add additional units to a context by calling `setUnit()` or `setUnits()`. For example, when a user logs in to your application, you may want to add a user-level unit to the context. Note that **you cannot override an already set unit type** as that would be a change of identity, and will throw an exception. In this case, you must create a new context instead.

```java
context.setUnit("db_user_id", "1000013");

context.setUnits(Map.of(
    "db_user_id", "1000013"
));
```

## Basic Usage

### Selecting a Treatment

```java
if (context.getTreatment("exp_test_experiment") == 0) {
    // user is in control group (variant 0)
} else {
    // user is in treatment group
}
```

### Treatment Variables

```java
final Object variable = context.getVariable("my_variable");
```

### Peek at Treatment Variants

Although generally not recommended, it is sometimes necessary to peek at a treatment or variable without triggering an exposure. The SDK provides `peekTreatment()` for that purpose.

```java
if (context.peekTreatment("exp_test_experiment") == 0) {
    // user is in control group (variant 0)
} else {
    // user is in treatment group
}
```

#### Peeking at Variables

```java
final Object variable = context.peekVariable("my_variable");
```

### Overriding Treatment Variants

During development, it is useful to force a treatment for an experiment. This can be achieved with `setOverride()` and/or `setOverrides()`. These methods can be called before the context is ready.

```java
context.setOverride("exp_test_experiment", 1);

context.setOverrides(Map.of(
    "exp_test_experiment", 1,
    "exp_another_experiment", 0
));
```

## Advanced

### Context Attributes

Attributes can be set before the context is ready.

```java
context.setAttribute("user_agent", "Android/12");

context.setAttributes(Map.of(
    "customer_age", "new_customer"
));
```

### Tracking Goals

Goals are created in the A/B Smartly web console.

```java
context.track("payment", Map.of(
    "item_count", 1,
    "total_amount", 1999.99
));
```

### Publishing Pending Data

Sometimes it is necessary to ensure all events have been published to the A/B Smartly collector before proceeding. You can explicitly call `publish()` or `publishAsync()`.

```java
context.publish();
```

### Finalizing

The `close()` and `closeAsync()` methods will ensure all events have been published to the A/B Smartly collector, like `publish()`, and will also "seal" the context, throwing an error if any method that could generate an event is called.

```java
context.close();
```

### Custom Event Logger

The SDK can be instantiated with an event logger used for all contexts. In addition, an event logger can be specified when creating a particular context in the `ContextConfig`.

```java
public class CustomEventLogger implements ContextEventLogger {
    @Override
    public void handleEvent(Context context, ContextEventLogger.EventType event, Object data) {
        switch (event) {
        case Exposure:
            final Exposure exposure = (Exposure) data;
            Log.d("ABSmartly", "exposed to experiment " + exposure.name);
            break;
        case Goal:
            final GoalAchievement goal = (GoalAchievement) data;
            Log.d("ABSmartly", "goal tracked: " + goal.name);
            break;
        case Error:
            Log.e("ABSmartly", "error: " + data);
            break;
        case Publish:
        case Ready:
        case Refresh:
        case Close:
            break;
        }
    }
}
```

Usage:

```java
// For all contexts, during SDK initialization
final ABsmartlyConfig sdkConfig = ABsmartlyConfig.create();
sdkConfig.setContextEventLogger(new CustomEventLogger());

// OR during a particular context initialization
final ContextConfig contextConfig = ContextConfig.create();
contextConfig.setEventLogger(new CustomEventLogger());
```

**Event Types**

| Event      | When                                                       | Data                                   |
| ---------- | ---------------------------------------------------------- | -------------------------------------- |
| `Error`    | `Context` receives an error                                | `Throwable` object                     |
| `Ready`    | `Context` turns ready                                      | `ContextData` used to initialize       |
| `Refresh`  | `Context.refresh()` method succeeds                        | `ContextData` used to refresh          |
| `Publish`  | `Context.publish()` method succeeds                        | `PublishEvent` sent to collector       |
| `Exposure` | `Context.getTreatment()` succeeds on first exposure        | `Exposure` enqueued for publishing     |
| `Goal`     | `Context.track()` method succeeds                          | `GoalAchievement` enqueued for publishing |
| `Close`    | `Context.close()` method succeeds the first time           | `null`                                 |

## Platform-Specific Examples

### Using with an Application Class

Initialize the SDK once in your `Application` subclass so it is available throughout the app lifecycle:

```java
import android.app.Application;
import com.absmartly.android.sdk.ABSmartlyAndroid;

public class MyApplication extends Application {

    private static ABSmartlyAndroid absmartly;

    @Override
    public void onCreate() {
        super.onCreate();

        absmartly = ABSmartlyAndroid.builder()
            .endpoint("https://your-company.absmartly.io/v1")
            .apiKey("YOUR-API-KEY")
            .application("android-app")
            .environment("production")
            .context(getApplicationContext())
            .build();
    }

    public static ABSmartlyAndroid getAbsmartly() {
        return absmartly;
    }
}
```

### Using with Activities

```java
import android.os.Bundle;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import com.absmartly.sdk.*;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Context absmartlyContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ABsmartly sdk = MyApplication.getAbsmartly();
        String deviceId = getOrCreateDeviceId();

        final ContextConfig contextConfig = ContextConfig.create()
            .setUnit("device_id", deviceId);

        absmartlyContext = sdk.createContext(contextConfig);

        absmartlyContext.waitUntilReadyAsync()
            .thenAccept(ctx -> {
                runOnUiThread(() -> setupUI(ctx));
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> setupDefaultUI());
                return null;
            });
    }

    private void setupUI(Context context) {
        int treatment = context.getTreatment("exp_button_color");

        if (treatment == 0) {
            setContentView(R.layout.activity_main_control);
        } else {
            setContentView(R.layout.activity_main_treatment);
        }
    }

    private void setupDefaultUI() {
        setContentView(R.layout.activity_main_control);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (absmartlyContext != null) {
            absmartlyContext.close();
        }
    }

    private String getOrCreateDeviceId() {
        SharedPreferences prefs = getSharedPreferences("absmartly", MODE_PRIVATE);
        String deviceId = prefs.getString("device_id", null);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            prefs.edit().putString("device_id", deviceId).apply();
        }
        return deviceId;
    }
}
```

### Using with Fragments

```java
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.absmartly.sdk.*;

public class ProductFragment extends Fragment {

    private Context absmartlyContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ABsmartly sdk = MyApplication.getAbsmartly();

        final ContextConfig contextConfig = ContextConfig.create()
            .setUnit("device_id", getDeviceId());

        absmartlyContext = sdk.createContext(contextConfig);

        absmartlyContext.waitUntilReadyAsync()
            .thenAccept(ctx -> {
                requireActivity().runOnUiThread(() -> {
                    int treatment = ctx.getTreatment("exp_product_layout");
                    if (treatment == 1) {
                        // apply treatment layout changes
                    }
                });
            });

        return inflater.inflate(R.layout.fragment_product, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (absmartlyContext != null) {
            absmartlyContext.close();
        }
    }

    private String getDeviceId() {
        return requireActivity()
            .getSharedPreferences("absmartly", android.content.Context.MODE_PRIVATE)
            .getString("device_id", "");
    }
}
```

### Lifecycle-Aware Context Cancellation

Cancel in-flight context creation when the Activity or Fragment is destroyed:

```java
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.absmartly.sdk.*;
import java8.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    private CompletableFuture<Context> contextFuture;
    private Context absmartlyContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ABsmartly sdk = MyApplication.getAbsmartly();

        final ContextConfig contextConfig = ContextConfig.create()
            .setUnit("device_id", getOrCreateDeviceId());

        absmartlyContext = sdk.createContext(contextConfig);
        contextFuture = absmartlyContext.waitUntilReadyAsync();

        contextFuture.thenAccept(ctx -> {
            runOnUiThread(() -> setupUI(ctx));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (contextFuture != null && !contextFuture.isDone()) {
            contextFuture.cancel(true);
        }

        if (absmartlyContext != null) {
            absmartlyContext.close();
        }
    }
}
```

## Android-Specific Components

### SqliteAndroidLocalCache

The Android SDK provides an Android-optimized implementation using SQLite for persisting context data and pending publish events. This ensures events are not lost if the app is terminated before they can be sent to the collector.

```java
import com.absmartly.android.sdk.cache.SqliteAndroidLocalCache;

SqliteAndroidLocalCache cache = new SqliteAndroidLocalCache(getApplicationContext());

// Store context data
cache.writeContextData(context.getData());

// Retrieve context data
ContextData cachedData = cache.getContextData();

// Store a publish event for later retry
cache.writePublishEvent(publishEvent);

// Retrieve and clear all pending publish events
List<PublishEvent> pendingEvents = cache.retrievePublishEvents();
```

The SQLite database is named `absmartly.db` and is automatically created in the application's default database directory.

## About A/B Smartly

**A/B Smartly** is the leading provider of state-of-the-art, on-premises, full-stack experimentation platforms for engineering and product teams that want to confidently deploy features as fast as they can develop them.
A/B Smartly's real-time analytics helps engineering and product teams ensure that new features will improve the customer experience without breaking or degrading performance and/or business metrics.

### Have a look at our growing list of clients and SDKs:
- [Java SDK](https://www.github.com/absmartly/java-sdk)
- [JavaScript SDK](https://www.github.com/absmartly/javascript-sdk)
- [PHP SDK](https://www.github.com/absmartly/php-sdk)
- [Swift SDK](https://www.github.com/absmartly/swift-sdk)
- [Vue2 SDK](https://www.github.com/absmartly/vue2-sdk)
- [Vue3 SDK](https://www.github.com/absmartly/vue3-sdk)
- [React SDK](https://www.github.com/absmartly/react-sdk)
- [Angular SDK](https://www.github.com/absmartly/angular-sdk)
- [Android SDK](https://www.github.com/absmartly/android-sdk) (this package)
- [Python3 SDK](https://www.github.com/absmartly/python3-sdk)
- [Go SDK](https://www.github.com/absmartly/go-sdk)
- [Ruby SDK](https://www.github.com/absmartly/ruby-sdk)
- [.NET SDK](https://www.github.com/absmartly/dotnet-sdk)
- [Dart SDK](https://www.github.com/absmartly/dart-sdk)
- [Flutter SDK](https://www.github.com/absmartly/flutter-sdk)
