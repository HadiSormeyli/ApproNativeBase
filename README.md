# ApproNativeBase — Complete README, Integration, and API Guide

This single-file guide explains how to install, configure, initialize, and use the current `ApproNativeBase` Android library in a Kotlin + Jetpack Compose application. It was prepared against the uploaded source snapshot and the current `ApproConfig` shown below.

It is based on the current library source under the namespace:

```text
com.approagency.base
```

The guide covers:

- store-specific Gradle setup for Bazaar, Myket, and Google Play;
- the current `ApproConfig` API;
- application and Koin initialization;
- Firebase Cloud Messaging setup and `FirebaseManager` usage;
- `PreferencesHelper`;
- `SessionManager`;
- `NotificationManager`;
- `ThemeManager`;
- deep links;
- `BaseActivity`;
- `BaseViewModel` and MVI architecture;
- `ApproViewModel` and `ApproContract`;
- the built-in subscription and terms bottom sheets;
- payment behavior;
- networking and Room/KSP requirements;
- all reusable Compose components;
- troubleshooting and current limitations.

---

## Table of contents

1. [Library overview](#1-library-overview)
2. [Requirements](#2-requirements)
3. [Install the library](#3-install-the-library)
4. [Configure store flavors](#4-configure-store-flavors)
5. [Current ApproConfig](#5-current-approconfig)
6. [Application initialization](#6-application-initialization)
7. [AndroidManifest setup](#7-androidmanifest-setup)
8. [Deep-link setup](#8-deep-link-setup)
9. [BaseActivity implementation](#9-baseactivity-implementation)
10. [PreferencesHelper](#10-preferenceshelper)
11. [SessionManager](#11-sessionmanager)
12. [Firebase and FirebaseManager](#12-firebase-and-firebasemanager)
13. [NotificationManager](#13-notificationmanager)
14. [Theme and localization](#14-theme-and-localization)
15. [BaseViewModel and MVI](#15-baseviewmodel-and-mvi)
16. [ApproViewModel and ApproContract](#16-approviewmodel-and-approcontract)
17. [Subscription and terms flow](#17-subscription-and-terms-flow)
18. [Payments](#18-payments)
19. [Networking](#19-networking)
20. [Room and KSP](#20-room-and-ksp)
21. [Navigation helpers](#21-navigation-helpers)
22. [Reusable Compose components](#22-reusable-compose-components)
23. [Recommended project structure](#23-recommended-project-structure)
24. [Troubleshooting](#24-troubleshooting)
25. [Security and current limitations](#25-security-and-current-limitations)
26. [Quick API reference](#26-quick-api-reference)
27. [Complete Compose component reference](#27-complete-compose-component-reference)
28. [End-to-end minimum checklist](#28-end-to-end-minimum-checklist)

---

## 1. Library overview

`ApproNativeBase` is a reusable Android library for Compose applications. It provides:

- centralized application configuration through `ApproConfig`;
- Koin dependency injection;
- Material 3 themes, typography, shapes, RTL/LTR, and persisted theme mode;
- Room-based session persistence;
- DataStore preferences;
- public and authenticated Retrofit/OkHttp clients;
- Appro login, OTP verification, subscription status, products, and promotions;
- Bazaar subscription payments through Poolakey;
- Myket subscription payments through Myket Billing Client;
- an optional Firebase Cloud Messaging manager;
- local notification channel, permission, and display utilities;
- configurable deep-link parsing and Compose navigation;
- SMS User Consent OTP autofill;
- reusable Compose UI components.

Main package structure:

```text
com.approagency.base
├── config
├── di
├── firebase
├── local
│   ├── preference
│   └── room
├── model
├── network
├── paymnet
├── presentation
│   ├── components
│   └── navigation
├── session
├── theme
└── utils
```

> The payment package is currently spelled `paymnet`. Use that exact spelling in imports.

---

## 2. Requirements

The current library source uses:

| Item | Project value |
|---|---:|
| Minimum SDK | 24 |
| Compile SDK | 36 |
| Java target | 21 |
| Android Gradle Plugin | 8.13.2 |
| Gradle wrapper | 9.1.0 |
| Kotlin | 2.3.20 |
| Koin | 4.2.2 |
| Room | 2.8.4 |
| Compose BOM | 2026.06.01 |
| Firebase BOM | 34.16.0 |
| Poolakey | 2.2.0 |
| Myket Billing | 1.19 |

Configure JVM 21 in the consuming application:

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
```

The Gradle wrapper version and Android Gradle Plugin version are separate. Gradle `9.1.0` does not mean AGP `9.1` is installed.

---

## 3. Install the library

### 3.1 Add JitPack

In the root `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

### 3.2 Add the published artifacts

The library publishes one Maven module for each store:

```text
com.github.HadiSormeyli.ApproNativeBase:bazar:<tag>
com.github.HadiSormeyli.ApproNativeBase:myket:<tag>
com.github.HadiSormeyli.ApproNativeBase:googleplay:<tag>
```

Add them to `gradle/libs.versions.toml`:

```toml
[versions]
approNativeBase = "1.3.0" # Replace with the exact Git tag

[libraries]
appro-native-base-bazar = { module = "com.github.HadiSormeyli.ApproNativeBase:bazar", version.ref = "approNativeBase" }

appro-native-base-myket = { module = "com.github.HadiSormeyli.ApproNativeBase:myket", version.ref = "approNativeBase" }

appro-native-base-googleplay = { module = "com.github.HadiSormeyli.ApproNativeBase:googleplay", version.ref = "approNativeBase" }
```
---

## 4. Configure store flavors

The consuming app must use the same flavor dimension:

```kotlin
android {
    flavorDimensions += "store"

    productFlavors {
        create("bazar") {
            dimension = "store"

            buildConfigField(
                "String",
                "FLAVOR_NAME",
                "\"BAZAR\""
            )

            buildConfigField(
                "String",
                "MARKET_RSA",
                "\"YOUR_BAZAAR_RSA_PUBLIC_KEY\""
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
                "MARKET_RSA",
                "\"YOUR_MYKET_RSA_PUBLIC_KEY\""
            )

            val marketId = "ir.mservices.market"
            manifestPlaceholders["marketApplicationId"] = marketId
            manifestPlaceholders["marketBindAddress"] =
                "$marketId.InAppBillingService.BIND"
            manifestPlaceholders["marketPermission"] =
                "$marketId.BILLING"
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
                "MARKET_RSA",
                "\"\""
            )
        }
    }

    buildTypes {
        debug {
            // The published library exposes release variants.
            matchingFallbacks += listOf("release")
        }
    }
}

dependencies {
    "bazarImplementation"(libs.appro.native.base.bazar)
    "myketImplementation"(libs.appro.native.base.myket)
    "googlePlayImplementation"(libs.appro.native.base.googleplay)
}
```

`Flavor.fromString()` accepts the enum name, gateway, English label, or Persian label. Unknown values fall back to `Flavor.BAZAR`.

```kotlin
val flavor = Flavor.fromString(BuildConfig.FLAVOR_NAME)
```

Store helpers are available on `ApproConfig`:

```kotlin
config.isBazaar()
config.isMyket()
config.isGooglePlay()
```

---

## 5. Current ApproConfig

The current configuration model is:

```kotlin
data class ApproConfig(
    val appName: String,
    val packageName: String,
    val flavor: Flavor,
    val paymentRsaKey: String,
    val versionName: String,
    val versionCode: Int,
    val debug: Boolean,
    val logEnabled: Boolean = debug,
    val storeLink: String? = null,
    val deepLink: String = "",
    val legalConfig: LegalConfig = LegalConfig(),
    val deepLinks: List<String> = listOf(deepLink),
    val isPaymentAvailable: Boolean = paymentRsaKey.isNotEmpty(),
    val defaultLocale: Locale = Locale.forLanguageTag("fa-IR"),
    val lightColorSchema: ColorScheme =
        createLightColorScheme(Color(0xFF6750A4)),
    val darkColorSchema: ColorScheme =
        createDarkColorScheme(Color(0xFFD0BCFF)),
    val typography: Typography = ApproTypography,
    val shapes: Shapes = ApproShapes,
    val shimmerConfig: ShimmerConfig? = null,
    val defaultThemeMode: ThemeMode = ThemeMode.SYSTEM,
    val providers: @Composable (
        isDarkMode: Boolean
    ) -> Array<ProvidedValue<*>> = { emptyArray() },
    val extra: Map<String, Any?> = emptyMap()
) {
    fun isDeepLink(uri: Uri): Boolean {
        return deepLinks
            .filter(String::isNotBlank)
            .any { link ->
                val configuredUri = link.toUri()

                configuredUri.scheme.equals(
                    uri.scheme,
                    ignoreCase = true
                ) &&
                    (
                        configuredUri.host.isNullOrBlank() ||
                            configuredUri.host.equals(
                                uri.host,
                                ignoreCase = true
                            )
                    ) &&
                    (
                        configuredUri.path.isNullOrBlank() ||
                            uri.path.orEmpty().startsWith(
                                configuredUri.path
                                    .orEmpty()
                                    .trimEnd('/')
                            )
                    )
            }
    }

    fun isMyket() = flavor == Flavor.MYKET
    fun isBazaar() = flavor == Flavor.BAZAR
    fun isGooglePlay() = flavor == Flavor.GOOGLE_PLAY
}
```

### Property reference

| Property | Purpose |
|---|---|
| `appName` | Display name used by legal/terms content and host UI |
| `packageName` | Package sent to Appro APIs and used for local database/DataStore naming |
| `flavor` | Selects Bazaar, Myket, or Google Play behavior |
| `paymentRsaKey` | Bazaar/Myket public payment verification key |
| `versionName` / `versionCode` | Host version metadata; `versionCode` is also included in the default Bazaar/Myket developer payload |
| `debug` | Currently controls the built-in `Logger`, HTTP body logging, and supported billing debug behavior |
| `logEnabled` | Present in the configuration API, but the current `Logger` implementation still checks `debug`; changing only `logEnabled` has no effect in this source snapshot |
| `storeLink` | Optional host store-page value; currently stored in the config but not consumed internally by the library |
| `deepLink` | Main base URI used when creating internal links |
| `deepLinks` | URI prefixes accepted as internal links |
| `legalConfig` | Terms, support email, legal sections, and update date |
| `isPaymentAvailable` | Enables payment checks; defaults to RSA key non-empty |
| `defaultLocale` | Initial locale, default `fa-IR` |
| `lightColorSchema` / `darkColorSchema` | Material 3 color schemes |
| `typography` / `shapes` | Material theme typography and shapes |
| `shimmerConfig` | Optional global shimmer colors and duration |
| `defaultThemeMode` | Initial `SYSTEM`, `LIGHT`, or `DARK` mode |
| `providers` | Extra `CompositionLocal` providers based on dark mode |
| `extra` | Host-specific arbitrary configuration values |

### Legal configuration

`appName` is now part of `ApproConfig`, not `LegalConfig`.

```kotlin
val legalConfig = LegalConfig(
    lastUpdated = "2026-07-22",
    supportEmail = "support@example.com",
    customSections = listOf(
        legalSection(
            title = "Application-specific conditions",
            "First condition",
            "Second condition"
        )
    )
)
```

The default terms introduction uses `ApproConfig.appName` automatically.

### Custom theme and CompositionLocals

```kotlin
val config = ApproConfig(
    appName = getString(R.string.app_name),
    packageName = BuildConfig.APPLICATION_ID,
    flavor = Flavor.fromString(BuildConfig.FLAVOR_NAME),
    paymentRsaKey = BuildConfig.MARKET_RSA,
    versionName = BuildConfig.VERSION_NAME,
    versionCode = BuildConfig.VERSION_CODE,
    debug = BuildConfig.DEBUG,
    lightColorSchema = createLightColorScheme(
        Color(0xFF006C4C)
    ),
    darkColorSchema = createDarkColorScheme(
        Color(0xFF60DDAA)
    ),
    typography = AppTypography,
    shapes = AppShapes,
    providers = { isDarkMode ->
        arrayOf(
            LocalAppColors provides if (isDarkMode) {
                DarkAppColors
            } else {
                LightAppColors
            }
        )
    },
    extra = mapOf(
        "support_phone" to "+98...",
        "feature_x_enabled" to true
    )
)
```

Read host-defined values safely:

```kotlin
val enabled = config.extra["feature_x_enabled"] as? Boolean ?: false
```

---

## 6. Application initialization

`Appro.initialize()` performs these actions:

- initializes the library logger;
- initializes `PreferencesHelper`;
- starts Koin;
- registers the built-in configuration, local storage, session, UI, network, and payment modules;
- registers host modules passed through `appModules`;
- initializes `DeepLinkManager` when a parser is supplied.

Do not call `startKoin()` separately.

### 6.1 Complete `Application` example

```kotlin
package com.example.app

import android.app.Application
import android.app.NotificationManager as AndroidNotificationManager
import androidx.core.content.ContextCompat
import com.approagency.base.config.ApproConfig
import com.approagency.base.config.Flavor
import com.approagency.base.di.Appro
import com.approagency.base.di.firebaseModule
import com.approagency.base.firebase.FirebaseConfig
import com.approagency.base.model.ui.LegalConfig
import com.approagency.base.model.ui.notification.NotificationChannelConfig
import com.approagency.base.model.ui.notification.NotificationChannelGroupConfig
import com.approagency.base.theme.ThemeMode
import java.util.Locale

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val approConfig = ApproConfig(
            appName = getString(R.string.app_name),
            packageName = BuildConfig.APPLICATION_ID,
            flavor = Flavor.fromString(
                BuildConfig.FLAVOR_NAME
            ),
            paymentRsaKey = BuildConfig.MARKET_RSA,
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
            debug = BuildConfig.DEBUG,
            logEnabled = BuildConfig.DEBUG,
            storeLink = BuildConfig.STORE_LINK
                .takeIf(String::isNotBlank),
            deepLink = "toolbox://app",
            legalConfig = LegalConfig(
                lastUpdated = "2026-07-22",
                supportEmail = "support@example.com"
            ),
            defaultLocale = Locale.forLanguageTag("fa-IR"),
            defaultThemeMode = ThemeMode.SYSTEM
        )

        val firebaseConfig = FirebaseConfig(
            smallIcon = R.drawable.ic_notification,
            defaultTitle = approConfig.appName,
            notificationColor = ContextCompat.getColor(
                this,
                R.color.notification_color
            ),
            channelGroup = NotificationChannelGroupConfig(
                id = "firebase",
                name = "Firebase notifications",
                description = "Remote application notifications"
            ),
            channel = NotificationChannelConfig(
                id = "firebase_general",
                name = "General notifications",
                description = "General application notifications",
                importance = AndroidNotificationManager.IMPORTANCE_HIGH
            ),
            autoInitEnabled = true,
            showForegroundNotifications = true,
            showBackgroundNotifications = true,
            notificationFilter = { message ->
                message.data["disabled"] != "true"
            },
            onTokenChanged = { token ->
                // Optional host callback. Avoid logging the full token.
                // Example: notify a host repository that registration changed.
            },
            onMessageReceived = { message ->
                // Optional host callback for every accepted logged-in message.
                // Example: update an in-app unread count from message.data.
            }
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

### 6.2 Without Firebase

Remove `firebaseModule(firebaseConfig)`:

```kotlin
Appro.initialize(
    application = this,
    config = approConfig,
    deepLinkParser = AppDeepLinkParser(),
    appModules = listOf(appModule)
)
```

When Firebase is not installed, do not access `BaseActivity.firebaseManager` and do not inject `FirebaseManager` elsewhere.

### 6.3 Without deep links

Use the default blank values and omit the parser:

```kotlin
val config = ApproConfig(
    appName = getString(R.string.app_name),
    packageName = BuildConfig.APPLICATION_ID,
    flavor = Flavor.fromString(BuildConfig.FLAVOR_NAME),
    paymentRsaKey = BuildConfig.MARKET_RSA,
    versionName = BuildConfig.VERSION_NAME,
    versionCode = BuildConfig.VERSION_CODE,
    debug = BuildConfig.DEBUG
)

Appro.initialize(
    application = this,
    config = config,
    appModules = listOf(appModule)
)
```

The parser and deep-link configuration must be supplied together. Passing a parser while leaving the main deep link blank, or configuring deep links without a parser, causes initialization to fail.

### 6.4 Host Koin module

```kotlin
package com.example.app.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single<AppRepository> {
        AppRepositoryImpl(
            service = get(),
            database = get()
        )
    }

    viewModelOf(::HomeViewModel)
    viewModelOf(::SettingsViewModel)
}
```

Every module passed through `appModules` is installed in the same Koin application as the base modules.

---

## 7. AndroidManifest setup

The library AAR contributes:

- `INTERNET` permission;
- `POST_NOTIFICATIONS` permission;
- store package visibility queries;
- the `ApproFirebaseMessagingService` service;
- Firebase Messaging auto-init disabled by default.

The host must register its `Application` and main activity. Add a deep-link intent filter only when deep links are enabled.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".App"
        android:allowBackup="true"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.App">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop">

            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />

                <data
                    android:scheme="toolbox"
                    android:host="app" />
            </intent-filter>

            <intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />

                <data
                    android:scheme="https"
                    android:host="example.com"
                    android:pathPrefix="/app" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

`android:launchMode="singleTop"` is important because warm-start deep links and notification clicks are forwarded through `BaseActivity.onNewIntent()`.

---

## 8. Deep-link setup

The deep-link system separates parsing from navigation:

```text
Intent or String
    ↓
DeepLinkInput
    ↓
DeepLinkParser
    ↓
DeepLinkTarget
    ↓
DeepLinkNavigationHandler
    ↓
NavController
```

### 8.1 Define routes

Example with typed Compose Navigation routes:

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data class ProductRoute(
    val id: String
)

@Serializable
data object SettingsRoute
```

### 8.2 Implement `DeepLinkParser`

```kotlin
import com.approagency.base.model.ui.deepLink.DeepLinkInput
import com.approagency.base.model.ui.deepLink.DeepLinkNavigationType
import com.approagency.base.model.ui.deepLink.DeepLinkParser
import com.approagency.base.model.ui.deepLink.DeepLinkTarget

class AppDeepLinkParser : DeepLinkParser {
    override fun parse(
        input: DeepLinkInput
    ): DeepLinkTarget? {
        val segments = input.uri.pathSegments

        return when {
            segments.isEmpty() || segments.first() == "home" -> {
                DeepLinkTarget(
                    route = HomeRoute,
                    navigationType =
                        DeepLinkNavigationType.CLEAR_STACK
                )
            }

            segments.firstOrNull() == "product" -> {
                val productId = segments.getOrNull(1)
                    ?: input.data["product_id"]
                    ?: return null

                DeepLinkTarget(
                    route = ProductRoute(productId),
                    navigationType =
                        DeepLinkNavigationType.PUSH
                )
            }

            segments.firstOrNull() == "settings" -> {
                DeepLinkTarget(
                    route = SettingsRoute,
                    navigationType =
                        DeepLinkNavigationType.SINGLE_TOP
                )
            }

            else -> null
        }
    }
}
```

Navigation types:

| Type | Behavior |
|---|---|
| `PUSH` | Normal navigation |
| `SINGLE_TOP` | Uses `launchSingleTop = true` |
| `CLEAR_STACK` | Clears to the graph start destination and navigates |

### 8.3 Render the navigation handler

Place it beside the `NavHost`:

```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    DeepLinkNavigationHandler(
        navController = navController
    )

    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeScreen()
        }

        composable<ProductRoute> { entry ->
            val route = entry.toRoute<ProductRoute>()
            ProductScreen(route.id)
        }

        composable<SettingsRoute> {
            SettingsScreen()
        }
    }
}
```

### 8.4 Create links

```kotlin
val link = DeepLinkManager.createLink(
    route = "product",
    pathParameters = listOf(productId),
    queryParameters = mapOf(
        "source" to "share"
    )
)
```

Example result:

```text
toolbox://app/product/25?source=share
```

Create a `PendingIntent` for a notification:

```kotlin
val pendingIntent = DeepLinkManager.createDeepLinkIntent(
    context = context,
    route = "product",
    pathParameters = listOf(productId)
)
```

### 8.5 Handle a link manually

```kotlin
val handled = deepLinkManager.handle(
    link = "toolbox://app/product/25",
    data = mapOf("source" to "manual")
)
```

`ApproConfig.isDeepLink(uri)` compares configured scheme, optional host, and optional path prefix.

---

## 9. BaseActivity implementation

Use `BaseActivity` for the main Compose activity:

```kotlin
class MainActivity : BaseActivity() {
    @Composable
    override fun CreateView() {
        AppRoot()
    }
}
```

### 9.1 What `BaseActivity` provides

Public properties:

```kotlin
val drawerState: DrawerState
val approViewModel: ApproViewModel
val sessionManager: SessionManager
val themeManager: ThemeManager
val config: ApproConfig
val deepLinkManager: DeepLinkManager
val firebaseManager: FirebaseManager
val notificationManager: NotificationManager
var language: String
lateinit var snackBarHostState: SnackbarHostState
```

Protected sheet state:

```kotlin
protected var showSubscriptionBottomSheet: Boolean
protected var showTermsBottomSheet: Boolean
```

Helpers:

```kotlin
openDrawer()
closeDrawer()
showSubscriptionBottomSheet()
hideSubscriptionBottomSheet()
showTermsBottomSheet()
hideTermsBottomSheet()
showSnackBar(...)
startOtpAutofill()
stopOtpAutofill()
```

Automatic behavior:

- installs Android splash screen;
- enables edge-to-edge mode;
- wraps `CreateView()` in `ApproTheme`;
- provides `LocalBaseActivity`;
- applies persisted locale and RTL/LTR direction;
- forwards `onCreate()` and `onNewIntent()` intents to `DeepLinkManager`;
- integrates SMS User Consent OTP autofill;
- exposes one activity-scoped `ApproViewModel`.

### 9.2 Important: render the provided UI state

`BaseActivity` owns the snackbar and sheet state, but it does not automatically render the snackbar host, subscription sheet, terms sheet, drawer, or navigation graph. The host must render them inside `CreateView()`.

### 9.3 Complete `MainActivity` example

```kotlin
class MainActivity : BaseActivity() {

    @Composable
    override fun CreateView() {
        val navController = rememberNavController()
        val approState by approViewModel.state

        // SessionManager starts in Loading. Trigger this once so the
        // saved session becomes Login or Logout.
        LaunchedEffect(Unit) {
            approViewModel.setEvent(
                ApproContract.Event.CheckStatus
            )

            approViewModel.setEvent(
                ApproContract.Event.FetchPromotions
            )
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    DrawerContent(
                        items = buildDrawerItems(navController),
                        header = {
                            Text(
                                text = config.appName,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    )
                }
            }
        ) {
            Scaffold(
                snackbarHost = {
                    ApproSnackBarHost(
                        hostState = snackBarHostState
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier.padding(paddingValues)
                ) {
                    DeepLinkNavigationHandler(
                        navController = navController
                    )

                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute
                    ) {
                        composable<HomeRoute> {
                            HomeScreen(
                                promotionsState =
                                    approState.promotions,
                                onSubscribe =
                                    ::showSubscriptionBottomSheet
                            )
                        }

                        composable<SettingsRoute> {
                            SettingsScreen()
                        }
                    }
                }
            }
        }

        if (showSubscriptionBottomSheet) {
            SubscriptionBottomSheet(
                approViewModel = approViewModel,
                onRulesClick = ::showTermsBottomSheet
            )
        }

        if (showTermsBottomSheet) {
            TermsBottomSheet()
        }
    }
}
```

The sheet components call the matching `hide...()` activity helper when dismissed.

### 9.4 Activity-scoped `ApproViewModel`

Use the instance supplied by `BaseActivity`:

```kotlin
val approViewModel: ApproViewModel by viewModel()
```

Pass that instance to child composables when they must share the same authentication, products, purchase, status, and promotion state. Calling `koinViewModel()` in an individual navigation destination may create or retrieve a ViewModel scoped to a different `ViewModelStoreOwner`.

### 9.5 Show a snackbar

```kotlin
showSnackBar(
    message = "Saved successfully",
    type = SnackBarType.SUCCESS
)
```

With an action:

```kotlin
showSnackBar(
    message = "Item deleted",
    type = SnackBarType.WARNING,
    actionLabel = "Undo",
    onActionClick = viewModel::undoDelete
)
```

### 9.6 Change language

```kotlin
PreferencesHelper.write(
    PreferencesHelper.Keys.LANGUAGE,
    "en"
)

recreate()
```

`attachBaseContext()` reads the stored language during recreation.

---

## 10. PreferencesHelper

`PreferencesHelper` is a singleton backed by DataStore Preferences.

`Appro.initialize()` initializes it automatically using:

```text
<packageName>.ds
```

Do not call `PreferencesHelper.initialize()` manually when using `Appro.initialize()`.

### 10.1 Built-in keys

```kotlin
PreferencesHelper.Keys.THEME_MODE
PreferencesHelper.Keys.LANGUAGE
```

### 10.2 Define custom keys

```kotlin
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object AppPreferenceKeys {
    val ONBOARDING_FINISHED =
        booleanPreferencesKey("ONBOARDING_FINISHED")

    val HOME_OPEN_COUNT =
        intPreferencesKey("HOME_OPEN_COUNT")

    val USER_FILTER =
        stringPreferencesKey("USER_FILTER")
}
```

### 10.3 Write a value

```kotlin
PreferencesHelper.write(
    AppPreferenceKeys.ONBOARDING_FINISHED,
    true
)
```

### 10.4 Read a nullable value

```kotlin
val savedFilter: String? = PreferencesHelper.read(
    AppPreferenceKeys.USER_FILTER
)
```

### 10.5 Read with a default value

```kotlin
val onboardingFinished = PreferencesHelper.read(
    AppPreferenceKeys.ONBOARDING_FINISHED,
    false
)
```

### 10.6 Observe as a Flow

```kotlin
val onboardingFlow: Flow<Boolean> =
    PreferencesHelper.readFlow(
        AppPreferenceKeys.ONBOARDING_FINISHED,
        false
    )
```

In Compose:

```kotlin
val onboardingFinished by PreferencesHelper
    .readFlow(
        AppPreferenceKeys.ONBOARDING_FINISHED,
        false
    )
    .collectAsStateWithLifecycle(initialValue = false)
```

### 10.7 Remove a value

```kotlin
viewModelScope.launch {
    PreferencesHelper.remove(
        AppPreferenceKeys.USER_FILTER
    )
}
```

### 10.8 Usage note

The current `write()` and synchronous `read()` methods internally use `runBlocking`. They are convenient for startup and small settings operations, but should not be called repeatedly during recomposition or inside a high-frequency loop. Prefer `readFlow()` for continuously observed UI state.

---

## 11. SessionManager

`SessionManager` stores the Appro session in the library Room database and exposes:

```kotlin
val state: StateFlow<SessionState>
```

States:

```kotlin
SessionState.Loading
SessionState.Logout
SessionState.Login(session)
```

The session model includes:

```kotlin
data class Session(
    val id: String = Session.ID,
    val approToken: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val tokenType: String? = null,
    val expiresAt: Long? = null,
    val userId: String? = null,
    val phoneNumber: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val isPremium: Boolean = false
)
```

### 11.1 Observe session state in Compose

```kotlin
val sessionState by sessionManager.state
    .collectAsStateWithLifecycle()

when (val state = sessionState) {
    SessionState.Loading -> Loading()
    SessionState.Logout -> LoginRequiredContent()
    is SessionState.Login -> UserContent(
        session = state.session
    )
}
```

### 11.2 Initialize the session state

`SessionManager` starts in `Loading`. Dispatch the built-in status check once at startup:

```kotlin
LaunchedEffect(Unit) {
    approViewModel.setEvent(
        ApproContract.Event.CheckStatus
    )
}
```

This reads the saved session, verifies status with the Appro backend, then changes `SessionManager.state` to `Login` or `Logout`.

### 11.3 Read the saved session

```kotlin
viewModelScope.launch {
    val session = sessionManager.getSession()
}
```

### 11.4 Store a session manually

```kotlin
viewModelScope.launch {
    sessionManager.login(
        Session(
            accessToken = accessToken,
            approToken = approToken,
            phoneNumber = phoneNumber,
            firstName = firstName,
            lastName = lastName
        )
    )
}
```

### 11.5 Logout

```kotlin
viewModelScope.launch {
    sessionManager.logout()
}
```

Or use the built-in ViewModel event:

```kotlin
approViewModel.setEvent(
    ApproContract.Event.Logout
)
```

### 11.6 Update tokens

```kotlin
viewModelScope.launch {
    sessionManager.updateTokens(
        approToken = newApproToken,
        accessToken = newAccessToken,
        refreshToken = newRefreshToken,
        expiresAt = newExpiresAt
    )
}
```

### 11.7 Update user profile fields

```kotlin
viewModelScope.launch {
    sessionManager.updateUser(
        userId = user.id,
        phoneNumber = user.phoneNumber,
        firstName = user.firstName,
        lastName = user.lastName
    )
}
```

### 11.8 Premium state

```kotlin
val premium = sessionManager.isPremium
```

In Compose:

```kotlin
val isPremium = rememberIsUserPremium()
```

Outside Compose:

```kotlin
val isPremium = isUserPremium()
```

`isPremium` is based on the current `SessionState.Login` value.

---

## 12. Firebase and FirebaseManager

Firebase Messaging is included as a runtime dependency, but the host must provide Firebase project configuration.

### 12.1 Host Firebase setup

1. Register the Android package in Firebase Console.
2. Download `google-services.json`.
3. Place it in:

```text
app/google-services.json
```

For flavor-specific Firebase projects, use:

```text
app/src/bazar/google-services.json
app/src/myket/google-services.json
app/src/googlePlay/google-services.json
```

4. Apply the Google Services plugin in the host app module.

Example version catalog entry:

```toml
[plugins]
google-services = { id = "com.google.gms.google-services", version = "<google-services-plugin-version>" }
```

```kotlin
plugins {
    alias(libs.plugins.google.services)
}
```

5. Add `firebaseModule(firebaseConfig)` to `Appro.initialize()`.

### 12.2 FirebaseConfig reference

```kotlin
data class FirebaseConfig(
    @DrawableRes val smallIcon: Int,
    val channelGroup: NotificationChannelGroupConfig,
    val channel: NotificationChannelConfig,
    @ColorInt val notificationColor: Int? = null,
    val defaultTitle: String? = null,
    val autoInitEnabled: Boolean = true,
    val showForegroundNotifications: Boolean = true,
    val showBackgroundNotifications: Boolean = true,
    val notificationFilter: (FirebaseMessage) -> Boolean = { true },
    val onTokenChanged: suspend (String) -> Unit = {},
    val onMessageReceived: suspend (FirebaseMessage) -> Unit = {}
)
```

Use a valid monochrome notification icon for `smallIcon`.

### 12.3 Login-controlled FCM lifecycle

The current `FirebaseManager` intentionally ties FCM to the Appro session:

```text
SessionState.Login
    -> enable Firebase Messaging auto-init
    -> obtain token
    -> submit token to Appro backend
    -> call onTokenChanged

SessionState.Logout
    -> disable auto-init
    -> clear manager token state
    -> delete local FCM token
```

Therefore:

- a user must be logged in before `getToken()`;
- topic subscription requires a logged-in session;
- received messages are ignored while logged out;
- manual notifications through `FirebaseManager` are ignored while logged out.

### 12.4 FirebaseManager API

Observable values:

```kotlin
val token: StateFlow<String?>
val messages: SharedFlow<FirebaseMessage>
```

Main functions:

```kotlin
initialize(): Boolean
suspend fun getToken(): String
fun refreshToken()
suspend fun deleteToken()
suspend fun subscribeToTopic(topic: String)
suspend fun unsubscribeFromTopic(topic: String)
fun requestNotificationPermission(activity: Activity)
fun hasNotificationPermission(): Boolean
fun openNotificationSettings()
fun showNotification(message: FirebaseMessage)
fun close()
```

### 12.5 Inject FirebaseManager

In a Koin-managed class:

```kotlin
class NotificationsViewModel(
    private val firebaseManager: FirebaseManager
) : ViewModel()
```

Koin module:

```kotlin
val appModule = module {
    viewModelOf(::NotificationsViewModel)
}
```

In `BaseActivity`, call the suspend API from a coroutine:

```kotlin
lifecycleScope.launch {
    val token = firebaseManager.getToken()
    // Use the token only when the host has an additional requirement.
}
```

The activity property resolves successfully only when the Firebase module was registered.

### 12.6 Observe the token in Compose

```kotlin
val token by firebaseManager.token
    .collectAsStateWithLifecycle()

Text(token ?: "No FCM token")
```

### 12.7 Observe messages

```kotlin
LaunchedEffect(firebaseManager) {
    firebaseManager.messages.collect { message ->
        when (message.data["type"]) {
            "chat" -> openChat(message.data["chat_id"])
            "promotion" -> openPromotion()
        }
    }
}
```

`FirebaseMessage` contains:

```kotlin
data class FirebaseMessage(
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val data: Map<String, String> = emptyMap()
)
```

### 12.8 Get or refresh the token

```kotlin
viewModelScope.launch {
    runCatching {
        firebaseManager.getToken()
    }.onSuccess { token ->
        // Token was also submitted by FirebaseManager.
    }.onFailure { throwable ->
        // User may be logged out or Firebase may be disabled.
    }
}
```

Refresh asynchronously:

```kotlin
firebaseManager.refreshToken()
```

### 12.9 Delete the token

```kotlin
viewModelScope.launch {
    firebaseManager.deleteToken()
}
```

Normal app logout already changes `SessionManager` to `Logout`, which causes automatic token deletion.

### 12.10 Topics

```kotlin
viewModelScope.launch {
    firebaseManager.subscribeToTopic("news")
}
```

```kotlin
viewModelScope.launch {
    firebaseManager.unsubscribeFromTopic("news")
}
```

### 12.11 Notification permission through FirebaseManager

```kotlin
if (!firebaseManager.hasNotificationPermission()) {
    firebaseManager.requestNotificationPermission(this)
}
```

Open system settings:

```kotlin
firebaseManager.openNotificationSettings()
```

### 12.12 Show a Firebase-style local notification

```kotlin
firebaseManager.showNotification(
    FirebaseMessage(
        title = "Download completed",
        description = "Your file is ready.",
        data = mapOf(
            "type" to "download",
            "id" to downloadId
        )
    )
)
```

### 12.13 Notification click deep links

When an FCM message contains a data entry named by the library's Firebase link constant, `FirebaseManager` places that link in the launch intent. `BaseActivity` receives the intent and forwards it to `DeepLinkManager`.

Use `singleTop` launch mode and render `DeepLinkNavigationHandler` so the full click flow works.

---

## 13. NotificationManager

The library class is:

```kotlin
com.approagency.base.utils.NotificationManager
```

It can conflict by name with Android's `android.app.NotificationManager`. Use import aliases when both are needed:

```kotlin
import android.app.NotificationManager as AndroidNotificationManager
import com.approagency.base.utils.NotificationManager as ApproNotificationManager
```

### 13.1 Inject it

```kotlin
class LocalNotificationRepository(
    private val notificationManager: ApproNotificationManager
)
```

Or use the property from `BaseActivity`:

```kotlin
notificationManager
```

### 13.2 Permission API

```kotlin
notificationManager.isRequired()
notificationManager.hasPermission()
notificationManager.areNotificationsEnabled()
notificationManager.shouldShowPermissionRationale(activity)
notificationManager.requestPermission(activity)
```

Open application notification settings on Android 8+:

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    notificationManager.openNotificationSettings(context)
}
```

### 13.3 Create a channel group and channel

```kotlin
notificationManager.createChannelGroup(
    NotificationChannelGroupConfig(
        id = "downloads",
        name = "Downloads",
        description = "Download notifications"
    )
)

notificationManager.createChannel(
    NotificationChannelConfig(
        id = "downloads_progress",
        name = "Download progress",
        description = "Progress and completion notifications",
        importance =
            AndroidNotificationManager.IMPORTANCE_LOW,
        groupId = "downloads",
        showBadge = false,
        enableVibration = false
    )
)
```

Batch creation:

```kotlin
notificationManager.createChannelGroups(groups)
notificationManager.createChannels(channels)
```

Delete channels:

```kotlin
notificationManager.deleteChannel("downloads_progress")
notificationManager.deleteChannelGroup("downloads")
```

### 13.4 Show a local notification

```kotlin
val notificationId = notificationManager.show(
    NotificationRequest(
        channelId = "downloads_progress",
        smallIcon = R.drawable.ic_notification,
        title = "Download completed",
        text = "report.pdf is ready",
        priority = NotificationCompat.PRIORITY_DEFAULT,
        autoCancel = true
    )
)
```

`show()` returns the used notification ID, or `null` when permission is missing.

### 13.5 Notification with a deep-link click

```kotlin
val contentIntent = DeepLinkManager.createDeepLinkIntent(
    context = context,
    route = "download",
    pathParameters = listOf(downloadId)
)

notificationManager.show(
    NotificationRequest(
        channelId = "downloads_progress",
        smallIcon = R.drawable.ic_notification,
        title = "Download completed",
        text = "Tap to open",
        contentIntent = contentIntent
    )
)
```

### 13.6 Progress notification

```kotlin
notificationManager.show(
    NotificationRequest(
        id = DOWNLOAD_NOTIFICATION_ID,
        channelId = "downloads_progress",
        smallIcon = R.drawable.ic_notification,
        title = "Downloading",
        text = "$progress%",
        ongoing = progress < 100,
        onlyAlertOnce = true,
        progressMax = 100,
        progress = progress,
        progressIndeterminate = false
    )
)
```

Update using the same required ID:

```kotlin
notificationManager.update(
    request.copy(
        id = DOWNLOAD_NOTIFICATION_ID,
        progress = newProgress
    )
)
```

### 13.7 Custom builder options

`NotificationRequest` supports actions, people, styles, intents, large icons, progress, grouping, custom views, visibility, sound, vibration, and a builder lambda:

```kotlin
notificationManager.show(
    NotificationRequest(
        channelId = "general",
        smallIcon = R.drawable.ic_notification,
        title = "New message",
        text = message,
        builder = {
            setCategory(NotificationCompat.CATEGORY_MESSAGE)
            setPriority(NotificationCompat.PRIORITY_HIGH)
        }
    )
)
```

### 13.8 Cancel notifications

```kotlin
notificationManager.cancel(notificationId)
notificationManager.cancel(tag, notificationId)
notificationManager.cancelAll()
```

### 13.9 No-access notification helper

```kotlin
notificationManager.showNoAccessNotification(
    context = context,
    channel = "general",
    route = "subscription",
    title = "Subscription required",
    content = "Tap to purchase access",
    smallIcon = R.drawable.ic_notification
)
```

---

## 14. Theme and localization

### 14.1 ThemeManager

`ThemeManager` exposes:

```kotlin
val themeMode: State<ThemeMode>
suspend fun setThemeMode(mode: ThemeMode)
fun getDefaultMode(): ThemeMode
@Composable fun isDarkMode(): Boolean
```

Supported modes:

```kotlin
ThemeMode.SYSTEM
ThemeMode.LIGHT
ThemeMode.DARK
```

### 14.2 Observe and change the theme

```kotlin
val themeManager: ThemeManager = koinInject()
val mode = themeManager.themeMode.value
val scope = rememberCoroutineScope()

ThemeModeSelector(
    selectedMode = mode,
    onModeSelected = { selected ->
        scope.launch {
            themeManager.setThemeMode(selected)
        }
    }
)
```

The value is stored through `PreferencesHelper.Keys.THEME_MODE`.

### 14.3 LocalBaseActivity

Inside content rendered by `BaseActivity.ApproTheme`:

```kotlin
val activity = LocalBaseActivity.current
```

Use it to access:

```kotlin
activity.config
activity.sessionManager
activity.themeManager
activity.approViewModel
activity.notificationManager
activity.drawerState
activity.showSnackBar(...)
activity.showSubscriptionBottomSheet()
```

### 14.4 Localization

The initial locale comes from:

```kotlin
ApproConfig.defaultLocale
```

The selected language is persisted under:

```kotlin
PreferencesHelper.Keys.LANGUAGE
```

Change it and recreate the activity:

```kotlin
PreferencesHelper.write(
    PreferencesHelper.Keys.LANGUAGE,
    "fa"
)

activity.recreate()
```

`ApproTheme` selects RTL or LTR from the active locale.

---

## 15. BaseViewModel and MVI

The library provides:

```kotlin
BaseViewModel<Event, State, SideEffect>
BaseApplicationViewModel<Event, State, SideEffect>
```

Architecture:

```text
UI action
    ↓
Event
    ↓
ViewModel.onTriggerEvent()
    ↓
setState { copy(...) }
    ↓
Compose recomposition
```

One-time UI actions:

```text
ViewModel
    ↓
setEffect { ... }
    ↓
Effect Flow
    ↓
Snackbar / Navigation / Dialog
```

`BaseViewModel` exposes:

```kotlin
val state: State<UiState>
val event: SharedFlow<Event>
val effect: Flow<Effect>
fun setEvent(event: Event)
protected fun setState(reduce: UiState.() -> UiState)
protected fun setEffect(builder: () -> Effect)
```

The state is Compose `State`, not `StateFlow`:

```kotlin
val state by viewModel.state
```

Do not call `collectAsStateWithLifecycle()` on `BaseViewModel.state`.

### 15.1 Complete MVI example

#### Contract

```kotlin
package com.example.counter

import com.approagency.base.presentation.ViewEvent
import com.approagency.base.presentation.ViewSideEffect
import com.approagency.base.presentation.ViewState

class CounterContract {

    sealed interface Event : ViewEvent {
        data object Increment : Event
        data object Decrement : Event
        data object Reset : Event
        data object Load : Event
    }

    data class State(
        val count: Int = 0,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : ViewState

    sealed interface SideEffect : ViewSideEffect {
        data class ShowMessage(
            val message: String
        ) : SideEffect

        data object NavigateBack : SideEffect
    }
}
```

#### ViewModel

```kotlin
package com.example.counter

import androidx.lifecycle.viewModelScope
import com.approagency.base.presentation.BaseViewModel
import kotlinx.coroutines.launch

class CounterViewModel(
    private val repository: CounterRepository
) : BaseViewModel<
    CounterContract.Event,
    CounterContract.State,
    CounterContract.SideEffect
>() {

    override fun setInitialState(): CounterContract.State {
        return CounterContract.State()
    }

    override fun onTriggerEvent(
        event: CounterContract.Event
    ) {
        when (event) {
            CounterContract.Event.Increment -> increment()
            CounterContract.Event.Decrement -> decrement()
            CounterContract.Event.Reset -> reset()
            CounterContract.Event.Load -> load()
        }
    }

    private fun increment() {
        setState {
            copy(count = count + 1)
        }
    }

    private fun decrement() {
        setState {
            copy(count = count - 1)
        }
    }

    private fun reset() {
        setState {
            copy(count = 0)
        }

        setEffect {
            CounterContract.SideEffect.ShowMessage(
                "Counter reset"
            )
        }
    }

    private fun load() {
        viewModelScope.launch {
            setState {
                copy(
                    isLoading = true,
                    error = null
                )
            }

            runCatching {
                repository.loadCount()
            }.onSuccess { value ->
                setState {
                    copy(
                        count = value,
                        isLoading = false
                    )
                }
            }.onFailure { throwable ->
                setState {
                    copy(
                        isLoading = false,
                        error = throwable.message
                    )
                }

                setEffect {
                    CounterContract.SideEffect.ShowMessage(
                        "Failed to load counter"
                    )
                }
            }
        }
    }
}
```

#### Koin module

```kotlin
val counterModule = module {
    single<CounterRepository> {
        CounterRepositoryImpl()
    }

    viewModelOf(::CounterViewModel)
}
```

Pass it to `Appro.initialize()` through `appModules`.

#### Compose route

```kotlin
@Composable
fun CounterRoute(
    viewModel: CounterViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CounterContract.SideEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        effect.message
                    )
                }

                CounterContract.SideEffect.NavigateBack -> {
                    onBack()
                }
            }
        }
    }

    CounterScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onIncrement = {
            viewModel.setEvent(
                CounterContract.Event.Increment
            )
        },
        onDecrement = {
            viewModel.setEvent(
                CounterContract.Event.Decrement
            )
        },
        onReset = {
            viewModel.setEvent(
                CounterContract.Event.Reset
            )
        },
        onLoad = {
            viewModel.setEvent(
                CounterContract.Event.Load
            )
        }
    )
}
```

#### Stateless screen

```kotlin
@Composable
fun CounterScreen(
    state: CounterContract.State,
    snackbarHostState: SnackbarHostState,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    onLoad: () -> Unit
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {
            Text("Count: ${state.count}")

            if (state.isLoading) {
                Loading()
            }

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                FilledTextButton(
                    text = "-",
                    onClick = onDecrement
                )

                FilledTextButton(
                    text = "+",
                    onClick = onIncrement
                )
            }

            OutlinedTextButton(
                text = "Reset",
                onClick = onReset
            )

            LoadingButton(
                text = "Load",
                isLoading = state.isLoading,
                onClick = onLoad
            )
        }
    }
}
```

### 15.2 BaseApplicationViewModel

Use `BaseApplicationViewModel` only when the ViewModel truly needs an `Application` instance:

```kotlin
class FilesViewModel(
    application: Application
) : BaseApplicationViewModel<
    FilesContract.Event,
    FilesContract.State,
    FilesContract.SideEffect
>(application) {
    // Same event/state/effect API.
}
```

Prefer `BaseViewModel` with injected abstractions in most features.

---

## 16. ApproViewModel and ApproContract

`ApproViewModel` is the built-in MVI ViewModel for authentication, session status, subscription products, purchases, FCM token submission, logout, and promotions.

### 16.1 Events

Current events:

```kotlin
ApproContract.Event.CheckPhoneNumber(phoneNumber)
ApproContract.Event.CheckStatus
ApproContract.Event.CheckOtp(phoneNumber, otp, sessionId)
ApproContract.Event.OnOtpChanged(otp)
ApproContract.Event.OnPhoneNumberChanged(phoneNumber)
ApproContract.Event.GetProducts
ApproContract.Event.Purchase(activity, paymentRequest)
ApproContract.Event.ResetLoginState
ApproContract.Event.EditPhoneNumber
ApproContract.Event.ResetPurchaseState
ApproContract.Event.SendFCMToken(token)
ApproContract.Event.Logout
ApproContract.Event.FetchPromotions
```

### 16.2 State

```kotlin
data class State(
    val step: AuthStep = AuthStep.Phone,
    val phoneNumber: String = "",
    val otp: String = "",
    val loginState: UiState<Unit> = UiState.Idle(),
    val otpState: UiState<Session> = UiState.Idle(),
    val statusState: UiState<UserStatus> = UiState.Loading(),
    val productsState: UiState<List<Product>> = UiState.Loading(),
    val purchaseState: UiState<String> = UiState.Idle(),
    val promotions: UiState<List<Promotion>> = UiState.Idle()
)
```

`ApproContract.SideEffect` currently has no concrete effect types. Observe the state fields for built-in flows.

### 16.3 Observe state

```kotlin
val state by approViewModel.state
```

### 16.4 Check saved session and subscription status

```kotlin
LaunchedEffect(Unit) {
    approViewModel.setEvent(
        ApproContract.Event.CheckStatus
    )
}
```

This operation:

1. loads the saved Room session;
2. verifies required tokens;
3. requests status from the Appro backend;
4. updates `statusState`;
5. updates `Session.isPremium`;
6. sets `SessionManager.state` to `Login`;
7. logs out on unauthorized/forbidden or missing session.

### 16.5 Send login OTP

```kotlin
approViewModel.setEvent(
    ApproContract.Event.CheckPhoneNumber(
        phoneNumber = "09123456789"
    )
)
```

On success, `state.step` changes from `AuthStep.Phone` to `AuthStep.Otp`.

### 16.6 Update fields

```kotlin
approViewModel.setEvent(
    ApproContract.Event.OnPhoneNumberChanged(
        "09123456789"
    )
)
```

```kotlin
approViewModel.setEvent(
    ApproContract.Event.OnOtpChanged(
        "12345"
    )
)
```

### 16.7 Verify OTP

```kotlin
approViewModel.setEvent(
    ApproContract.Event.CheckOtp(
        phoneNumber = state.phoneNumber.trim(),
        otp = state.otp.trim(),
        sessionId = Session.ID
    )
)
```

On success, the ViewModel creates and stores a `Session`, then calls `CheckStatus`.

### 16.8 Load products

```kotlin
approViewModel.setEvent(
    ApproContract.Event.GetProducts
)
```

Observe:

```kotlin
when (val products = state.productsState) {
    is UiState.Loading -> Loading()
    is UiState.Error -> ErrorLayout(
        message = products.error.text,
        onRetry = {
            approViewModel.setEvent(
                ApproContract.Event.GetProducts
            )
        }
    )
    is UiState.Success -> Products(products.data)
    is UiState.Idle -> Unit
}
```

### 16.9 Purchase

```kotlin
approViewModel.setEvent(
    ApproContract.Event.Purchase(
        activity = activity,
        paymentRequest = product.toPaymentRequest()
    )
)
```

On successful purchase, the ViewModel checks status again.

### 16.10 Promotions

```kotlin
approViewModel.setEvent(
    ApproContract.Event.FetchPromotions
)
```

```kotlin
PromotionSliderState(
    state = state.promotions,
    onRetry = {
        approViewModel.setEvent(
            ApproContract.Event.FetchPromotions
        )
    }
)
```

### 16.11 Logout and reset

```kotlin
approViewModel.setEvent(
    ApproContract.Event.Logout
)
```

```kotlin
approViewModel.setEvent(
    ApproContract.Event.ResetLoginState
)
```

```kotlin
approViewModel.setEvent(
    ApproContract.Event.ResetPurchaseState
)
```

---

## 17. Subscription and terms flow

The library includes a ready-to-use subscription UI:

```kotlin
SubscriptionBottomSheet
```

It combines:

- phone number input;
- OTP sending;
- SMS User Consent autofill;
- OTP verification;
- session creation;
- product loading;
- product selection;
- Bazaar/Myket purchase;
- purchase result snackbar;
- subscription status refresh.

### 17.1 Required startup action

Because `SessionManager.state` begins as `Loading`, call:

```kotlin
LaunchedEffect(Unit) {
    approViewModel.setEvent(
        ApproContract.Event.CheckStatus
    )
}
```

Without this, the subscription sheet may remain on the `SessionState.Loading` branch.

### 17.2 Show the built-in sheet from BaseActivity

```kotlin
FilledTextButton(
    text = "Buy subscription",
    onClick = activity::showSubscriptionBottomSheet
)
```

Render it in `BaseActivity.CreateView()`:

```kotlin
if (showSubscriptionBottomSheet) {
    SubscriptionBottomSheet(
        approViewModel = approViewModel,
        onRulesClick = ::showTermsBottomSheet,
        sessionId = Session.ID,
        otpCount = 5
    )
}
```

### 17.3 Render terms

```kotlin
if (showTermsBottomSheet) {
    TermsBottomSheet()
}
```

The terms sheet reads:

```kotlin
ApproConfig.legalConfig
ApproConfig.appName
```

### 17.4 Customize subscription text

```kotlin
val subscriptionText = SubscriptionBottomSheetText(
    loginTitle = "Sign in",
    subscriptionTitle = "Choose a plan",
    enterPhone = "Enter your phone number",
    phoneLabel = "Phone",
    sendCode = "Send code",
    login = "Verify",
    editPhone = "Edit number",
    rules = "Terms and conditions",
    otpPrefix = "Enter the code sent to ",
    otpSuffix = ".",
    retry = "Retry",
    purchase = "Purchase",
    selected = "Selected",
    currency = "Toman",
    emptyProducts = "No products found"
)

SubscriptionBottomSheet(
    text = subscriptionText,
    onRulesClick = activity::showTermsBottomSheet
)
```

### 17.5 Use lower-level subscription components

The following are public when a host needs a custom sheet shell:

```kotlin
LoginSheetContent
ProductsSheetContent
ProductItem
```

Normally, use `SubscriptionBottomSheet` because it connects these sections correctly to `ApproViewModel`, `SessionManager`, OTP autofill, and payment state.

### 17.6 OTP autofill behavior

`BaseActivity` implements `OtpAutofillController` using SMS User Consent. When the OTP step becomes active, `LoginSheetContent` starts listening. A five-digit code matching this pattern is extracted:

```regex
\b\d{5}\b
```

Use `otpCount = 5` unless the backend and SMS format are changed together.

---

## 18. Payments

The payment API is:

```kotlin
interface PaymentService {
    fun purchase(
        activity: ComponentActivity,
        request: PaymentRequest
    ): Flow<Resource<String>>
}
```

Request model:

```kotlin
data class PaymentRequest(
    val productId: Int,
    val productUuid: String,
    val type: PaymentProductType =
        PaymentProductType.SUBSCRIPTION,
    val sessionId: String = Session.ID,
    val payload: String? = null
)
```

### 18.1 Bazaar

The Bazaar implementation:

- checks `ApproConfig.isPaymentAvailable`;
- verifies Bazaar is installed;
- requires a saved session;
- rejects purchase when already premium;
- validates the Poolakey purchase payload;
- sends the purchase token and `cafe` gateway to the Appro backend.

### 18.2 Myket

The Myket implementation:

- checks `ApproConfig.isPaymentAvailable`;
- verifies Myket is installed;
- requires a saved session;
- rejects purchase when already premium;
- validates the Myket developer payload;
- sends the purchase token and `myket` gateway to the Appro backend.

### 18.3 Google Play limitation

The current Google Play `MarketPaymentService` is a placeholder and throws:

```kotlin
Failure.StoreUnavailable
```

Google Play billing is not implemented in the current library source. The empty Google Play RSA key also makes the default `isPaymentAvailable` false.

### 18.4 Direct PaymentService usage

```kotlin
class PurchaseViewModel(
    private val paymentService: PaymentService
) : ViewModel() {

    fun purchase(
        activity: ComponentActivity,
        request: PaymentRequest
    ) {
        viewModelScope.launch {
            paymentService.purchase(
                activity,
                request
            ).collect { result ->
                // Handle Resource.Loading/Error/Success.
            }
        }
    }
}
```

Most applications should use `ApproViewModel` or `SubscriptionBottomSheet` instead.

---

## 19. Networking

The library installs named public and private clients:

```kotlin
ApproConstants.APPRO_PUBLIC_OKHTTP
ApproConstants.APPRO_PRIVATE_OKHTTP
ApproConstants.APPRO_PUBLIC_RETROFIT
ApproConstants.APPRO_PRIVATE_RETROFIT
```

The private client includes `ApproTokenInterceptor`, which reads the current session token.

### 19.1 Create a host service from the private Retrofit

```kotlin
val appModule = module {
    single {
        get<Retrofit>(
            named(ApproConstants.APPRO_PRIVATE_RETROFIT)
        ).createWebService<AppPrivateService>()
    }
}
```

### 19.2 Public service

```kotlin
single {
    get<Retrofit>(
        named(ApproConstants.APPRO_PUBLIC_RETROFIT)
    ).createWebService<AppPublicService>()
}
```

### 19.3 Resource and UiState

Repository operations commonly expose:

```kotlin
sealed class Resource<out T>
```

The presentation layer uses:

```kotlin
sealed class UiState<out T> {
    class Success<T>(val data: T) : UiState<T>()
    class Error<T>(
        val error: Failure,
        val data: T? = null
    ) : UiState<T>()
    class Loading<T>(val data: T? = null) : UiState<T>()
    class Idle<T> : UiState<T>()
}
```

Use exhaustive `when` branches because these are classes, not data objects.

---

## 20. Room and KSP

The base library contains its own Room database and session DAO. Its runtime and generated implementation are part of the library artifact.

### 20.1 When the host needs KSP

If the host app declares any of its own:

```kotlin
@Database
@Dao
@Entity
```

then the host module must apply KSP and add the Room compiler itself.

```toml
[versions]
ksp = "<version-compatible-with-host-kotlin>"
room = "2.8.4"

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }

[libraries]
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
```

```kotlin
plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    ksp(libs.room.compiler)
}
```

KSP is a build-time code generator and cannot be inherited from an AAR.

### 20.2 Missing generated database error

This error:

```text
AppDatabase_Impl does not exist
```

means the module that owns `AppDatabase` did not run the Room compiler. Apply KSP and add `ksp(room-compiler)` in that module.

---

## 21. Navigation helpers

Available helpers include:

```kotlin
drawerAwareComposable<T>()
drawerAwareComposable(route = "...")
navController.sharedViewModel<T>()
navController.sharedViewModel<T>(route)
navController.popBackStackSafely()
navController.navigateSafely(route)
navController.navigateAndClean(route, startDestination)
navController.navigateDeepLink(target)
```

### 21.1 Drawer-aware destination

```kotlin
drawerAwareComposable<HomeRoute> {
    HomeScreen()
}
```

Back closes the drawer first when it is open.

### 21.2 Shared ViewModel

```kotlin
val sharedViewModel = navController
    .sharedViewModel<SharedViewModel>()
```

Or scope it to a specific graph route:

```kotlin
val sharedViewModel = navController
    .sharedViewModel<SharedViewModel>("main_graph")
```

### 21.3 Safe navigation

```kotlin
navController.navigateSafely(SettingsRoute)
```

```kotlin
navController.navigateAndClean(
    route = HomeRoute,
    startDeputation = SplashRoute
)
```

---

## 22. Reusable Compose components

Most components are imported with:

```kotlin
import com.approagency.base.presentation.components.*
```

Related models:

```kotlin
import com.approagency.base.model.ui.*
import com.approagency.base.model.showcase.*
```

The detailed component reference follows after the main integration sections in this same file.

---

## 23. Recommended project structure

```text
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/app/
│   │   │   ├── App.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── config/
│   │   │   │   └── AppDeepLinkParser.kt
│   │   │   ├── di/
│   │   │   │   └── AppModule.kt
│   │   │   ├── navigation/
│   │   │   │   ├── Routes.kt
│   │   │   │   └── AppNavigation.kt
│   │   │   ├── feature/
│   │   │   │   └── home/
│   │   │   │       ├── HomeContract.kt
│   │   │   │       ├── HomeViewModel.kt
│   │   │   │       └── HomeScreen.kt
│   │   │   └── notification/
│   │   │       └── AppNotificationChannels.kt
│   │   ├── res/
│   │   │   └── drawable/ic_notification.xml
│   │   ├── AndroidManifest.xml
│   │   └── google-services.json
│   ├── bazar/
│   ├── myket/
│   └── googlePlay/
└── build.gradle.kts
```

Feature MVI convention:

```text
feature-name/
├── FeatureContract.kt
├── FeatureViewModel.kt
├── FeatureScreen.kt
└── FeatureRepository.kt
```

---

## 24. Troubleshooting

### `NoBeanDefFoundException: FirebaseManager`

Cause: `firebaseModule(firebaseConfig)` was not passed to `Appro.initialize()`.

Fix:

```kotlin
appModules = listOf(
    appModule,
    firebaseModule(firebaseConfig)
)
```

Or do not access/inject `FirebaseManager` when Firebase is intentionally disabled.

### Subscription sheet shows nothing

Cause: `SessionManager.state` is still `SessionState.Loading`.

Fix:

```kotlin
LaunchedEffect(Unit) {
    approViewModel.setEvent(
        ApproContract.Event.CheckStatus
    )
}
```

### Snackbar does not appear

Cause: `BaseActivity.showSnackBar()` updated `snackBarHostState`, but the host did not render `ApproSnackBarHost`.

Fix:

```kotlin
Scaffold(
    snackbarHost = {
        ApproSnackBarHost(
            hostState = snackBarHostState
        )
    }
) { /* content */ }
```

### Deep link is received but navigation does not happen

Check all of these:

1. `ApproConfig.deepLink` is nonblank.
2. `ApproConfig.deepLinks` contains the URI prefix.
3. A `DeepLinkParser` is passed to `Appro.initialize()`.
4. The manifest intent filter matches scheme/host/path.
5. The activity uses `singleTop`.
6. The activity extends `BaseActivity` or manually forwards intents.
7. `DeepLinkNavigationHandler(navController)` is rendered.
8. The parser returns a non-null `DeepLinkTarget`.

### Firebase token is unavailable

Check:

1. Google Services plugin is applied.
2. `google-services.json` matches `applicationId`.
3. `firebaseModule(firebaseConfig)` is installed.
4. `FirebaseConfig.autoInitEnabled` is true.
5. `SessionManager.state` is `SessionState.Login`.
6. Internet access is available.

`FirebaseManager.getToken()` intentionally rejects logged-out calls.

### Notifications are not shown

Check:

1. Android 13+ notification permission.
2. Application notification settings are enabled.
3. Channel importance and configuration.
4. `showForegroundNotifications` / `showBackgroundNotifications`.
5. `notificationFilter` result.
6. Current login state for Firebase-managed notifications.
7. Valid `smallIcon` resource.

### `AppDatabase_Impl does not exist`

The host owns a Room database but did not apply KSP. Add the KSP plugin and Room compiler to the host module.

### Koin `NoSuchMethodError`

Usually caused by a Koin binary version mismatch or by consuming a classified AAR without dependency metadata. Use the normal per-store Maven module and avoid adding a conflicting direct Koin version.

### Missing Bazaar or Myket SDK classes

Use the matching store module:

```kotlin
"bazarImplementation"(libs.appro.native.base.bazar)
"myketImplementation"(libs.appro.native.base.myket)
```

Do not use classifier-only AAR dependencies.

### Google Play purchase always fails

The current Google Play payment implementation is a placeholder and returns `Failure.StoreUnavailable`.

### `PreferencesHelper.initialize()` error

Call `Appro.initialize()` from `Application.onCreate()` before using any activity or service that reads preferences.

### Activity uses a different ApproViewModel instance

Pass `BaseActivity.approViewModel` to shared composables. A `koinViewModel()` call in another navigation scope may resolve a different instance.

---

## 25. Security and current limitations

- Never commit Bazaar or Myket RSA keys, service-account JSON, backend secrets, or private signing keys to a public repository.
- `google-services.json` contains project identifiers and API configuration; manage it according to the project's repository policy.
- Do not log complete FCM tokens, access tokens, refresh tokens, OTP values, or payment tokens in production.
- `ApproConfig.extra` accepts `Any?`; read values with safe casts.
- Firebase is active only for logged-in sessions by design.
- Google Play billing is not implemented in the current source.
- The current payment package name is misspelled as `paymnet`.
- `PreferencesHelper.read()` and `write()` are blocking operations internally.
- `ApproConfig.logEnabled` is not currently read by `Logger`; `ApproConfig.debug` controls logging in this source snapshot.
- `ApproContract.SideEffect` currently has no concrete built-in effects.
- The host must explicitly render the snackbar host and built-in sheets.
- The host must dispatch `ApproContract.Event.CheckStatus` to resolve the initial session state.
- Build-time plugins such as KSP and Google Services are host responsibilities and are not transferred by an AAR.

---

## 26. Quick API reference

### Initialization

```kotlin
Appro.initialize(
    application,
    config,
    deepLinkParser,
    appModules
)
```

### BaseActivity

```kotlin
showSnackBar(...)
openDrawer()
closeDrawer()
showSubscriptionBottomSheet()
hideSubscriptionBottomSheet()
showTermsBottomSheet()
hideTermsBottomSheet()
```

### PreferencesHelper

```kotlin
write(key, value)
read(key)
read(key, defaultValue)
readFlow(key, defaultValue)
remove(key)
```

### SessionManager

```kotlin
state
getSession()
loading()
login(session)
logout()
updateTokens(...)
updateUser(...)
isPremium
```

### FirebaseManager

```kotlin
token
messages
getToken()
refreshToken()
deleteToken()
subscribeToTopic(topic)
unsubscribeFromTopic(topic)
requestNotificationPermission(activity)
hasNotificationPermission()
openNotificationSettings()
showNotification(message)
```

### NotificationManager

```kotlin
isRequired()
hasPermission()
requestPermission(activity)
createChannelGroup(config)
createChannel(config)
show(request)
update(request)
cancel(id)
cancelAll()
```

### ThemeManager

```kotlin
themeMode
setThemeMode(mode)
getDefaultMode()
isDarkMode()
```

### BaseViewModel

```kotlin
state
event
effect
setEvent(event)
setState { copy(...) }
setEffect { effect }
```

### ApproViewModel startup

```kotlin
approViewModel.setEvent(
    ApproContract.Event.CheckStatus
)
```

### Built-in sheets

```kotlin
SubscriptionBottomSheet(...)
TermsBottomSheet(...)
```


---

---

## 27. Complete Compose component reference

This document covers the reusable Jetpack Compose components currently exposed by ApproNativeBase.

Most UI functions are in:

```kotlin
import com.approagency.base.presentation.components.*
```

Related models are mainly in:

```kotlin
import com.approagency.base.model.ui.*
import com.approagency.base.model.showcase.*
```

### Component prerequisites

Initialize ApproNativeBase in the host `Application` and preferably use `BaseActivity` for the main Compose activity. Some components read dependencies through Koin or `LocalBaseActivity`.

```kotlin
class MainActivity : BaseActivity() {
    @Composable
    override fun CreateView() {
        AppContent()
    }
}
```

Components that especially depend on the base activity or initialized base modules include:

- `DeepLinkNavigationHandler`
- `DrawerIconButton`
- `PromotionSlider` default click behavior
- `ScreenShotBox`
- `SubscriptionBottomSheet`
- `TermsBottomSheet`
- `ApproModalBottomSheet` default snackbar overlay

---

### 1. Buttons and selection controls

#### `FilledTextButton`

Material filled button with text and ApproNativeBase defaults.

```kotlin
FilledTextButton(
    text = "Continue",
    onClick = ::continueAction
)
```

Main options: `enabled`, `shape`, `colors`, `contentPadding`, `style`, and `textAlign`.

#### `OutlinedTextButton`

Outlined alternative to `FilledTextButton`.

```kotlin
OutlinedTextButton(
    text = "Cancel",
    onClick = ::close
)
```

#### `LoadingButton`

Displays a progress indicator instead of allowing repeated submission while `isLoading` is true.

```kotlin
LoadingButton(
    text = "Sign in",
    isLoading = state is UiState.Loading,
    onClick = viewModel::login
)
```

#### `DottedLoadingButton`

Button with the animated three-dot loading indicator.

```kotlin
DottedLoadingButton(
    text = "Send code",
    isLoading = sending,
    onClick = ::sendCode
)
```

#### `SimpleTextButton`

Low-emphasis Material text button.

```kotlin
SimpleTextButton(
    text = "Terms",
    onClick = { showTerms = true }
)
```

#### `SimpleIconButton`

Two overloads are available: one accepts `ImageVector`, the other accepts `Painter`.

```kotlin
SimpleIconButton(
    imageVector = Icons.Default.Settings,
    contentDescription = "Settings",
    onClick = ::openSettings
)
```

#### `LabeledSwitchButton`

A full-width label and switch row.

```kotlin
LabeledSwitchButton(
    label = "Notifications",
    checked = notificationsEnabled,
    onCheckedChange = { notificationsEnabled = it }
)
```

#### `LabeledCheckBox`

Checkbox and text in one reusable row.

```kotlin
LabeledCheckBox(
    text = "I accept the terms",
    checked = accepted,
    onCheckedChange = { accepted = it }
)
```

---

### 2. Text fields and OTP

#### `CustomOutlinedTextField`

General-purpose outlined field with built-in error text, placeholder, length limit, keyboard options, and optional icons.

```kotlin
CustomOutlinedTextField(
    value = name,
    onValueChange = { name = it },
    label = "Name",
    isError = nameError != null,
    errorText = nameError,
    maxLength = 50
)
```

#### `PasswordTextField`

Password field with visibility toggle.

```kotlin
PasswordTextField(
    value = password,
    onValueChange = { password = it },
    label = "Password",
    onDone = ::submit
)
```

#### `OtpTextField`

Separated OTP cells with focus management and completion callback.

```kotlin
OtpTextField(
    otpText = otp,
    onOtpTextChange = { value, complete ->
        otp = value
        if (complete) viewModel.verify(value)
    },
    onComplete = viewModel::verify,
    otpCount = 5
)
```

When SMS User Consent is enabled through `BaseActivity`, the received OTP can be connected to this state.

---

### 3. App bars

#### `SimpleAppBar`

Title bar with optional back action and trailing content.

```kotlin
SimpleAppBar(
    title = "Profile",
    onBackClick = navController::navigateUp,
    action = {
        SimpleIconButton(
            imageVector = Icons.Default.Edit,
            onClick = ::edit
        )
    }
)
```

#### `DrawerAppBar`

App bar designed for drawer-based layouts. When no back action is supplied, it uses `DrawerIconButton`.

```kotlin
DrawerAppBar(
    title = "Toolbox"
)
```

#### `DrawerIconButton`

Animated menu/close icon connected to the drawer state exposed by `BaseActivity`.

```kotlin
DrawerIconButton()
```

---

### 4. Bottom navigation

#### Required model

```kotlin
data class BottomNavigationItem<T>(
    val label: Label,
    val icon: Icon,
    val route: T,
    val selectedIcon: Icon = icon,
    val enabled: Boolean = true,
    val badgeCount: Int? = null
)
```

`Label` can be `Label.Text`, `Label.Resource`, or `Label.Annotated`. `Icon` can be `Icon.Vector` or `Icon.Resource`.

#### `BottomNavigationBar`

Adaptive wrapper that can display horizontal or vertical navigation according to `BottomNavigationLayout`.

```kotlin
val items = listOf(
    BottomNavigationItem(
        label = Label.Text("Home"),
        icon = Icon.Vector(Icons.Default.Home),
        route = HomeRoute
    ),
    BottomNavigationItem(
        label = Label.Text("Settings"),
        icon = Icon.Vector(Icons.Default.Settings),
        route = SettingsRoute
    )
)

BottomNavigationBar(
    navController = navController,
    items = items,
    onNavigate = { route -> navController.navigate(route) },
    routeMatcher = { route, currentRoute ->
        currentRoute == route::class.qualifiedName
    }
)
```

Important parameters:

- `layout`: `AUTO`, horizontal, or vertical behavior
- `isVisible`: hide the bar for selected routes
- `showHorizontalLabels` and `showVerticalLabels`
- selected/unselected colors and indicator styling

#### `HorizontalBottomBarTabs`

Use when selection is managed by index instead of `NavController`.

```kotlin
HorizontalBottomBarTabs(
    tabs = items,
    selectedItem = selectedIndex,
    onTabSelected = { selected ->
        selectedIndex = items.indexOf(selected)
    }
)
```

#### `VerticalBottomBarTabs`

Vertical equivalent for large layouts or navigation rails.

#### `BottomTabItem`

Low-level single-item renderer used when building a custom bar.

---

### 5. Drawer components

#### Drawer models

`DrawerItem` supports:

- `DrawerItem.Simple`
- `DrawerItem.DropDown`
- `DrawerItem.Divider`

Example:

```kotlin
val drawerItems = listOf(
    DrawerItem.Simple(
        title = Label.Text("Home"),
        icon = Icon.Vector(Icons.Default.Home),
        selected = true,
        onClick = ::openHome
    ),
    DrawerItem.DropDown(
        title = Label.Text("Tools"),
        icon = Icon.Vector(Icons.Default.Build),
        children = listOf(
            DrawerItem.Simple(
                title = Label.Text("Scanner"),
                onClick = ::openScanner
            )
        )
    ),
    DrawerItem.Divider()
)
```

#### `DrawerContent`

Renders a complete drawer list with optional header and footer.

```kotlin
DrawerContent(
    items = drawerItems,
    progress = drawerProgress,
    header = {
        Text("Appro Toolbox")
    }
)
```

#### `SimpleDrawerItem`

Renders one `DrawerItem.Simple` and supports custom trailing content.

#### `DropdownDrawerItem`

Expandable drawer group with child items and optional child indicator.

---

### 6. Loading, retry, and error states

#### `Loading`

Centered circular progress indicator.

```kotlin
Loading()
```

#### `ThreeDottedLoading`

Animated three-dot indicator.

```kotlin
ThreeDottedLoading()
```

#### `Refresh`

Large retry icon action.

```kotlin
Refresh(onClick = viewModel::reload)
```

#### `LoadingDialog`

Non-dismissible loading dialog by default.

```kotlin
LoadingDialog(isLoading = saving)
```

#### `ErrorLayout`

Two overloads accept `UiText?` or `String`.

```kotlin
ErrorLayout(
    message = errorMessage,
    onRetry = viewModel::reload
)
```

#### `SimpleErrorLayout`

Minimal retry-icon error state.

```kotlin
SimpleErrorLayout(onRetry = viewModel::reload)
```

#### `UiState`

Several components use the shared state model:

```kotlin
sealed class UiState<out T> {
    class Success<T>(val data: T) : UiState<T>()
    class Error<T>(val error: Failure, val data: T? = null) : UiState<T>()
    class Loading<T>(val data: T? = null) : UiState<T>()
    class Idle<T> : UiState<T>()
}
```

---

### 7. Shimmer placeholders

Available components:

- `ShimmerText`
- `ShimmerImage`
- `ShimmerContainer`
- `ShimmerIcon`
- `ShimmerIconButton`
- `shimmerConfig()`

```kotlin
Column {
    ShimmerImage(
        modifier = Modifier.fillMaxWidth(),
        size = 180.dp
    )
    VerticalSpacer()
    ShimmerText(width = 160.dp)
}
```

The global shimmer colors can also be supplied in `ApproConfig.shimmerConfig`.

---

### 8. Images and promotions

#### `NetworkImage`

Coil-based image component with cache controls, crossfade, loading/error customization, and retry support.

```kotlin
NetworkImage(
    imageUrl = item.imageUrl,
    contentDescription = item.title,
    modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
)
```

#### `PromotionSlider`

Auto-scrolling promotion pager. It accepts a list of `Promotion`, supports custom item content and indicators, and can use store-specific promotion URLs.

```kotlin
PromotionSlider(
    items = promotions,
    autoScrollDelay = 5_000L,
    onPromotionClick = { promotion ->
        viewModel.openPromotion(promotion)
    }
)
```

#### `PromotionSliderState`

Convenience wrapper that renders loading, error, empty, or success UI from `UiState<List<Promotion>>`.

```kotlin
PromotionSliderState(
    state = uiState,
    onRetry = viewModel::loadPromotions
)
```

---

### 9. Pull to refresh

#### `PullToRefresh`

Suspend-based pull-to-refresh container with customizable indicator.

```kotlin
PullToRefresh(
    onRefresh = { viewModel.reload() }
) {
    LazyColumn {
        // content
    }
}
```

`onRefresh` is a suspend callback; the component manages its refreshing state and optional delay.

---

### 10. Modals and bottom sheets

#### `ApproModalBottomSheet`

General Material 3 modal sheet with header, drag handle, shape transition, dismiss controls, optional header action, and default snackbar overlay.

```kotlin
ApproModalBottomSheet(
    title = "Filters",
    onDismiss = { showFilters = false }
) {
    FilterContent()
}
```

#### `ApproAlertBottomSheet`

Confirmation/destructive-action sheet.

```kotlin
ApproAlertBottomSheet(
    title = "Delete recording?",
    description = "This action cannot be undone.",
    onConfirm = viewModel::delete,
    onDismiss = { showDelete = false }
)
```

#### `ApproModalHeader`

Reusable modal title row with optional close button and trailing action.

#### `ApproModalDragHandle`

Standalone drag handle used by the default modal sheet.

#### `TermsBottomSheet`

Displays the `LegalConfig` supplied through `ApproConfig`.

```kotlin
if (showTerms) {
    TermsBottomSheet(
        onDismiss = { showTerms = false }
    )
}
```

It supports custom header, section title, item rendering, colors, typography, and padding.

#### `SubscriptionBottomSheet`

Built-in login/OTP/product/purchase flow connected to `ApproViewModel`, `SessionManager`, and the selected store payment implementation.

```kotlin
if (showSubscription) {
    SubscriptionBottomSheet(
        onDismiss = { showSubscription = false },
        onRulesClick = { showTerms = true }
    )
}
```

Use `SubscriptionBottomSheetText` to override its displayed strings.

Lower-level public sections are also available:

- `LoginSheetContent`
- `ProductsSheetContent`
- `ProductItem`

These are useful when the host needs a custom outer sheet but wants to reuse the internal flow UI.

---

### 11. Snackbars

#### `ApproSnackBarHost`

Host for the snackbar state exposed by `BaseActivity`.

```kotlin
val activity = LocalBaseActivity.current

ApproSnackBarHost(
    hostState = activity.snackBarHostState
)
```

#### `ApproSnackBar`

Renders custom visuals using `SnackBarType` and `SnackBarStyle`.

The visual model is:

```kotlin
data class ApproSnackBarVisuals(
    override val message: String,
    val type: SnackBarType = SnackBarType.SIMPLE,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean,
    override val duration: SnackbarDuration,
    val onActionClick: (() -> Unit)? = null
) : SnackbarVisuals
```

---

### 12. Theme components

#### `ThemeModeSelector`

Selectable `SYSTEM`, `LIGHT`, and `DARK` modes with configurable colors, typography, dimensions, shapes, animations, labels, and custom item content.

```kotlin
val scope = rememberCoroutineScope()

ThemeModeSelector(
    selectedMode = themeMode,
    onModeSelected = { mode ->
        scope.launch {
            themeManager.setThemeMode(mode)
        }
    }
)
```

Supporting types:

- `ThemeModeOption`
- `ThemeModeSelectorColors`
- `ThemeModeSelectorShapes`
- `ThemeModeSelectorDimensions`
- `ThemeModeSelectorTypography`
- `ThemeModeSelectorAnimation`
- `ThemeModeSelectorDefaults`

#### `CircularThemeReveal`

Wraps screen content and animates a circular overlay during light/dark theme changes.

```kotlin
CircularThemeReveal(
    isDarkTheme = isDark,
    onThemeChange = { isDark = it }
) { revealState ->
    AppScaffold(
        onToggleTheme = {
            revealState.toggleTheme()
        }
    )
}
```

Use `rememberCircularThemeRevealState(isDarkTheme)` when the state must be hoisted.

---

### 13. Deep-link navigation component

#### `DeepLinkNavigationHandler`

Collects `DeepLinkManager` events and applies the requested navigation behavior to a `NavController`.

```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    DeepLinkNavigationHandler(
        navController = navController
    )

    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        // routes
    }
}
```

It requires:

1. a nonblank `ApproConfig.deepLink`;
2. a host `DeepLinkParser` passed to `Appro.initialize()`;
3. the activity manifest intent filter;
4. `BaseActivity` or equivalent intent forwarding.

See the main integration guide for the full deep-link setup.

---

### 14. Screenshot component

#### `ScreenShotBox`

Captures its Compose content through a `GraphicsLayer`, requests legacy storage permission only below Android 10, and saves the bitmap with `ApproUtils.saveBitmapToGallery()` by default.

```kotlin
ScreenShotBox(
    name = "invoice-${invoice.id}",
    onSaved = { bitmap ->
        // update UI
    },
    onError = logger::record
) { capture ->
    Column {
        InvoiceContent(invoice)
        FilledTextButton(
            text = "Save image",
            onClick = capture
        )
    }
}
```

Callbacks include permission granted/denied, capture, save, and error events. A custom suspend `saveBitmap` implementation can be supplied.

---

### 15. Showcase/onboarding

#### `ShowcaseLayout`

Displays indexed onboarding targets and stores completion state in DataStore Preferences.

```kotlin
val showcaseKey = intPreferencesKey("home_showcase")

ShowcaseLayout(
    key = showcaseKey,
    lastIndex = 2
) { showing, index ->
    HomeContent(
        modifier = Modifier,
        showing = showing,
        showcaseIndex = index
    )
}
```

Supporting models include:

- `ShowcaseMsg`
- `Arrow`
- `Gravity`
- `Side`
- `Head`
- `MsgAnimation`
- `ShowcaseState`

#### `defaultShowCaseMessage`

Creates a default message and arrow configuration.

```kotlin
val message = defaultShowCaseMessage(
    text = "Tap here to scan",
    gravity = Gravity.Bottom,
    targetFrom = Side.Bottom
)
```

---

### 16. Layout spacers

Available helpers:

```kotlin
HorizontalSpacer(8.dp)
VerticalSpacer(16.dp)
```

Inside a `Column`:

```kotlin
FillSpacer()
```

Inside a `Row`:

```kotlin
FillSpacer()
```

The scoped `FillSpacer` uses `Modifier.weight()`.

---

### 17. Component quick index

| Category | Components |
|---|---|
| Buttons | `FilledTextButton`, `OutlinedTextButton`, `LoadingButton`, `DottedLoadingButton`, `SimpleTextButton`, `SimpleIconButton`, `LabeledSwitchButton`, `LabeledCheckBox` |
| Inputs | `CustomOutlinedTextField`, `PasswordTextField`, `OtpTextField` |
| App bars | `SimpleAppBar`, `DrawerAppBar`, `DrawerIconButton` |
| Bottom navigation | `BottomNavigationBar`, `BottomTabItem`, `HorizontalBottomBarTabs`, `VerticalBottomBarTabs` |
| Drawer | `DrawerContent`, `SimpleDrawerItem`, `DropdownDrawerItem` |
| Loading/errors | `Loading`, `ThreeDottedLoading`, `Refresh`, `LoadingDialog`, `ErrorLayout`, `SimpleErrorLayout` |
| Shimmer | `ShimmerText`, `ShimmerImage`, `ShimmerContainer`, `ShimmerIcon`, `ShimmerIconButton` |
| Media | `NetworkImage`, `PromotionSlider`, `PromotionSliderState`, `ScreenShotBox` |
| Refresh | `PullToRefresh` |
| Modals | `ApproModalBottomSheet`, `ApproAlertBottomSheet`, `ApproModalHeader`, `ApproModalDragHandle`, `TermsBottomSheet`, `SubscriptionBottomSheet` |
| Snackbar | `ApproSnackBarHost`, `ApproSnackBar` |
| Theme | `ThemeModeSelector`, `CircularThemeReveal`, `rememberCircularThemeRevealState` |
| Navigation | `DeepLinkNavigationHandler` |
| Showcase | `ShowcaseLayout`, `defaultShowCaseMessage` |
| Layout | `HorizontalSpacer`, `VerticalSpacer`, `FillSpacer` |



---


---

## 28. End-to-end minimum checklist

Before considering the integration complete, confirm:

- [ ] JitPack is added.
- [ ] The correct per-store dependency is used.
- [ ] The host defines the `store` flavor dimension.
- [ ] Bazaar/Myket RSA keys are supplied only to their flavors.
- [ ] `ApproConfig.appName` is set.
- [ ] `Appro.initialize()` is called once from `Application.onCreate()`.
- [ ] The host does not call `startKoin()` separately.
- [ ] `Application` is registered in the manifest.
- [ ] `MainActivity` extends `BaseActivity`.
- [ ] `ApproSnackBarHost` is rendered.
- [ ] `CheckStatus` is dispatched once at startup.
- [ ] Subscription and terms sheets are rendered from the activity flags.
- [ ] Deep-link parser, config, manifest filter, and navigation handler are all present when deep links are enabled.
- [ ] Google Services plugin and `google-services.json` are present when Firebase is enabled.
- [ ] `firebaseModule(firebaseConfig)` is installed when `FirebaseManager` is used.
- [ ] Android 13+ notification permission is requested.
- [ ] Host Room classes use KSP and Room compiler.
- [ ] Google Play payment is treated as unavailable until implemented.
