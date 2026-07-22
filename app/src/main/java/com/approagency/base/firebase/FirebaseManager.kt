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
import com.approagency.base.session.SessionManager
import com.approagency.base.session.SessionState
import com.approagency.base.utils.Logger
import com.approagency.base.utils.NotificationManager
import com.approagency.base.utils.awaitCompletion
import com.approagency.base.utils.awaitResult
import com.approagency.base.utils.isAppForeground
import com.approagency.base.utils.toFirebaseMessage
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class FirebaseManager(
    context: Context,
    private val approConfig: ApproConfig,
    private val repository: ApproRepository,
    private val notificationManager: NotificationManager,
    private val config: FirebaseConfig,
    private val sessionManager: SessionManager
) {
    private val context = context.applicationContext

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private val initialized = AtomicBoolean(false)
    private val tokenSyncMutex = Mutex()

    private var sessionJob: Job? = null
    private var lastSubmittedToken: String? = null

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _messages = MutableSharedFlow<FirebaseMessage>(
        replay = 1,
        extraBufferCapacity = 2,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    val messages: SharedFlow<FirebaseMessage> =
        _messages.asSharedFlow()

    private val isLoggedIn: Boolean
        get() = sessionManager.state.value is SessionState.Login

    fun initialize(): Boolean {
        if (initialized.get()) {
            Logger.debug(
                TAG,
                "FirebaseManager already initialized"
            )

            return true
        }

        val firebaseApp = FirebaseApp
            .getApps(context)
            .firstOrNull()
            ?: FirebaseApp.initializeApp(context)

        if (firebaseApp == null) {
            Logger.error(
                TAG,
                "FirebaseApp initialization failed"
            )

            return false
        }

        FirebaseMessaging.getInstance()
            .isAutoInitEnabled = false

        notificationManager.createChannelGroup(
            config.channelGroup
        )

        notificationManager.createChannel(
            config.channel.copy(
                groupId = config.channelGroup.id
            )
        )

        initialized.set(true)

        Logger.info(
            TAG,
            "FirebaseManager initialized package=${approConfig.packageName}"
        )

        observeSession()

        return true
    }

    private fun observeSession() {
        sessionJob?.cancel()

        sessionJob = scope.launch {
            sessionManager.state
                .map { state ->
                    state is SessionState.Login
                }
                .distinctUntilChanged()
                .collectLatest { loggedIn ->
                    Logger.info(
                        TAG,
                        "Session changed loggedIn=$loggedIn"
                    )

                    if (loggedIn) {
                        activateForLoggedInUser()
                    } else {
                        deactivateForLoggedOutUser()
                    }
                }
        }
    }

    private suspend fun activateForLoggedInUser() {
        ensureInitialized()

        if (!isLoggedIn) {
            Logger.warning(
                TAG,
                "FCM activation ignored because user is logged out"
            )

            return
        }

        if (!config.autoInitEnabled) {
            FirebaseMessaging.getInstance()
                .isAutoInitEnabled = false

            Logger.warning(
                TAG,
                "FCM activation ignored because autoInitEnabled=false"
            )

            return
        }

        val messaging = FirebaseMessaging.getInstance()

        messaging.isAutoInitEnabled = true

        Logger.info(
            TAG,
            "FCM enabled for logged-in user"
        )

        val currentToken = runCatching {
            @Suppress("DEPRECATION")
            messaging.token.awaitResult()
        }.onFailure { throwable ->
            Logger.error(
                tag = TAG,
                message = "Failed to retrieve FCM token",
                throwable = throwable
            )
        }.getOrNull() ?: return

        if (!isLoggedIn) {
            Logger.warning(
                TAG,
                "User logged out while retrieving FCM token"
            )

            return
        }

        submitToken(
            token = currentToken,
            source = "session_login"
        )
    }

    private suspend fun submitToken(
        token: String,
        source: String
    ) {
        tokenSyncMutex.withLock {
            if (!isLoggedIn) {
                Logger.warning(
                    TAG,
                    "Token submission ignored because user is logged out source=$source"
                )

                return@withLock
            }

            if (!config.autoInitEnabled) {
                Logger.warning(
                    TAG,
                    "Token submission ignored because FCM is disabled source=$source"
                )

                return@withLock
            }

            _token.value = token

            if (lastSubmittedToken == token) {
                Logger.debug(
                    TAG,
                    "Duplicate token skipped source=$source token=${Logger.maskToken(token)}"
                )

                return@withLock
            }

            lastSubmittedToken = token

            Logger.info(
                TAG,
                "Submitting FCM token source=$source token=${Logger.maskToken(token)}"
            )

            try {
                repository.sendFCMToken(
                    token = token,
                    packageName = approConfig.packageName
                ).collect { result ->
                    Logger.debug(
                        TAG,
                        "FCM token backend result=$result"
                    )
                }

                Logger.info(
                    TAG,
                    "FCM token request completed token=${Logger.maskToken(token)}"
                )

                if (isLoggedIn) {
                    config.onTokenChanged(token)
                }
            } catch (throwable: Throwable) {
                if (lastSubmittedToken == token) {
                    lastSubmittedToken = null
                }

                Logger.error(
                    tag = TAG,
                    message = "FCM token submission failed source=$source",
                    throwable = throwable
                )
            }
        }
    }

    private suspend fun deactivateForLoggedOutUser() {
        tokenSyncMutex.withLock {
            if (!initialized.get()) {
                _token.value = null
                lastSubmittedToken = null
                return@withLock
            }

            val messaging = FirebaseMessaging.getInstance()

            messaging.isAutoInitEnabled = false

            _token.value = null
            lastSubmittedToken = null

            Logger.info(
                TAG,
                "FCM disabled for logged-out user"
            )

            runCatching {
                @Suppress("DEPRECATION")
                messaging
                    .deleteToken()
                    .awaitCompletion()
            }.onSuccess {
                Logger.info(
                    TAG,
                    "Local FCM token deleted"
                )
            }.onFailure { throwable ->
                Logger.error(
                    tag = TAG,
                    message = "Failed to delete local FCM token",
                    throwable = throwable
                )
            }
        }
    }

    suspend fun getToken(): String {
        ensureInitialized()

        check(isLoggedIn) {
            "User must be logged in before requesting an FCM token"
        }

        check(config.autoInitEnabled) {
            "Firebase messaging is disabled"
        }

        val messaging = FirebaseMessaging.getInstance()

        messaging.isAutoInitEnabled = true

        @Suppress("DEPRECATION")
        val currentToken = messaging
            .token
            .awaitResult()

        submitToken(
            token = currentToken,
            source = "manual_get_token"
        )

        return currentToken
    }

    fun refreshToken() {
        scope.launch {
            if (isLoggedIn) {
                activateForLoggedInUser()
            } else {
                Logger.warning(
                    TAG,
                    "Token refresh ignored because user is logged out"
                )
            }
        }
    }

    suspend fun deleteToken() {
        deactivateForLoggedOutUser()
    }

    suspend fun subscribeToTopic(
        topic: String
    ) {
        ensureInitialized()

        check(isLoggedIn) {
            "User must be logged in before subscribing to a topic"
        }

        FirebaseMessaging.getInstance()
            .subscribeToTopic(topic)
            .awaitCompletion()

        Logger.info(
            TAG,
            "Subscribed to topic=$topic"
        )
    }

    suspend fun unsubscribeFromTopic(
        topic: String
    ) {
        ensureInitialized()

        FirebaseMessaging.getInstance()
            .unsubscribeFromTopic(topic)
            .awaitCompletion()

        Logger.info(
            TAG,
            "Unsubscribed from topic=$topic"
        )
    }

    fun requestNotificationPermission(
        activity: Activity
    ) {
        Logger.info(
            TAG,
            "Requesting notification permission"
        )

        notificationManager.requestPermission(activity)
    }

    fun hasNotificationPermission(): Boolean {
        val granted = notificationManager.hasPermission()

        Logger.debug(
            TAG,
            "Notification permission granted=$granted"
        )

        return granted
    }

    fun openNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.openNotificationSettings(context)
        }
    }

    fun showNotification(
        message: FirebaseMessage
    ) {
        if (!isLoggedIn) {
            Logger.warning(
                TAG,
                "Manual notification ignored because user is logged out"
            )

            return
        }

        scope.launch {
            showFirebaseNotification(message)
        }
    }

    internal fun handleNewToken(
        token: String
    ) {
        Logger.info(
            TAG,
            "New FCM token received loggedIn=$isLoggedIn token=${Logger.maskToken(token)}"
        )

        scope.launch {
            if (!isLoggedIn) {
                FirebaseMessaging.getInstance()
                    .isAutoInitEnabled = false

                _token.value = null

                Logger.warning(
                    TAG,
                    "New FCM token ignored because user is logged out"
                )

                return@launch
            }

            submitToken(
                token = token,
                source = "on_new_token"
            )
        }
    }

    internal fun handleMessage(
        remoteMessage: RemoteMessage
    ) {
        val foreground = context.isAppForeground()

        Logger.info(
            TAG,
            buildString {
                append("FCM message received")
                append(" id=")
                append(remoteMessage.messageId)
                append(" from=")
                append(remoteMessage.from)
                append(" loggedIn=")
                append(isLoggedIn)
                append(" foreground=")
                append(foreground)
                append(" data=")
                append(remoteMessage.data)
                append(" notificationPayload=")
                append(remoteMessage.notification != null)
            }
        )

        if (!isLoggedIn) {
            Logger.warning(
                TAG,
                "FCM message ignored because user is logged out"
            )

            return
        }

        val message = remoteMessage.toFirebaseMessage()

        if (!isAllowedForCurrentFlavor(message.data)) {
            Logger.info(
                tag = TAG,
                message = buildString {
                    append("FCM ignored. ")
                    append("Current flavor=${approConfig.flavor}, ")
                    append(
                        "target=${
                            message.data[
                                ApproConstants.FIREBASE_FLAVOR
                            ]
                        }"
                    )
                }
            )

            return
        }

        _messages.tryEmit(message)

        scope.launch {
            if (!isLoggedIn) {
                Logger.warning(
                    TAG,
                    "FCM message cancelled because user logged out"
                )

                return@launch
            }

            runCatching {
                config.onMessageReceived(message)
            }.onFailure { throwable ->
                Logger.error(
                    tag = TAG,
                    message = "onMessageReceived callback failed",
                    throwable = throwable
                )
            }

            if (!isLoggedIn) {
                return@launch
            }

            val accepted = runCatching {
                config.notificationFilter(message)
            }.onFailure { throwable ->
                Logger.error(
                    tag = TAG,
                    message = "Notification filter failed",
                    throwable = throwable
                )
            }.getOrDefault(false)

            if (!accepted) {
                Logger.warning(
                    TAG,
                    "FCM message rejected by notificationFilter"
                )

                return@launch
            }

            val appForeground = context.isAppForeground()

            val shouldShow = if (appForeground) {
                config.showForegroundNotifications
            } else {
                config.showBackgroundNotifications
            }

            val permissionGranted =
                notificationManager.hasPermission()

            Logger.info(
                TAG,
                buildString {
                    append("Notification decision")
                    append(" foreground=")
                    append(appForeground)
                    append(" showForeground=")
                    append(config.showForegroundNotifications)
                    append(" showBackground=")
                    append(config.showBackgroundNotifications)
                    append(" shouldShow=")
                    append(shouldShow)
                    append(" permission=")
                    append(permissionGranted)
                }
            )

            if (!shouldShow) {
                Logger.warning(
                    TAG,
                    "Notification disabled for current app state"
                )

                return@launch
            }

            if (!permissionGranted) {
                Logger.warning(
                    TAG,
                    "Notification not shown because permission is missing"
                )

                return@launch
            }

            showFirebaseNotification(message)
        }
    }

    fun close() {
        sessionJob?.cancel()
        sessionJob = null
        scope.cancel()

        Logger.info(
            TAG,
            "FirebaseManager closed"
        )
    }

    private suspend fun showFirebaseNotification(
        message: FirebaseMessage
    ) {
        if (!isLoggedIn) {
            Logger.warning(
                TAG,
                "Notification ignored because user is logged out"
            )

            return
        }

        if (!notificationManager.hasPermission()) {
            Logger.warning(
                TAG,
                "Notification not shown because permission is missing"
            )

            return
        }

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
                }.onFailure { throwable ->
                    Logger.warning(
                        tag = TAG,
                        message = "Failed to download notification image",
                        throwable = throwable
                    )
                }.getOrNull()
            }

        if (!isLoggedIn) {
            Logger.warning(
                TAG,
                "Notification cancelled because user logged out"
            )

            return
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

        val requestedId =
            message.data[ApproConstants.FIREBASE_ID]
                ?.hashCode()
                ?: System.currentTimeMillis().hashCode()

        val shownId = notificationManager.show(
            NotificationRequest(
                id = requestedId,
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

        if (shownId == null) {
            Logger.error(
                TAG,
                "NotificationHelper failed to show notification"
            )
        } else {
            Logger.info(
                TAG,
                "Notification shown id=$shownId channel=${config.channel.id}"
            )
        }
    }

    private fun createContentIntent(
        message: FirebaseMessage
    ): PendingIntent? {
        val selectedLink = resolveFirebaseLink(
            data = message.data
        )

        val requestCode = message.data[
            ApproConstants.FIREBASE_ID
        ]?.hashCode()
            ?: selectedLink?.hashCode()
            ?: System.currentTimeMillis().hashCode()

        val pendingIntentFlags =
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE

        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(
                context.packageName
            )
            ?: run {
                Logger.error(
                    TAG,
                    "Application launch intent was not found"
                )

                return null
            }

        launchIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

        launchIntent.putExtra(
            ApproConstants.LINK,
            selectedLink
        )

        launchIntent.putExtra(
            ApproConstants.DATA,
            JSONObject(message.data).toString()
        )

        return PendingIntent.getActivity(
            context,
            requestCode,
            launchIntent,
            pendingIntentFlags
        )
    }

    private fun isAllowedForCurrentFlavor(
        data: Map<String, String>
    ): Boolean {
        val rawFlavor = data[
            ApproConstants.FIREBASE_FLAVOR
        ]?.trim()

        if (rawFlavor.isNullOrBlank()) {
            return true
        }

        val allowedFlavors = rawFlavor
            .split(",")
            .mapNotNull { normalizeFlavor(it) }
            .toSet()

        val currentFlavor = when {
            approConfig.isMyket() -> "myket"
            approConfig.isBazaar() -> "bazar"
            approConfig.isGooglePlay() -> "googleplay"
            else -> return false
        }

        return currentFlavor in allowedFlavors
    }

    private fun normalizeFlavor(
        value: String
    ): String? {
        return when (
            value
                .trim()
                .lowercase(Locale.ROOT)
                .replace("_", "")
                .replace("-", "")
                .replace(" ", "")
        ) {
            "myket" -> "myket"

            "bazar",
            "bazaar" -> "bazar"

            "googleplay" -> "googleplay"

            else -> null
        }
    }

    private fun resolveFirebaseLink(
        data: Map<String, String>
    ): String? {
        val flavorUrlKey = when {
            approConfig.isBazaar() ->
                ApproConstants.FIREBASE_URL_BAZAR

            approConfig.isMyket() ->
                ApproConstants.FIREBASE_URL_MYKET

            approConfig.isGooglePlay() ->
                ApproConstants.FIREBASE_URL_GOOGLE_PLAY

            else -> null
        }

        val flavorUrl = flavorUrlKey
            ?.let(data::get)
            ?.trim()
            ?.takeIf(String::isNotEmpty)

        val fallbackLink = data[
            ApproConstants.FIREBASE_LINK
        ]
            ?.trim()
            ?.takeIf(String::isNotEmpty)

        return flavorUrl ?: fallbackLink
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
                    Logger.warning(
                        TAG,
                        "Notification image request failed code=${connection.responseCode}"
                    )

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

    companion object {
        private const val TAG = "ApproFirebase"
    }
}