package com.approagency.base.firebase

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.approagency.base.config.ApproConfig
import com.approagency.base.config.ApproConstants
import com.approagency.base.model.ui.notification.NotificationRequest
import com.approagency.base.network.repository.ApproRepository
import com.approagency.base.utils.NotificationHelper
import com.approagency.base.utils.awaitCompletion
import com.approagency.base.utils.awaitResult
import com.approagency.base.utils.isAppForeground
import com.approagency.base.utils.toFirebaseMessage
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean


class FirebaseManager(
    context: Context,
    private val approConfig: ApproConfig,
    private val repository: ApproRepository,
    private val notificationHelper: NotificationHelper,
    private val config: FirebaseConfig
) {
    private val context = context.applicationContext

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private val initialized = AtomicBoolean(false)

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _messages = MutableSharedFlow<FirebaseMessage>(
        replay = 1,
        extraBufferCapacity = 2,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    val messages: SharedFlow<FirebaseMessage> =
        _messages.asSharedFlow()

    fun initialize(): Boolean {
        if (initialized.get()) {
            return true
        }

        FirebaseApp.getApps(context).firstOrNull()
            ?: FirebaseApp.initializeApp(context)
            ?: return false

        FirebaseMessaging.getInstance()
            .isAutoInitEnabled = config.autoInitEnabled

        notificationHelper.createChannelGroup(
            config.channelGroup
        )

        notificationHelper.createChannel(
            config.channel.copy(
                groupId = config.channelGroup.id
            )
        )

        initialized.set(true)
        refreshToken()

        return true
    }

    suspend fun getToken(): String {
        ensureInitialized()

        val token = FirebaseMessaging.getInstance()
            .token
            .awaitResult()

        _token.value = token

        return token
    }

    fun refreshToken() {
        scope.launch {
            runCatching {
                getToken()
            }
        }
    }

    suspend fun deleteToken() {
        ensureInitialized()

        FirebaseMessaging.getInstance()
            .deleteToken()
            .awaitCompletion()

        _token.value = null
    }

    suspend fun subscribeToTopic(topic: String) {
        ensureInitialized()

        FirebaseMessaging.getInstance()
            .subscribeToTopic(topic)
            .awaitCompletion()
    }

    suspend fun unsubscribeFromTopic(topic: String) {
        ensureInitialized()

        FirebaseMessaging.getInstance()
            .unsubscribeFromTopic(topic)
            .awaitCompletion()
    }

    fun requestNotificationPermission(
        activity: Activity
    ) {
        notificationHelper.requestPermission(activity)
    }

    fun hasNotificationPermission(): Boolean {
        return notificationHelper.hasPermission()
    }

    fun openNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationHelper.openNotificationSettings(context)
        }
    }

    fun showNotification(
        message: FirebaseMessage
    ) {
        scope.launch {
            showFirebaseNotification(message)
        }
    }

    internal fun handleNewToken(token: String) {
        _token.value = token

        scope.launch {
            launch {
                repository.sendFCMToken(token, approConfig.packageName).collect { }
            }

            config.onTokenChanged(token)
        }
    }

    internal fun handleMessage(
        remoteMessage: RemoteMessage
    ) {
        val message = remoteMessage.toFirebaseMessage()

        _messages.tryEmit(message)

        scope.launch {
            config.onMessageReceived(message)

            if (!config.notificationFilter(message)) {
                return@launch
            }

            val shouldShow = if (context.isAppForeground()) {
                config.showForegroundNotifications
            } else {
                config.showBackgroundNotifications
            }

            if (shouldShow) {
                showFirebaseNotification(message)
            }
        }
    }

    fun close() {
        scope.cancel()
    }

    private suspend fun showFirebaseNotification(
        message: FirebaseMessage
    ) {
        val title = message.title
            ?: config.defaultTitle
            ?: context.applicationInfo
                .loadLabel(context.packageManager)
                .toString()

        val image = message.imageUrl
            ?.takeIf(String::isNotBlank)
            ?.let { imageUrl ->
                runCatching {
                    downloadBitmap(imageUrl)
                }.getOrNull()
            }

        val style = if (image != null) {
            NotificationCompat.BigPictureStyle()
                .bigPicture(image)
                .setBigContentTitle(title)
                .setSummaryText(message.description)
        } else {
            NotificationCompat.BigTextStyle()
                .bigText(message.description)
        }

        notificationHelper.show(
            NotificationRequest(
                id = message.data[ApproConstants.FIREBASE_ID]?.hashCode()
                    ?: System.currentTimeMillis().hashCode(),
                channelId = config.channel.id,
                smallIcon = config.smallIcon,
                title = title,
                text = message.description,
                color = config.notificationColor,
                largeIcon = image,
                style = style,
                contentIntent = createContentIntent(message)
            )
        )
    }

    private fun createContentIntent(
        message: FirebaseMessage
    ): PendingIntent? {
        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?: return null

        launchIntent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

        message.data[ApproConstants.FIREBASE_LINK]?.let { link ->
            launchIntent.putExtra(
                ApproConstants.LINK,
                link
            )
        }

        launchIntent.putExtra(
            ApproConstants.DATA,
            JSONObject(message.data).toString()
        )

        return PendingIntent.getActivity(
            context,
            message.data[ApproConstants.FIREBASE_ID]?.hashCode()
                ?: System.currentTimeMillis().hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    private suspend fun downloadBitmap(
        imageUrl: String
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            val connection = URL(imageUrl)
                .openConnection() as HttpURLConnection

            try {
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.doInput = true
                connection.connect()

                if (
                    connection.responseCode !in
                    HttpURLConnection.HTTP_OK until
                    HttpURLConnection.HTTP_MULT_CHOICE
                ) {
                    return@withContext null
                }

                connection.inputStream.use(
                    BitmapFactory::decodeStream
                )
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun ensureInitialized() {
        check(initialize()) {
            "Firebase initialization failed"
        }
    }
}