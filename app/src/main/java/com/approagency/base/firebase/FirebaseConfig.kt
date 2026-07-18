package com.approagency.base.firebase

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.approagency.base.model.ui.notification.NotificationChannelConfig
import com.approagency.base.model.ui.notification.NotificationChannelGroupConfig

data class FirebaseConfig(
    @param:DrawableRes val smallIcon: Int,
    val channelGroup: NotificationChannelGroupConfig,
    val channel: NotificationChannelConfig,
    @param:ColorInt val notificationColor: Int? = null,
    val defaultTitle: String? = null,
    val autoInitEnabled: Boolean = true,
    val showForegroundNotifications: Boolean = true,
    val showBackgroundNotifications: Boolean = true,
    val notificationFilter: (FirebaseMessage) -> Boolean = { true },
    val onTokenChanged: suspend (String) -> Unit = {},
    val onMessageReceived: suspend (FirebaseMessage) -> Unit = {}
)