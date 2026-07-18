# Appro Native Base

A reusable Android base library for Kotlin and Jetpack Compose applications. It provides shared application configuration, Koin dependency injection, Material 3 theming, session persistence, networking, OTP authentication, subscription products, store payments, notifications, optional Firebase Cloud Messaging, deep links, and reusable Compose components.

## Installation

Add JitPack to the consuming project:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Add the library to `build.gradle.kts (:app)`:

```kotlin
dependencies {
    implementation("com.github.HadiSormeyli:ApproNativeBase:v1.0.0")
}
```

## Requirements

| Requirement | Project value |
|---|---:|
| Minimum Android SDK | 24 |
| Compile SDK | 36 |
| Java | 21 |
| Kotlin | 2.3.20 |
| Android Gradle Plugin | 8.13.2 |
| Gradle | 9.1.0 |
| UI | Jetpack Compose + Material 3 |
| Dependency injection | Koin |
| Local database | Room |
| Preferences | DataStore Preferences |
| HTTP | Retrofit + OkHttp |

The consuming application should use Java and Kotlin JVM target 21.

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
```

## Store flavors

Add a `store` flavor dimension to `build.gradle.kts (:app)`:

```kotlin
android {
    flavorDimensions += "store"

    productFlavors {
        create("bazaar") {
            dimension = "store"

            buildConfigField(
                "String",
                "FLAVOR_NAME",
                "\"BAZAAR\""
            )

            buildConfigField(
                "String",
                "PAYMENT_RSA_KEY",
                "\"YOUR_BAZAAR_RSA_KEY\""
            )
        }

        create("myket") {
            dimension = "store"

            buildConfigField(
                "String",
                "FLAVOR_NAME",
                "\"MYKET\""
            )

            buildConfigField(
                "String",
                "PAYMENT_RSA_KEY",
                "\"YOUR_MYKET_RSA_KEY\""
            )
        }

        create("googlePlay") {
            dimension = "store"

            buildConfigField(
                "String",
                "FLAVOR_NAME",
                "\"GOOGLE_PLAY\""
            )

            buildConfigField(
                "String",
                "PAYMENT_RSA_KEY",
                "\"\""
            )
        }
    }
}
```

`Flavor.fromString()` supports `BAZAAR`, `MYKET`, and `GOOGLE_PLAY`.

## Application initialization

Create an `Application` class and initialize the library once.

This example includes:

- the base library;
- application-specific Koin modules;
- an optional deep-link parser;
- optional Firebase support.

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val approConfig = ApproConfig(
            packageName = BuildConfig.APPLICATION_ID,
            flavor = Flavor.fromString(BuildConfig.FLAVOR_NAME),
            storeLink = "YOUR_MARKET_APPLICATION_LINK",
            deepLink = "your application deepLink",
            paymentRsaKey = BuildConfig.PAYMENT_RSA_KEY,
            isPaymentAvailable = BuildConfig.PAYMENT_RSA_KEY.isNotBlank(),
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
            defaultLocale = Locale.forLanguageTag("fa-IR"),
            debug = BuildConfig.DEBUG
        )

        val firebaseConfig = FirebaseConfig(
            smallIcon = R.drawable.ic_notification,
            defaultTitle = getString(R.string.app_name),
            channelGroup = NotificationChannelGroupConfig(
                id = "firebase",
                name = "Firebase notifications"
            ),
            channel = NotificationChannelConfig(
                id = "firebase_general",
                name = "Notifications",
                description = "Application notifications",
                importance = NotificationManager.IMPORTANCE_HIGH
            )
        )

        Appro.initialize(
            application = this,
            config = approConfig,
            deepLinkParser = AppDeepLinkParser(),
            appModules = listOf(
                appModule,
                firebaseModule(firebaseConfig)
            )
        )
    }
}
```

## Required AndroidManifest Setup
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
   
    <application android:name=".App" android:allowBackup="true" android:label="@string/app_name"
        android:supportsRtl="true" android:theme="@style/Theme.YourApplication">
        <service android:name="com.approagency.base.firebase.ApproFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
        <activity android:name=".MainActivity" android:exported="true"
            android:launchMode="singleTop">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:host="example.com" />
                <data android:scheme="example" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

## `ApproConfig`

`ApproConfig` is registered as a Koin singleton and controls the main behavior and appearance of the library.

```kotlin
data class ApproConfig(
    val packageName: String,
    val flavor: Flavor,
    val storeLink: String,
    val deepLinkScheme: String = "",
    val deepLinkSchemes: List<String> = listOf(deepLinkScheme),
    val paymentRsaKey: String,
    val isPaymentAvailable: Boolean = paymentRsaKey.isNotEmpty(),
    val versionName: String,
    val versionCode: Int,
    val defaultLocale: Locale = Locale.forLanguageTag("fa-IR"),
    val lightColorSchema: ColorScheme = createLightColorScheme(Color(0xFF6750A4)),
    val darkColorSchema: ColorScheme = createDarkColorScheme(Color(0xFFD0BCFF)),
    val typography: Typography = ApproTypography,
    val shapes: Shapes = ApproShapes,
    val debug: Boolean = false,
    val shimmerColors: ShimmerColors? = null,
    val defaultThemeMode: ThemeMode = ThemeMode.SYSTEM,
    val providers: @Composable (isDarkMode: Boolean) -> Array<ProvidedValue<*>> = { emptyArray() },
    val extra: Map<String, Any?> = emptyMap()
)
```

Important properties:

| Property                               | Purpose                                                     |
|----------------------------------------|-------------------------------------------------------------|
| `packageName`                          | Package sent to Appro APIs and used for local storage names |
| `flavor`                               | Selects Bazaar, Myket, or Google Play payment               |
| `storeLink`                            | Application page in the selected market                     |
| `deepLinks`                            | application deep links                                      |
| `deepLink`                             | main application deep link                                  |
| `paymentRsaKey`                        | Bazaar or Myket RSA public key                              |
| `isPaymentAvailable`                   | Enables or disables payment                                 |
| `versionName` / `versionCode`          | Host application version                                    |
| `defaultLocale`                        | Initial application locale                                  |
| `lightColorSchema` / `darkColorSchema` | Material 3 color schemes                                    |
| `typography` / `shapes`                | Material theme configuration                                |
| `shimmerColors`                        | Optional custom shimmer colors                              |
| `defaultThemeMode`                     | `SYSTEM`, `LIGHT`, or `DARK`                                |
| `providers`                            | Custom CompositionLocal providers                           |
| `extra`                                | Application-specific values                                 |

## Host application manifest

Add market package visibility:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <queries>
        <package android:name="com.farsitel.bazaar" />
        <package android:name="ir.mservices.market" />
        <package android:name="com.android.vending" />
    </queries>

    <application
        android:name=".App"
        android:supportsRtl="true" />
</manifest>
```

`com.android.vending` is the Google Play Store package.

## Base activity

Extend `BaseActivity` for the main Compose activity:

```kotlin
class MainActivity : BaseActivity() {
    @Composable
    override fun CreateView() {
        AppContent(
            approViewModel = approViewModel
        )
    }
}
```

`BaseActivity` provides:

- `ApproTheme`;
- the activity-scoped `ApproViewModel`;
- `SessionManager`;
- `ThemeManager`;
- `ApproConfig`;
- `DeepLinkManager`;
- drawer state and open/close helpers;
- snackbar support;
- subscription bottom-sheet state;
- OTP autofill;
- deep-link intent handling in `onCreate()` and `onNewIntent()`.

Passing `approViewModel` from the activity to composables guarantees that those composables use the same activity-scoped ViewModel instance.

## Theme and localization

The library uses Material 3 and supports:

- light and dark color schemes;
- custom typography;
- custom shapes;
- persisted theme mode;
- RTL/LTR direction based on the selected locale;
- custom CompositionLocals through `ApproConfig.providers`.

```kotlin
val approConfig = ApproConfig(
    packageName = BuildConfig.APPLICATION_ID,
    flavor = Flavor.fromString(BuildConfig.FLAVOR_NAME),
    storeLink = "YOUR_STORE_LINK",
    paymentRsaKey = BuildConfig.PAYMENT_RSA_KEY,
    versionName = BuildConfig.VERSION_NAME,
    versionCode = BuildConfig.VERSION_CODE,
    lightColorSchema = createLightColorScheme(Color(0xFF006C4C)),
    darkColorSchema = createDarkColorScheme(Color(0xFF60DDAA)),
    typography = AppTypography,
    shapes = AppShapes
)
```

Custom providers:

```kotlin
val approConfig = ApproConfig(
    packageName = BuildConfig.APPLICATION_ID,
    flavor = Flavor.fromString(BuildConfig.FLAVOR_NAME),
    storeLink = "YOUR_STORE_LINK",
    paymentRsaKey = BuildConfig.PAYMENT_RSA_KEY,
    versionName = BuildConfig.VERSION_NAME,
    versionCode = BuildConfig.VERSION_CODE,
    providers = { isDarkMode ->
        arrayOf(
            LocalCustomColors provides if (isDarkMode) {
                DarkCustomColors
            } else {
                LightCustomColors
            }
        )
    }
)
```

## State architecture

The library includes reusable event/state/effect ViewModel foundations:

```kotlin
BaseViewModel<Event, State, SideEffect>
BaseApplicationViewModel<Event, State, SideEffect>
```

`ApproViewModel` handles the built-in Appro flow:

- send login OTP;
- verify OTP;
- save the session;
- check subscription status;
- load products;
- perform a purchase;
- refresh status after purchase;
- send the FCM token;
- reset authentication and purchase states.

Observe state:

```kotlin
val state by approViewModel.state
```

Trigger events:

```kotlin
approViewModel.onTriggerEvent(
    ApproContract.Event.Login(
        phoneNumber = "09123456789"
    )
)
```

```kotlin
approViewModel.onTriggerEvent(
    ApproContract.Event.CheckOtp(
        phoneNumber = "09123456789",
        otp = "12345"
    )
)
```

```kotlin
approViewModel.onTriggerEvent(
    ApproContract.Event.GetProducts
)
```

## Session and local storage

### Room session

`SessionManager` persists sessions through Room and exposes:

```kotlin
val state: StateFlow<SessionState>
```

Session states:

```kotlin
SessionState.Loading
SessionState.Logout
SessionState.Login(session)
```

Main operations:

```kotlin
sessionManager.login(session)
sessionManager.updateTokens(...)
sessionManager.updateUser(...)
sessionManager.logout()
val session = sessionManager.getSession()
```

### DataStore preferences

`PreferencesHelper` stores library and application preferences using a DataStore name based on `ApproConfig.packageName`.

Built-in keys include theme mode and language. Applications may also create and pass their own `Preferences.Key<T>` values.

## Networking

The library provides:

- public and authenticated OkHttp clients;
- public and authenticated Retrofit instances;
- an `ApproTokenInterceptor`;
- Gson conversion;
- HTTP logging;
- centralized `networkCall`;
- `Resource.Loading`, `Resource.Success`, and `Resource.Error`;
- `Failure` conversion;
- `ApproService`;
- `ApproRepository`.

Built-in Appro endpoints cover:

- promotions;
- OTP login;
- OTP verification;
- subscription status;
- products;
- product subscription;
- FCM token registration.

Named Koin instances:

```kotlin
ApproConstants.APPRO_PUBLIC_OKHTTP
ApproConstants.APPRO_PRIVATE_OKHTTP
ApproConstants.APPRO_PUBLIC_RETROFIT
ApproConstants.APPRO_PRIVATE_RETROFIT
```

Create application services from the appropriate Retrofit instance:

```kotlin
val appModule = module {
    single {
        get<Retrofit>(
            named(ApproConstants.APPRO_PRIVATE_RETROFIT)
        ).createWebService<AppService>()
    }
}
```

## Payments

`PaymentService` is selected automatically from `ApproConfig.flavor`.

| Flavor | Service |
|---|---|
| `Flavor.BAZAAR` | `BazaarPaymentService` |
| `Flavor.MYKET` | `MyketPaymentService` |
| `Flavor.GOOGLE_PLAY` | `GooglePlayPaymentService` |

Bazaar uses Poolakey. Myket uses Myket Billing Client. Google Play is currently a placeholder and throws `Failure.StoreUnavailable`.

Start a purchase through `ApproViewModel`:

```kotlin
approViewModel.onTriggerEvent(
    ApproContract.Event.Purchase(
        activity = activity,
        paymentRequest = PaymentRequest(
            productId = product.id,
            productUuid = product.uuid,
            type = PaymentProductType.SUBSCRIPTION
        )
    )
)
```

`PaymentProductType` supports:

```kotlin
PaymentProductType.SUBSCRIPTION
PaymentProductType.IN_APP
```

The purchase token is sent to the Appro backend before the purchase is treated as successful.

## Generic notifications

`NotificationHelper` is independent from Firebase and can display local notifications created by any part of the application.

It supports:

- notification permission checks and requests;
- notification settings;
- channels and channel groups;
- small and large icons;
- big text and big picture styles;
- actions and remote input;
- progress;
- groups;
- custom `RemoteViews`;
- full-screen intents;
- sounds and vibration;
- notification update and cancellation.

Create the required channel:

```kotlin
val notificationHelper: NotificationHelper by inject()

notificationHelper.createChannel(
    NotificationChannelConfig(
        id = "general",
        name = "General",
        description = "General notifications",
        importance = NotificationManager.IMPORTANCE_DEFAULT
    )
)
```

Request permission:

```kotlin
notificationHelper.requestPermission(this)
```

Show a notification:

```kotlin
notificationHelper.show(
    NotificationRequest(
        channelId = "general",
        smallIcon = R.drawable.ic_notification,
        title = "Title",
        text = "Description",
        style = NotificationCompat.BigTextStyle()
            .bigText("Description")
    )
)
```

## Optional Firebase Cloud Messaging

Firebase is optional at runtime. Applications that do not need Firebase should omit the Firebase module and all Firebase-specific host setup.

### 1. Add `google-services.json`

Download `google-services.json` from Firebase and place it in:

```text
app/google-services.json
```

### 2. Apply the Google Services plugin

Add the plugin to the consuming application:

```kotlin
plugins {
    id("com.google.gms.google-services")
}
```

### 3. Add manifest permissions and service

Add these only to Firebase-enabled applications:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application>
        <service
            android:name="com.approagency.base.firebase.ApproFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
    </application>
</manifest>
```

### 4. Create `FirebaseConfig`

Firebase uses the existing generic `NotificationHelper`; it does not create a second notification system.

```kotlin
val firebaseConfig = FirebaseConfig(
    smallIcon = R.drawable.ic_notification,
    notificationColor = ContextCompat.getColor(
        this,
        R.color.notification_color
    ),
    defaultTitle = getString(R.string.app_name),
    channelGroup = NotificationChannelGroupConfig(
        id = "firebase",
        name = "Firebase notifications"
    ),
    channel = NotificationChannelConfig(
        id = "firebase_general",
        name = "Notifications",
        description = "Application notifications",
        importance = NotificationManager.IMPORTANCE_HIGH
    ),
    showForegroundNotifications = true,
    showBackgroundNotifications = true,
    notificationFilter = { message ->
        true
    },
    onTokenChanged = { token ->
        Log.d("Firebase", token)
    },
    onMessageReceived = { message ->
        Log.d("Firebase", message.toString())
    }
)
```

### 5. Pass the Firebase module

```kotlin
Appro.initialize(
    application = this,
    config = approConfig,
    deepLinkParser = AppDeepLinkParser(),
    appModules = listOf(
        appModule,
        firebaseModule(firebaseConfig)
    )
)
```

### 6. Request notification permission

```kotlin
val firebaseManager: FirebaseManager by inject()

firebaseManager.requestNotificationPermission(this)
```

### 7. Token and topics

```kotlin
lifecycleScope.launch {
    val token = firebaseManager.getToken()
}
```

```kotlin
lifecycleScope.launch {
    firebaseManager.subscribeToTopic("news")
}
```

```kotlin
lifecycleScope.launch {
    firebaseManager.unsubscribeFromTopic("news")
}
```

### Firebase message format

The mapped `FirebaseMessage` contains only:

```kotlin
data class FirebaseMessage(
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val data: Map<String, String>
)
```

All fields other than title, description, and image URL remain inside `data`.

Recommended data payload:

```json
{
  "message": {
    "token": "FCM_TOKEN",
    "android": {
      "priority": "high"
    },
    "data": {
      "title": "Notification title",
      "description": "Notification description",
      "image_url": "https://example.com/image.jpg",
      "link": "myapp://product/25",
      "id": "25"
    }
  }
}
```

`image_url` is optional. When present, the manager downloads the image directly and displays a big-picture notification. The notification is still displayed without an image if loading fails.

`link` is optional. It is attached to the app launch intent and is then handled by `DeepLinkManager`.

## Deep links

Deep-link parsing is application-specific. The base library owns the manager and navigation behavior, while the consuming project supplies the parser.

Navigation types:

| Type | Behavior |
|---|---|
| `PUSH` | Pushes a new destination |
| `SINGLE_TOP` | Reuses the destination when possible |
| `CLEAR_STACK` | Clears the navigation stack and opens the destination |

### Configure schemes

```kotlin
val approConfig = ApproConfig(
    packageName = BuildConfig.APPLICATION_ID,
    flavor = Flavor.fromString(BuildConfig.FLAVOR_NAME),
    storeLink = "YOUR_STORE_LINK",
    deepLinks = listOf("deep link"),
    deepLink = "deep link",
    paymentRsaKey = BuildConfig.PAYMENT_RSA_KEY,
    versionName = BuildConfig.VERSION_NAME,
    versionCode = BuildConfig.VERSION_CODE
)
```

### Create the project parser

Create `AppDeepLinkParser.kt`:

```kotlin
class AppDeepLinkParser : DeepLinkParser {
    override fun parse(
        input: DeepLinkInput
    ): DeepLinkTarget? {
        return when (input.uri.host) {
            "home" -> {
                DeepLinkTarget(
                    route = Route.Home,
                    navigationType =
                        DeepLinkNavigationType.CLEAR_STACK
                )
            }

            "settings" -> {
                DeepLinkTarget(
                    route = Route.Settings,
                    navigationType =
                        DeepLinkNavigationType.SINGLE_TOP
                )
            }

            "product" -> {
                val productId =
                    input.uri.pathSegments.firstOrNull()
                        ?: input.data["product_id"]
                        ?: return null

                DeepLinkTarget(
                    route = Route.Product(productId),
                    navigationType =
                        DeepLinkNavigationType.PUSH
                )
            }

            else -> null
        }
    }
}
```

Pass it during initialization:

```kotlin
Appro.initialize(
    application = this,
    config = approConfig,
    deepLinkParser = AppDeepLinkParser(),
    appModules = listOf(appModule)
)
```

### Register the activity intent filter

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTop">

    <intent-filter>
        <action android:name="android.intent.action.VIEW" />

        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <data
            android:scheme="myapp" />
    </intent-filter>
</activity>
```

### Attach the navigation handler

`BaseActivity` submits incoming intents to `DeepLinkManager`. Place the handler beside the application `NavHost`:

```kotlin
@Composable
fun AppContent(
    navController: NavHostController =
        rememberNavController()
) {
    DeepLinkNavigationHandler(
        navController = navController
    )

    NavHost(
        navController = navController,
        startDestination = Route.Home
    ) {
        // Destinations
    }
}
```

Deep links listed in `ApproConfig.deepLinks` are routed through the app parser. Other schemes are opened externally.

### Create a deep link

```kotlin
val deepLinkManager: DeepLinkManager by inject()

val link = deepLinkManager.createLink(
    route = "product",
    pathParameters = listOf("25"),
    queryParameters = mapOf(
        "source" to "share"
    )
)
```

Result:

```text
myapp://product/25?source=share
```

### Consume deep-link events manually

`DeepLinkManager.events` is a replaying `SharedFlow` with capacity one. A project may collect it wherever needed:

```kotlin
lifecycleScope.launch {
    deepLinkManager.events.collectLatest { event ->
        // Handle event
        deepLinkManager.consume(event.id)
    }
}
```

## Subscription bottom sheet

`SubscriptionBottomSheet` integrates with the activity-scoped `ApproViewModel` and supports:

- phone-number login;
- OTP verification;
- OTP autofill;
- product loading;
- product selection;
- Bazaar/Myket purchase;
- loading and error states;
- successful purchase handling.

```kotlin
SubscriptionBottomSheet(
    approViewModel = activity.approViewModel,
    onDismiss = activity::hideSubscriptionDialog
)
```

## Navigation helpers

The library provides:

- `drawerAwareComposable`;
- `navigateSafely`;
- `navigateAndClean`;
- `popBackStackSafely`;
- shared ViewModel helpers;
- deep-link navigation;
- horizontal and vertical bottom navigation.

`BottomNavigationItem` accepts resource, plain-string, or annotated labels and resource or vector icons:

```kotlin
val item = BottomNavigationItem(
    label = Label.Resource(R.string.home),
    icon = Icon.Resource(R.drawable.ic_home),
    selectedIcon = Icon.Resource(R.drawable.ic_home_filled),
    route = Route.Home,
    badgeCount = 3
)
```

## Drawer

Drawer items support:

- simple items;
- expandable items;
- dividers;
- selected state;
- enabled state;
- badges;
- custom background and foreground colors;
- resource or vector icons;
- resource, plain-string, or annotated labels.

```kotlin
val drawerItems = listOf(
    DrawerItem.Simple(
        title = Label.Resource(R.string.home),
        icon = Icon.Resource(R.drawable.ic_home),
        selected = true,
        onClick = {
            navController.navigate(Route.Home)
        }
    ),
    DrawerItem.Divider(),
    DrawerItem.DropDown(
        title = Label.Text("Settings"),
        children = listOf(
            DrawerItem.Simple(
                title = Label.Text("Theme")
            )
        )
    )
)
```

## Reusable Compose components

The library includes customizable Material 3 components without requiring applications to rebuild common UI primitives.

### Buttons and controls

- filled, outlined, simple text, icon, and loading buttons;
- dotted loading button;
- labeled switch;
- labeled checkbox.

### Text fields

- customizable outlined text field;
- OTP field;
- password field.

### Loading and shimmer

- circular loading;
- three-dot loading;
- refresh indicator;
- loading dialog;
- shimmer text;
- shimmer image;
- shimmer icon;
- shimmer icon button;
- shimmer container.

Shimmer colors may be supplied through `ApproConfig.shimmerColors`.

### App bars and navigation

- simple app bar;
- drawer app bar;
- drawer icon button;
- responsive bottom navigation;
- drawer content and items.

### Errors, images, and feedback

- full error layout;
- simple refresh-only error layout;
- Coil network image;
- custom snackbars and snackbar host.

### Modals

- configurable Material bottom sheet;
- alert bottom sheet;
- modal header and drag handle.

### Layout

- horizontal spacer;
- vertical spacer;
- weighted fill spacer.

### Other systems

- circular theme reveal;
- feature showcase;
- subscription bottom sheet.

## Circular theme reveal

Wrap the screen content with `CircularThemeReveal` and mark the theme button with `circularThemeRevealOrigin`.

```kotlin
val revealState = rememberCircularThemeRevealState(
    isDarkTheme = isDarkMode
)

CircularThemeReveal(
    isDarkTheme = isDarkMode,
    state = revealState,
    onThemeChange = { dark ->
        themeManager.changeDarkMode(dark)
    }
) {
    AppContent(
        themeButtonModifier =
            Modifier.circularThemeRevealOrigin(
                revealState
            ),
        onThemeClick = revealState::toggle
    )
}
```

The component records the screen and reveals the new theme from the selected UI element.

## OTP autofill

`BaseActivity` implements `OtpAutofillController` through the SMS User Consent API.

```kotlin
activity.startOtpAutofill()
```

Collected OTP values are emitted through `OtpAutoFillBus`.

The current implementation extracts a five-digit code.

## Showcase

The showcase system can highlight application features with configurable:

- target position;
- gravity and side;
- arrow;
- message;
- animation;
- sequential showcase state.

Use it for onboarding or feature discovery without implementing a separate overlay system.

## Project structure

```text
com.approagency.base
├── config
├── di
├── firebase
├── local
│   ├── preference
│   └── room
├── model
│   ├── network
│   ├── session
│   ├── showcase
│   ├── ui
│   │   ├── deepLink
│   │   └── notification
│   └── user
├── network
│   ├── dto
│   ├── interceptor
│   ├── repository
│   └── service
├── paymnet
├── presentation
│   ├── components
│   └── navigation
├── session
├── theme
└── utils
```

## Built-in dependency versions

| Dependency | Version |
|---|---:|
| Kotlin | 2.3.20 |
| Coroutines | 1.11.0 |
| Koin | 4.2.2 |
| Room | 2.8.4 |
| DataStore | 1.2.1 |
| Retrofit | 3.0.0 |
| OkHttp | 5.4.0 |
| Compose BOM | 2026.06.01 |
| Navigation Compose | 2.9.8 |
| Coil | 2.7.0 |
| Poolakey | 2.2.0 |
| Myket Billing | 1.19 |
| Firebase BOM | 34.16.0 |

## Before publishing `v1.0`

Review these current source issues before publishing the release:

1. `uiModule` constructs `DeepLinkManager()` while its current constructor requires `ApproConfig`; register it with `DeepLinkManager(config = get())`.
2. The Room database name currently contains an extra closing brace: `"${packageName}.base.db}"`.
3. The package name is currently spelled `paymnet`; renaming it after release will be a breaking API change.
4. The Google Services plugin is currently applied to the library module even though Firebase is intended to be optional. Prefer applying it only in Firebase-enabled consuming applications.
5. Firebase is currently bundled as a library dependency. A separate optional Firebase artifact would make it truly optional at dependency level.
6. `GooglePlayPaymentService` is a placeholder and does not implement Google Play Billing.
7. All store payment SDKs are currently included together. Flavor-specific artifacts or source sets would reduce final application size.
8. The Firebase service is not declared in the base manifest, so Firebase-enabled applications must add it to their own manifest as documented above.
9. Verify the deep-link external-link flow: the current manager parses before emitting, so external links may need to be emitted even when no internal target exists.
10. Add Maven/JitPack publishing configuration and verify the exact `v1.0` artifact before advertising the dependency coordinate.
11. Unregister the OTP broadcast receiver in `BaseActivity.onDestroy()`.
12. Review `addSLLFactory()`: it trusts every certificate and hostname and must not be used in production.
13. Add unit and instrumentation tests for sessions, authentication, payments, Firebase, deep links, and navigation.

## Security

- Never commit market private credentials.
- `PAYMENT_RSA_KEY` is a public verification key, not a private server key.
- Always verify purchase tokens on the backend.
- Do not use the trust-all SSL helper in production.
- Validate and restrict deep-link destinations and parameters.
- Validate Firebase data before navigation.
- Do not place access tokens or personal information in notification data or deep-link query parameters.

## License

Add a `LICENSE` file before public distribution.
