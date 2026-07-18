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
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
import com.approagency.base.R
import com.approagency.base.config.ApproConstants
import com.approagency.base.firebase.FirebaseMessage
import com.approagency.base.model.UiText
import com.approagency.base.model.ui.Icon
import com.approagency.base.model.ui.Label
import com.approagency.base.model.ui.deepLink.DeepLinkInput
import com.approagency.base.presentation.BaseActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import org.json.JSONObject
import retrofit2.Retrofit
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.resumeWithException

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