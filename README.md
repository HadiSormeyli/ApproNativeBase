# Appro Native Base

A reusable Android base library for applications built with Kotlin and Jetpack Compose. The project combines application configuration, dependency injection, theming, localization, session persistence, networking, OTP authentication, subscription products, Bazaar/Myket payments, navigation helpers, and reusable Compose UI components in one library module.

## Implementation
```kotlin
implementation("com.github.HadiSormeyli:ApproNativeBase:v1.0")
```

## Initialize the library

Add this to your build.gradle(:app)

```kotlin
android {
    flavorDimensions += "store"

    productFlavors {
        create("bazaar") {
            dimension = "store"
            buildConfigField("String", "FLAVOR_NAME", "\"BAZAAR\"")
            buildConfigField("String", "PAYMENT_RSA_KEY", "\"YOUR_BAZAAR_RSA_KEY\"")
        }

        create("myket") {
            dimension = "store"
            buildConfigField("String", "FLAVOR_NAME", "\"MYKET\"")
            buildConfigField("String", "PAYMENT_RSA_KEY", "\"YOUR_MYKET_RSA_KEY\"")
        }

        create("googlePlay") {
            dimension = "store"
            buildConfigField("String", "STORE_FLAVOR", "\"GOOGLE_PLAY\"")
            buildConfigField("String", "PAYMENT_RSA_KEY", "\"\"")
        }
    }
}
```

Initialize the base library once from the host `Application` class before resolving library dependencies.

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        BaseInitializer.init(
            application = this,
            config = BaseConfig(
                applicationPackage = BuildConfig.APPLICATION_ID,
                flavor = Flavor.fromString(BuildConfig.FLAVOR_NAME),
                storeLink = "link to your project on markets",
                paymentRsaKey = BuildConfig.PAYMENT_RSA_KEY,
                appVersionName = BuildConfig.VERSION_NAME,
                appVersionCode = BuildConfig.VERSION_CODE,
                isStoreAvailable = true,
                debug = BuildConfig.DEBUG
            ),
            appModules = listOf(appModule)
        )
    }
}
```

> **Project status:** pre-release. The uploaded project contains the main architecture and feature set, but the items in [Pre-release checklist](#pre-release-checklist) should be resolved before production publishing.

## Contents

- [Features](#features)
- [Architecture](#architecture)
- [Requirements](#requirements)
- [Installation](#installation)
- [Host application manifest](#host-application-manifest)
- [Initialize the library](#initialize-the-library)
- [BaseConfig](#baseconfig)
- [BaseActivity](#baseactivity)
- [Theme and localization](#theme-and-localization)
- [State architecture](#state-architecture)
- [Session and local storage](#session-and-local-storage)
- [Networking](#networking)
- [Authentication and subscription flow](#authentication-and-subscription-flow)
- [Payments](#payments)
- [Navigation](#navigation)
- [Reusable UI components](#reusable-ui-components)
- [Circular theme reveal](#circular-theme-reveal)
- [OTP autofill](#otp-autofill)
- [Showcase system](#showcase-system)
- [Project structure](#project-structure)
- [Dependency versions](#dependency-versions)
- [Pre-release checklist](#pre-release-checklist)
- [Security notes](#security-notes)
- [Testing](#testing)
- [Publishing](#publishing)
- [License](#license)

## Features

- Central application configuration through `BaseConfig`
- Koin-based initialization and dependency injection
- Material 3 theme with configurable light/dark color schemes
- Configurable typography, shapes, locale, and custom composition locals
- Theme persistence with DataStore
- Session persistence with Room
- Public and authenticated Retrofit/OkHttp client infrastructure
- Unified `Resource` and `UiState` models
- Base MVI-style ViewModel contracts with events, state, and side effects
- Built-in Appro authentication, OTP, status, product, and subscription flow
- Automatic payment implementation selection using the configured store flavor
- Bazaar payment through Poolakey
- Myket payment through Myket Billing Client
- Google Play payment placeholder for future implementation
- SMS User Consent OTP autofill
- Responsive horizontal/vertical bottom navigation
- Customizable drawer models and components
- Reusable Material 3 Compose components
- Circular theme reveal animation
- In-app feature showcase system
- Safe navigation helper functions

## Architecture

The library is organized as one Android library module named `:app` with the namespace:

```text
com.approagency.base
```

Main layers:

```text
config          Global library and store configuration
di              Koin modules and initialization
local           DataStore and Room persistence
model           Session, API, payment, UI, and showcase models
network         Retrofit services, repositories, interceptors, and error handling
paymnet         Bazaar, Myket, and Google Play payment services
presentation    Base ViewModels, ApproViewModel, BaseActivity, navigation, and UI
session         SessionManager and SessionState
theme           Material theme, theme mode, typography, and shapes
utils           OTP, theme reveal, SSL, sizing, and Compose extensions
```

> The current package is spelled `paymnet`. Rename it to `payment` before publishing if possible, because changing it later will be a breaking API change.

## Requirements

| Requirement | Current project value |
|---|---:|
| Minimum Android SDK | 24 |
| Compile SDK | 36 |
| Java | 21 |
| Kotlin | 2.3.20 |
| Android Gradle Plugin | 8.13.2 |
| Gradle wrapper | 9.1.0 |
| UI | Jetpack Compose + Material 3 |
| Dependency injection | Koin |
| Local database | Room |
| Preferences | DataStore Preferences |
| HTTP | Retrofit + OkHttp |

The consuming project must enable AndroidX and support Java/Kotlin JVM target 21, or the library target must be lowered to match the host project.

## Installation

### Local project dependency

The uploaded repository does not currently include Maven publishing configuration. The supported integration method in its present state is a project dependency.

Include the library module:

```kotlin
// settings.gradle.kts
include(":base")
project(":base").projectDir = file("../ApproNativeBase/app")
```

Add the dependency to the consuming application:

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":base"))
}
```

The current module is named `:app`, even though it applies `com.android.library`. Renaming it to `:base` or `:library` is recommended before distribution.

### Required repositories

The host project needs Google, Maven Central, and JitPack because Poolakey and Myket billing are resolved through JitPack.

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

## Host application manifest

The library performs network operations and checks whether supported stores are installed. Add the following to the consuming application's manifest:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

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

The OTP implementation uses the SMS User Consent API and does not require direct SMS-reading permission.

Register the application in the manifest:

```xml
<application android:name=".App" />
```

`BaseInitializer.init` starts Koin with these built-in modules:

- `baseConfigModule`
- `localModule`
- `sessionModule`
- `uiModule`
- `networkModule`
- `paymentModule`

Additional application modules can be passed through `appModules`.

> Do not call `BaseInitializer.init` when the host application has already called `startKoin`. In that case, add the base modules to the existing Koin application instead.

## BaseConfig

`BaseConfig` is the central configuration object and is registered as a Koin singleton.

| Property | Purpose |
|---|---|
| `applicationPackage` | Package name sent to Appro APIs and used for database/DataStore names |
| `flavor` | Selects Bazaar, Myket, or Google Play payment implementation |
| `storeLink` | Store page or update link |
| `paymentRsaKey` | Store billing RSA public key |
| `appVersionName` | Host application version name |
| `appVersionCode` | Host application version code |
| `paymentGateway` | Backend payment gateway value |
| `isStoreAvailable` | Intended switch for enabling/disabling store payment |
| `defaultLocale` | Initial application locale; defaults to `fa-IR` |
| `lightColorSchema` | Material 3 light color scheme |
| `darkColorSchema` | Material 3 dark color scheme |
| `typography` | Material 3 typography |
| `shapes` | Material 3 shapes |
| `debug` | Enables HTTP and billing debug logging |
| `shimmerColors` | Optional custom image/text shimmer palettes |
| `defaultThemeMode` | `SYSTEM`, `LIGHT`, or `DARK` |
| `providers` | Custom `CompositionLocal` providers |
| `extra` | Application-specific configuration values |

### Custom colors

```kotlin
val config = BaseConfig(
    applicationPackage = BuildConfig.APPLICATION_ID,
    flavor = Flavor.MYKET,
    storeLink = "myket://comment?id=${BuildConfig.APPLICATION_ID}",
    paymentRsaKey = BuildConfig.PAYMENT_RSA_KEY,
    appVersionName = BuildConfig.VERSION_NAME,
    appVersionCode = BuildConfig.VERSION_CODE,
    paymentGateway = Flavor.MYKET.gateway,
    lightColorSchema = createLightColorScheme(Color(0xFF006C4C)),
    darkColorSchema = createDarkColorScheme(Color(0xFF63DDB1))
)
```

### Custom CompositionLocals

```kotlin
val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No AppColors provided")
}

val config = BaseConfig(
    applicationPackage = BuildConfig.APPLICATION_ID,
    flavor = Flavor.BAZAAR,
    storeLink = "market://details?id=${BuildConfig.APPLICATION_ID}",
    paymentRsaKey = BuildConfig.PAYMENT_RSA_KEY,
    appVersionName = BuildConfig.VERSION_NAME,
    appVersionCode = BuildConfig.VERSION_CODE,
    paymentGateway = Flavor.BAZAAR.gateway,
    providers = { isDarkMode ->
        arrayOf(
            LocalAppColors provides if (isDarkMode) DarkAppColors else LightAppColors
        )
    }
)
```

## BaseActivity

`BaseActivity` provides:

- Edge-to-edge Compose setup
- Splash screen installation
- `ApproTheme`
- `SnackbarHostState`
- Material drawer state
- Activity-scoped `ApproViewModel`
- `SessionManager`, `ThemeManager`, and `BaseConfig`
- Subscription-sheet visibility helpers
- OTP User Consent integration
- Locale-aware context wrapping
- Intent-handling helper state

Create an application activity by extending it:

```kotlin
class MainActivity : BaseActivity() {
    @Composable
    override fun CreateView() {
        MainScreen()

        if (showSubscriptionBottomSheet) {
            SubscriptionBottomSheet(
                approViewModel = approViewModel,
                onRulesClick = {
                    // Open terms and conditions
                },
                onDismiss = ::hideSubscriptionDialog
            )
        }
    }
}
```

The `ApproViewModel` is scoped to the activity:

```kotlin
val approViewModel: ApproViewModel by viewModel()
```

Pass this instance to child composables when they must share the same state. Calling `koinViewModel()` inside a navigation destination may use that destination's `ViewModelStoreOwner` and return a different instance.

## Theme and localization

### Theme modes

```kotlin
lifecycleScope.launch {
    themeManager.setThemeMode(ThemeMode.DARK)
}
```

Supported modes:

```text
SYSTEM
LIGHT
DARK
```

The selected mode is stored in DataStore under `PreferencesHelper.Keys.THEME_MODE`.

### Locale

The default locale comes from `BaseConfig.defaultLocale`. The active language is stored under `PreferencesHelper.Keys.LANGUAGE`.

```kotlin
PreferencesHelper.write(
    PreferencesHelper.Keys.LANGUAGE,
    "en"
)
recreate()
```

`ApproTheme` automatically selects RTL or LTR layout direction using the locale's platform layout direction.

### Default design system

The library includes:

- `ApproTypography` using the bundled Dana font family
- `ApproShapes` with rounded Material 3 shape sizes
- `createLightColorScheme(primaryColor)`
- `createDarkColorScheme(primaryColor)`

All can be replaced through `BaseConfig`.

## State architecture

The base presentation architecture uses:

```text
ViewEvent
ViewState
ViewSideEffect
BaseViewModel<Event, State, Effect>
BaseApplicationViewModel<Event, State, Effect>
```

`BaseViewModel` exposes:

- Compose `State<UiState>` for screen state
- `SharedFlow<Event>` for events
- `Flow<Effect>` for one-time side effects
- `setEvent` to dispatch an event
- `setState` to reduce state
- `setEffect` to emit a side effect

Because `BaseViewModel.state` is Compose `State`, observe it directly:

```kotlin
val state by approViewModel.state
```

Do not call `collectAsStateWithLifecycle()` on it. That extension is for `Flow` and `StateFlow`.

### ApproContract

`ApproContract` currently includes these events:

- `Login`
- `CheckOtp`
- `CheckStatus`
- `GetProducts`
- `Purchase`
- `OnPhoneNumberChanged`
- `OnOtpChanged`
- `ResetLoginState`
- `ResetPurchaseState`

Its state contains:

- Current authentication step
- Phone number and OTP input
- Login state
- OTP state
- User status state
- Products state
- Purchase state

Dispatch events with:

```kotlin
approViewModel.setEvent(
    ApproContract.Event.CheckStatus
)
```

## Session and local storage

### Session model

`Session` stores:

- Appro/access/refresh tokens
- Token type and expiration
- User ID
- Phone number
- First and last name
- Creation/update timestamps
- Premium status

The default session ID is:

```kotlin
Session.ID // "key"
```

### SessionManager

`SessionManager` is a Koin singleton backed by Room and protected by a `Mutex` for writes.

Main operations:

```kotlin
sessionManager.getSession()
sessionManager.login(session)
sessionManager.updateTokens(...)
sessionManager.updateUser(...)
sessionManager.logout()
```

Observe session state:

```kotlin
val sessionState by sessionManager.state.collectAsStateWithLifecycle()
```

Available states:

```text
SessionState.Loading
SessionState.Logout
SessionState.Login(session)
```

### Room

The library database contains one `session` table through:

```text
ApproDatabase
SessionDao
SessionEntity
```

The intended database filename is:

```text
<applicationPackage>.base.db
```

### DataStore

The intended DataStore filename is:

```text
<applicationPackage>.ds
```

Built-in keys:

```kotlin
PreferencesHelper.Keys.THEME_MODE
PreferencesHelper.Keys.LANGUAGE
```

Custom keys are also supported:

```kotlin
val ONBOARDING_COMPLETED = booleanPreferencesKey("ONBOARDING_COMPLETED")

PreferencesHelper.write(ONBOARDING_COMPLETED, true)
val completed = PreferencesHelper.read(ONBOARDING_COMPLETED, false)
val completedFlow = PreferencesHelper.readFlow(ONBOARDING_COMPLETED, false)
```

## Networking

The default API base URL is:

```text
https://api.approagency.ir/api/
```

Default timeout values:

| Timeout | Value |
|---|---:|
| Connect | 30 seconds |
| Read | 30 seconds |
| Write | 240 seconds |

### Registered clients

The network module registers named public/private clients and Retrofit instances:

```kotlin
ApproConstants.APPRO_PUBLIC_OKHTTP
ApproConstants.APPRO_PRIVATE_OKHTTP
ApproConstants.APPRO_PUBLIC_RETROFIT
ApproConstants.APPRO_PRIVATE_RETROFIT
```

Resolve a named Retrofit instance:

```kotlin
val retrofit: Retrofit = get(
    named(ApproConstants.APPRO_PRIVATE_RETROFIT)
)
```

Create a service:

```kotlin
single<MyPrivateService> {
    get<Retrofit>(
        named(ApproConstants.APPRO_PRIVATE_RETROFIT)
    ).createWebService()
}
```

The private client uses `ApproTokenInterceptor`, which adds:

```text
Authorization: Bearer <approToken>
```

### Appro API operations

`ApproRepository` wraps these endpoints in `Flow<Resource<T>>`:

| Operation | Repository method |
|---|---|
| Promotions | `getPromotions()` |
| Send login OTP | `login(mobile)` |
| Verify OTP | `checkOtp(mobile, token)` |
| User/package status | `getStatus()` |
| Subscription products | `getProducts()` |
| Verify subscription purchase | `subscribeProduct(...)` |

### Resource and errors

Network calls emit:

```text
Resource.Loading
Resource.Success(data)
Resource.Error(failure)
```

Convert to UI state with:

```kotlin
resource.toUiState()
```

`Failure` maps HTTP, connection, permission, authentication, store installation, subscription, and purchase errors to localizable `UiText` values.

## Authentication and subscription flow

The built-in flow is coordinated by `ApproViewModel`:

```text
Phone number
    ↓
Send OTP
    ↓
Verify OTP
    ↓
Persist Session
    ↓
Check subscription status
    ↓
Load products
    ↓
Start store purchase
    ↓
Verify purchase with Appro API
    ↓
Refresh subscription status
```

### SubscriptionBottomSheet

The library provides a complete bottom sheet for login, OTP verification, product selection, purchase progress, and result feedback.

```kotlin
SubscriptionBottomSheet(
    approViewModel = approViewModel,
    onRulesClick = {
        navigator.openTerms()
    },
    onDismiss = {
        hideSubscriptionDialog()
    }
)
```

Text can be customized without replacing the component:

```kotlin
SubscriptionBottomSheet(
    approViewModel = approViewModel,
    text = SubscriptionBottomSheetText(
        loginTitle = "Sign in",
        subscriptionTitle = "Choose a plan",
        sendCode = "Send code",
        purchase = "Continue",
        currency = "USD"
    ),
    onRulesClick = navigator::openTerms
)
```

## Payments

`paymentModule` selects one `PaymentService` from `BaseConfig.flavor`:

```text
Flavor.BAZAAR      → BazaarPaymentService
Flavor.MYKET       → MyketPaymentService
Flavor.GOOGLE_PLAY → GooglePlayPaymentService
```

Inject the selected service normally:

```kotlin
val paymentService: PaymentService by inject()
```

### Store support

| Store | Package checked | Current implementation |
|---|---|---|
| Bazaar | `com.farsitel.bazaar` | Subscription purchase with Poolakey |
| Myket | `ir.mservices.market` | Subscription purchase with Myket Billing Client |
| Google Play | `com.android.vending` | Placeholder only |

### Payment request

```kotlin
val request = PaymentRequest(
    productId = product.id,
    productUuid = product.uuid,
    type = PaymentProductType.SUBSCRIPTION,
    sessionId = Session.ID
)
```

Start payment directly:

```kotlin
paymentService.purchase(
    activity = activity,
    request = request
).collect { result ->
    when (result) {
        is Resource.Loading -> Unit
        is Resource.Success -> Unit
        is Resource.Error -> Unit
    }
}
```

Or dispatch through `ApproViewModel`:

```kotlin
approViewModel.setEvent(
    ApproContract.Event.Purchase(
        activity = activity,
        paymentRequest = product.toPaymentRequest()
    )
)
```

The generated payload defaults to:

```text
<phoneNumber>|<versionCode>
```

A custom payload can be supplied in `PaymentRequest.payload`.

`PaymentProductType.IN_APP` exists in the model but the current Bazaar/Myket implementations execute subscription flows only.

## Navigation

### Drawer-aware destinations

`drawerAwareComposable` closes an open drawer when the system back button is pressed.

```kotlin
NavHost(
    navController = navController,
    startDestination = HomeRoute
) {
    drawerAwareComposable<HomeRoute> {
        HomeScreen()
    }
}
```

String routes are also supported.

### Safe navigation helpers

```kotlin
navController.navigateSafely(route)
navController.popBackStackSafely()
navController.navigateAndClean(route, startDestination)
```

### Shared ViewModels

```kotlin
val viewModel: SharedViewModel = navController.sharedViewModel()
```

A graph route can be supplied explicitly when needed.

### Bottom navigation models

`BottomNavigationItem<T>` supports resource, plain-text, or annotated labels and resource or vector icons.

```kotlin
val items = listOf(
    BottomNavigationItem(
        label = Label.Resource(R.string.home),
        icon = Icon.Resource(R.drawable.ic_home_outline),
        selectedIcon = Icon.Resource(R.drawable.ic_home_filled),
        route = HomeRoute,
        badgeCount = null
    ),
    BottomNavigationItem(
        label = Label.Text("Profile"),
        icon = Icon.Vector(Icons.Outlined.Person),
        selectedIcon = Icon.Vector(Icons.Filled.Person),
        route = ProfileRoute
    )
)
```

Use the responsive bar:

```kotlin
BottomNavigationBar(
    navController = navController,
    items = items,
    layout = BottomNavigationLayout.AUTO,
    routeMatcher = { route, currentRoute ->
        route::class.java.canonicalName == currentRoute
    },
    onNavigate = navController::navigateSafely
)
```

`AUTO` uses horizontal navigation in portrait and vertical navigation in landscape.

### Drawer models

```kotlin
val drawerItems = listOf(
    DrawerItem.Simple(
        title = Label.Text("Home"),
        icon = Icon.Vector(Icons.Default.Home),
        selected = true,
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        foregroundColor = MaterialTheme.colorScheme.onPrimaryContainer,
        onClick = navigator::openHome
    ),
    DrawerItem.DropDown(
        title = Label.Text("Tools"),
        icon = Icon.Vector(Icons.Default.Build),
        children = listOf(
            DrawerItem.Simple(
                title = Label.Text("Scanner"),
                onClick = navigator::openScanner
            )
        )
    ),
    DrawerItem.Divider()
)
```

Render the drawer:

```kotlin
DrawerContent(
    items = drawerItems,
    progress = drawerProgress,
    header = {
        ProfileHeader()
    },
    footer = {
        VersionFooter()
    }
)
```

Global colors are controlled through `DrawerColors`; each item may override its own foreground/background color.

## Reusable UI components

The library intentionally exposes configurable Material 3 building blocks without requiring a separate design-system module.

### App bars

- `SimpleAppBar`
- `DrawerAppBar`
- `DrawerIconButton`

### Buttons and controls

- `FilledTextButton`
- `OutlinedTextButton`
- `LoadingButton`
- `DottedLoadingButton`
- `SimpleTextButton`
- `SimpleIconButton` for `ImageVector` or `Painter`
- `LabeledSwitchButton`
- `LabeledCheckBox`

### Text fields

- `CustomOutlinedTextField`
- `OtpTextField`
- `PasswordTextField`

The OTP field supports configurable count, box size, focused size, colors, shape, and completion callback.

### Loading and error states

- `Loading`
- `ThreeDottedLoading`
- `Refresh`
- `LoadingDialog`
- `ErrorLayout`
- `SimpleErrorLayout`

### Shimmer

Custom shimmer colors can be set globally through `BaseConfig.shimmerColors` or per component.

Available components:

- `ShimmerText`
- `ShimmerImage`
- `ShimmerContainer`
- `ShimmerIcon`
- `ShimmerIconButton`

### Images

`NetworkImage` uses Coil and supports:

- Memory/disk/network cache policies
- Crossfade
- Custom loading content
- Custom error content
- Retry action
- Success/error callbacks
- Configurable content scale

### Modals and snackbars

- `ApproModalBottomSheet`
- `ApproAlertBottomSheet`
- `ApproModalHeader`
- `ApproModalDragHandle`
- `ApproSnackBarHost`
- `ApproSnackBar`

Snackbar types:

```text
SUCCESS
ERROR
WARNING
SIMPLE
```

### Layout helpers

- `HorizontalSpacer`
- `VerticalSpacer`
- `FillSpacer` for `RowScope`
- `FillSpacer` for `ColumnScope`

## Circular theme reveal

`CircularThemeReveal` records the rendered content and reveals the new theme from a selected composable's center position.

```kotlin
var isDarkTheme by remember {
    mutableStateOf(themeManager.isDarkMode())
}

CircularThemeReveal(
    isDarkTheme = isDarkTheme,
    onThemeChange = { dark ->
        isDarkTheme = dark

        scope.launch {
            themeManager.setThemeMode(
                if (dark) ThemeMode.DARK else ThemeMode.LIGHT
            )
        }
    }
) { revealState ->
    AppContent(
        themeButtonModifier = Modifier.circularThemeRevealOrigin(revealState),
        onThemeClick = revealState::toggle
    )
}
```

The origin modifier can be attached to any icon or composable inside the recorded content.

## OTP autofill

`BaseActivity` integrates Google's SMS User Consent API.

The library:

1. Starts SMS user consent when the OTP screen is active.
2. Opens the user-consent prompt when a matching SMS arrives.
3. Extracts a five-digit code.
4. Emits the code through `OtpAutoFillBus`.
5. Fills and submits the OTP in `SubscriptionBottomSheet`.

The OTP length is currently configured as five digits in both the SMS regex and the default `OtpTextField` count.

## Showcase system

The showcase package provides an overlay for highlighting interface elements and displaying guided messages.

Main elements include:

- `ShowcaseLayout`
- `ShowcaseState`
- `ShowcaseData`
- `ShowcaseMsg`
- `ShowcaseScopeImpl`
- Configurable arrow, gravity, side, head, and message animation models

Showcase progress is stored through a DataStore `Preferences.Key<Int>` so completed guides can be skipped later.

```kotlin
val HOME_SHOWCASE_STEP = intPreferencesKey("HOME_SHOWCASE_STEP")

ShowcaseLayout(
    key = HOME_SHOWCASE_STEP,
    lastIndex = 2
) { isShowing, currentIndex ->
    HomeContent()
}
```

## Project structure

```text
ApproNativeBase/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/approagency/base/
│       │   ├── config/
│       │   ├── di/
│       │   ├── local/
│       │   ├── model/
│       │   ├── network/
│       │   ├── paymnet/
│       │   ├── presentation/
│       │   ├── session/
│       │   ├── theme/
│       │   └── utils/
│       └── res/
├── gradle/libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

The uploaded source contains approximately 91 Kotlin files and 7,400 lines of Kotlin code.

## Dependency versions

Versions declared in `gradle/libs.versions.toml`:

| Dependency | Version |
|---|---:|
| Android Gradle Plugin | 8.13.2 |
| Kotlin | 2.3.20 |
| KSP | 2.3.10 |
| Room | 2.8.4 |
| Kotlin Coroutines | 1.11.0 |
| Koin | 4.2.2 |
| DataStore | 1.2.1 |
| Retrofit | 3.0.0 |
| Gson converter | 3.0.0 |
| OkHttp | 5.4.0 |
| Compose BOM | 2026.06.01 |
| Activity Compose | 1.13.0 |
| Navigation Compose | 2.9.8 |
| Splash Screen | 1.2.0 |
| Coil Compose | 2.7.0 |
| Poolakey | 2.2.0 |
| Myket Billing Client | 1.19 |
| Play Services Auth API Phone | 18.3.1 |

## Pre-release checklist

The following items were found in the uploaded source and should be fixed before publishing a production release.

### Build and initialization

- `localModule` returns `Room.databaseBuilder(...)` without calling `.build()`.
- The current database filename contains an extra closing brace: `"<package>.base.db}"`.
- `PreferencesHelper.init` is inside a lazy Koin singleton definition. `ThemeManager` accesses `PreferencesHelper` directly and may run before that singleton has initialized DataStore.
- `ApproTokenInterceptor` is required by the private client but is not registered in `networkModule`.
- The full Gradle compile could not be executed in the documentation environment because Gradle 9.1.0 was not locally available and external downloading was unavailable.

### Session and authentication

- `SessionManager` starts in `Loading` and does not currently observe or restore the persisted Room session automatically.
- `SessionManager.login` writes asynchronously. `ApproViewModel.checkOtp` immediately calls `checkStatus`, creating a possible race before the session has been inserted.
- `SessionManager.login` does not persist `isPremium`, so a refreshed premium value can be lost in Room.
- Authenticated Appro endpoints are currently created from the public Retrofit service registration; confirm whether `getStatus` and purchase verification must use the private client.

### Payment

- Bazaar and Myket currently throw `StoreUnavailable` when `config.isStoreAvailable` is `true`; the condition appears inverted.
- Google Play payment is not implemented and throws directly instead of returning an error `Flow<Resource<String>>`.
- `PaymentProductType.IN_APP` is declared but Bazaar/Myket currently use subscription purchase flows only.
- Myket uses `config.paymentGateway`, while Bazaar uses `config.flavor.gateway`; standardize the backend gateway source.
- The purchase success message is hardcoded in Persian.
- `ApproViewModel.purchase` does not currently block purchase when the session is already premium.

### Activity and OTP

- `BaseActivity` implements `OtpAutofillController` but does not currently implement `stopOtpAutofill()`.
- The registered SMS broadcast receiver is not unregistered in `onDestroy`.
- `INTENT_FLAG_KEY` is read from saved state but is not currently written in `onSaveInstanceState`.
- Autofill submits immediately after dispatching the OTP change event; ensure verification reads the new OTP rather than the previous state value.

### UI safety

- `SubscriptionBottomSheet` uses `product.title!!`; nullable server data can cause a crash.
- The purchase-success path calls the external `onDismiss` directly; ensure the activity's `showSubscriptionBottomSheet` flag is also cleared.

### Error mapping

- `Failure` contains duplicate/colliding error codes, including `HaveSubscription` and `NoAccess` both using `1005`.
- `InstallBazarApplication` appears twice in the internal failure list.
- `StoreUnavailable` is not currently included in the failure lookup list.
- HTTP 451 currently points to the too-many-requests text instead of the dedicated legal-reasons string.

### Distribution

- Maven/JitPack publishing is not configured.
- No public library version is declared.
- No `LICENSE` file is included.
- Only generated example tests are currently present.

## Security notes

### RSA keys

Do not commit production store RSA keys directly to source control. Provide them from a secure build configuration, CI secret, or local property.

```kotlin
buildConfigField(
    "String",
    "PAYMENT_RSA_KEY",
    "\"${providers.gradleProperty("PAYMENT_RSA_KEY").orNull.orEmpty()}\""
)
```

### Legacy SSL helper

`addSLLFactory()` currently trusts all certificates and accepts every hostname on Android 7.0 and lower. This disables TLS certificate validation and must not be used in production.

Replace it with a secure TLS 1.2 compatibility implementation that preserves the platform trust manager and hostname verification, or remove it when the minimum supported platform no longer needs the workaround.

### HTTP logging

`BaseConfig.debug = true` enables BODY-level HTTP logging. Do not enable it in production because bodies and authorization data may be sensitive.

## Testing

The current project contains only the generated example unit and instrumented tests. Recommended coverage before release:

- Session persistence and restoration
- Concurrent token/session updates
- Failure and HTTP-code mapping
- Public/private client authentication behavior
- OTP event ordering
- Bazaar cancellation, failure, and success paths
- Myket cancellation, failure, and success paths
- Payment verification requests
- Theme persistence and locale direction
- Bottom-navigation route matching
- Drawer selected/disabled states
- Subscription sheet state transitions

## License

No license is included in the uploaded project. Add a `LICENSE` file and replace this section with the selected license before public distribution.
