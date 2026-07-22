package com.approagency.base.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.approagency.base.Hsl
import com.approagency.base.R
import com.approagency.base.config.ApproConstants
import com.approagency.base.firebase.FirebaseMessage
import com.approagency.base.model.TonalPalette
import com.approagency.base.model.ui.GeneratedPalettes
import com.approagency.base.model.ui.Icon
import com.approagency.base.model.ui.Label
import com.approagency.base.model.ui.UiText
import com.approagency.base.model.ui.deepLink.DeepLinkInput
import com.approagency.base.presentation.BaseActivity
import com.approagency.base.session.SessionManager
import com.approagency.base.session.SessionState
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import org.json.JSONObject
import org.koin.compose.koinInject
import org.koin.core.context.GlobalContext.get
import retrofit2.Retrofit
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.resumeWithException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

fun OkHttpClient.Builder.addSLLFactory(): OkHttpClient.Builder {
    val trustAllCerts = arrayOf<TrustManager>(
        @SuppressLint("CustomX509TrustManager") object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(
                chain: Array<out X509Certificate>?,
                authType: String?
            ) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(
                chain: Array<out X509Certificate>?,
                authType: String?
            ) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    )

    val sslContext = SSLContext.getInstance("TLSv1.2").apply {
        init(null, trustAllCerts, SecureRandom())
    }

    val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
        .tlsVersions(TlsVersion.TLS_1_2)
        .cipherSuites(*ConnectionSpec.MODERN_TLS.cipherSuites!!.toTypedArray())
        .build()

    this.hostnameVerifier { _, _ -> true }
        .connectionSpecs(listOf(spec, ConnectionSpec.CLEARTEXT))
        .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
    return this
}

inline fun <reified T> Retrofit.createWebService(): T = create(T::class.java)

val Context.widthDp: Dp
    get() = this.resources.configuration.screenWidthDp.dp

val Context.heightDp: Dp
    get() = this.resources.configuration.screenHeightDp.dp

fun Modifier.shimmerLoadingAnimation(
    shimmerColors: List<Color>,
    widthOfShadowBrush: Int = 500,
    angleOfAxisY: Float = 270f,
    durationMillis: Int = ApproConstants.SHIMMER_ANIMATION_DURATION,
): Modifier {
    return composed {
        val transition = rememberInfiniteTransition(label = "")

        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = (durationMillis + widthOfShadowBrush).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMillis,
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "Shimmer loading animation",
        )

        this.background(
            brush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(x = translateAnimation.value - widthOfShadowBrush, y = 0.0f),
                end = Offset(x = translateAnimation.value, y = angleOfAxisY),
            ),
        )
    }
}

fun Context.isPortrait() = this.orientation == Configuration.ORIENTATION_PORTRAIT

fun Context.isLandScape() = this.orientation == Configuration.ORIENTATION_LANDSCAPE

val Context.orientation: Int
    get() = this.resources.configuration.orientation

val BaseActivity.orientation: Int
    get() = this.resources.configuration.orientation

@Composable
fun Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}

@Composable
fun calculateStatusBarPadding() = WindowInsets.statusBars.getTop(LocalDensity.current).toFloat()

@Composable
fun calculateNavigationBarPadding() =
    WindowInsets.statusBars.getBottom(LocalDensity.current).toFloat()

fun Modifier.circularThemeRevealOrigin(
    state: CircularThemeRevealState
): Modifier {
    return onGloballyPositioned { coordinates ->
        val position = coordinates.positionInRoot()
        state.origin = Offset(
            x = position.x + coordinates.size.width / 2f - state.containerPosition.x,
            y = position.y + coordinates.size.height / 2f - state.containerPosition.y
        )
    }
}

@Composable
fun Label.asAnnotatedString(): AnnotatedString {
    return when (this) {
        is Label.Resource -> AnnotatedString(stringResource(id))
        is Label.Text -> AnnotatedString(value)
        is Label.Annotated -> value
    }
}

fun Color.resolveColor(defaultColor: Color): Color {
    return if (this == Color.Unspecified) defaultColor else this
}

fun Color.applyEnabledAlpha(
    enabled: Boolean,
    disabledAlpha: Float
): Color {
    return if (enabled) this else copy(alpha = alpha * disabledAlpha)
}


@Composable
fun Icon.Icon(
    modifier: Modifier, tint: Color, contentDescription: String?
) {
    when (this) {
        is Icon.Resource -> Icon(
            painter = painterResource(this.id),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )

        is Icon.Vector -> Icon(
            imageVector = this.imageVector,
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    }
}

internal fun Context.isPackageInstalled(
    packageName: String
): Boolean {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }
    }.isSuccess
}

fun RemoteMessage.toFirebaseMessage(): FirebaseMessage {
    val notification = notification

    return FirebaseMessage(
        title = notification?.title
            ?: data[ApproConstants.FIREBASE_TITLE],
        description = notification?.body
            ?: data[ApproConstants.FIREBASE_DESCRIPTION]
            ?: data[ApproConstants.FIREBASE_BODY],
        imageUrl = notification?.imageUrl?.toString()
            ?: data[ApproConstants.FIREBASE_IMAGE]
            ?: data[ApproConstants.FIREBASE_IMAGE_URL],
        data = data
    )
}

suspend fun <T> Task<T>.awaitResult(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (!continuation.isActive) {
                return@addOnCompleteListener
            }

            when {
                task.isCanceled -> continuation.cancel()
                task.isSuccessful -> continuation.resume(task.result) { cause, _, _ ->

                }

                else -> continuation.resumeWithException(
                    task.exception ?: IllegalStateException(
                        "Firebase task failed"
                    )
                )
            }
        }
    }
}

suspend fun Task<Void>.awaitCompletion() {
    suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (!continuation.isActive) {
                return@addOnCompleteListener
            }

            when {
                task.isCanceled -> continuation.cancel()
                task.isSuccessful -> continuation.resume(Unit) { cause, _, _ -> }
                else -> continuation.resumeWithException(
                    task.exception ?: IllegalStateException(
                        "Firebase task failed"
                    )
                )
            }
        }
    }
}

fun Context.isAppForeground(): Boolean {
    val processInfo = ActivityManager.RunningAppProcessInfo()

    ActivityManager.getMyMemoryState(processInfo)

    return processInfo.importance ==
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
            processInfo.importance ==
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
}

fun Intent.toDeepLinkInput(): DeepLinkInput? {
    val link = dataString
        ?: getStringExtra(ApproConstants.LINK)
        ?: return null

    return DeepLinkInput(
        uri = link.toUri(),
        data = getStringExtra(ApproConstants.DATA)
            .toStringMap()
    )
}

private fun String?.toStringMap(): Map<String, String> {
    if (isNullOrBlank()) {
        return emptyMap()
    }

    return runCatching {
        val json = JSONObject(this)

        json.keys()
            .asSequence()
            .associateWith { key ->
                json.optString(key)
            }
    }.getOrDefault(emptyMap())
}

fun BaseActivity.openLink(link: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, link.toUri())
        startActivity(intent)
    } catch (_: Exception) {
        showSnackBar(UiText.StringResource(R.string.error_open_link))
    }
}

fun BaseActivity.openLink(link: Uri) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, link)
        startActivity(intent)
    } catch (_: Exception) {
        showSnackBar(UiText.StringResource(R.string.error_open_link))
    }
}

fun PagerState.pageOffset(page: Int): Float {
    return abs(
        currentPage - page + currentPageOffsetFraction
    ).coerceIn(0f, 1f)
}

@Composable
fun rememberIsUserPremium(
    sessionManager: SessionManager = koinInject()
): Boolean {
    val state by sessionManager.state.collectAsStateWithLifecycle()

    return (state as? SessionState.Login?)
        ?.session
        ?.isPremium == true
}

fun isUserPremium(): Boolean {
    return get()
        .get<SessionManager>()
        .isPremium
}

fun Color.toOpaqueColor(): Color {
    return Color(
        red = red.coerceIn(0f, 1f),
        green = green.coerceIn(0f, 1f),
        blue = blue.coerceIn(0f, 1f),
        alpha = 1f
    )
}

fun hslToColor(
    hue: Float,
    saturation: Float,
    lightness: Float
): Color {
    val h = ((hue % 360f) + 360f) % 360f
    val s = saturation.coerceIn(0f, 1f)
    val l = lightness.coerceIn(0f, 1f)

    val chroma = (1f - abs(2f * l - 1f)) * s
    val section = h / 60f
    val x = chroma * (1f - abs(section % 2f - 1f))
    val match = l - chroma / 2f

    val rgb = when {
        section < 1f -> Triple(chroma, x, 0f)
        section < 2f -> Triple(x, chroma, 0f)
        section < 3f -> Triple(0f, chroma, x)
        section < 4f -> Triple(0f, x, chroma)
        section < 5f -> Triple(x, 0f, chroma)
        else -> Triple(chroma, 0f, x)
    }

    return Color(
        red = (rgb.first + match).coerceIn(0f, 1f),
        green = (rgb.second + match).coerceIn(0f, 1f),
        blue = (rgb.third + match).coerceIn(0f, 1f),
        alpha = 1f
    )
}

fun lStarToRelativeLuminance(
    lStar: Float
): Float {
    val value = lStar.coerceIn(0f, 100f)

    return if (value > 8f) {
        val result = (value + 16f) / 116f
        result * result * result
    } else {
        value / 903.2963f
    }
}

fun Float.toLinearRgb(): Float {
    val channel = coerceIn(0f, 1f)

    return if (channel <= 0.04045f) {
        channel / 12.92f
    } else {
        ((channel + 0.055f) / 1.055f).pow(2.4f)
    }
}

fun Color.relativeLuminance(): Float {
    val linearRed = red.toLinearRgb()
    val linearGreen = green.toLinearRgb()
    val linearBlue = blue.toLinearRgb()

    return (
            0.2126f * linearRed +
                    0.7152f * linearGreen +
                    0.0722f * linearBlue
            ).coerceIn(0f, 1f)
}

fun mixOpaque(
    start: Color,
    end: Color,
    fraction: Float
): Color {
    val amount = fraction.coerceIn(0f, 1f)

    return Color(
        red = start.red + (end.red - start.red) * amount,
        green = start.green + (end.green - start.green) * amount,
        blue = start.blue + (end.blue - start.blue) * amount,
        alpha = 1f
    )
}

fun normalizeHue(
    hue: Float
): Float {
    return ((hue % 360f) + 360f) % 360f
}

fun Color.toHsl(): Hsl {
    val redValue = red.coerceIn(0f, 1f)
    val greenValue = green.coerceIn(0f, 1f)
    val blueValue = blue.coerceIn(0f, 1f)

    val maximum = max(
        redValue,
        max(greenValue, blueValue)
    )

    val minimum = min(
        redValue,
        min(greenValue, blueValue)
    )

    val difference = maximum - minimum
    val lightness = (maximum + minimum) / 2f

    val saturation = if (difference == 0f) {
        0f
    } else {
        difference / (1f - abs(2f * lightness - 1f))
    }

    val calculatedHue = when {
        difference == 0f -> {
            0f
        }

        maximum == redValue -> {
            60f * (((greenValue - blueValue) / difference) % 6f)
        }

        maximum == greenValue -> {
            60f * (((blueValue - redValue) / difference) + 2f)
        }

        else -> {
            60f * (((redValue - greenValue) / difference) + 4f)
        }
    }

    return Hsl(
        hue = normalizeHue(calculatedHue),
        saturation = saturation.coerceIn(0f, 1f),
        lightness = lightness.coerceIn(0f, 1f)
    )
}


fun Color.withTone(
    tone: Float
): Color {
    val targetTone = tone.coerceIn(0f, 100f)

    if (targetTone <= 0f) {
        return Color.Black
    }

    if (targetTone >= 100f) {
        return Color.White
    }

    val source = toOpaqueColor()
    val sourceLuminance = source.relativeLuminance()
    val targetLuminance = lStarToRelativeLuminance(targetTone)

    if (abs(sourceLuminance - targetLuminance) < 0.00001f) {
        return source
    }

    val destination = if (targetLuminance > sourceLuminance) {
        Color.White
    } else {
        Color.Black
    }

    val shouldBrighten = targetLuminance > sourceLuminance

    var minimum = 0f
    var maximum = 1f

    repeat(24) {
        val fraction = (minimum + maximum) / 2f

        val candidate = mixOpaque(
            start = source,
            end = destination,
            fraction = fraction
        )

        val candidateLuminance = candidate.relativeLuminance()

        if (shouldBrighten) {
            if (candidateLuminance < targetLuminance) {
                minimum = fraction
            } else {
                maximum = fraction
            }
        } else {
            if (candidateLuminance > targetLuminance) {
                minimum = fraction
            } else {
                maximum = fraction
            }
        }
    }

    return mixOpaque(
        start = source,
        end = destination,
        fraction = (minimum + maximum) / 2f
    )
}

fun generatePalettes(primaryColor: Color): GeneratedPalettes {
    val opaquePrimary = primaryColor.toOpaqueColor()
    val primaryHsl = opaquePrimary.toHsl()


    val primarySaturation = primaryHsl.saturation.coerceIn(
        minimumValue = 0.24f,
        maximumValue = 0.90f
    )

    val primarySeed = hslToColor(
        hue = primaryHsl.hue,
        saturation = primarySaturation,
        lightness = 0.50f
    )

    val secondarySeed = hslToColor(
        hue = normalizeHue(primaryHsl.hue + 24f),
        saturation = (primarySaturation * 0.55f).coerceIn(
            minimumValue = 0.16f,
            maximumValue = 0.50f
        ),
        lightness = 0.50f
    )

    val tertiarySeed = hslToColor(
        hue = normalizeHue(primaryHsl.hue + 60f),
        saturation = (primarySaturation * 0.75f).coerceIn(
            minimumValue = 0.24f,
            maximumValue = 0.68f
        ),
        lightness = 0.50f
    )

    val neutralSeed = hslToColor(
        hue = primaryHsl.hue,
        saturation = 0.035f,
        lightness = 0.50f
    )

    val neutralVariantSeed = hslToColor(
        hue = normalizeHue(primaryHsl.hue + 8f),
        saturation = 0.09f,
        lightness = 0.50f
    )

    val errorSeed = Color(0xFFB3261E)

    return GeneratedPalettes(
        primary = TonalPalette(primarySeed),
        secondary = TonalPalette(secondarySeed),
        tertiary = TonalPalette(tertiarySeed),
        neutral = TonalPalette(neutralSeed),
        neutralVariant = TonalPalette(neutralVariantSeed),
        error = TonalPalette(errorSeed)
    )
}