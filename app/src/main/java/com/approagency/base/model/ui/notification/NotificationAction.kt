package com.approagency.base.model.ui.notification

import android.app.PendingIntent
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput

data class NotificationAction(
    @param:DrawableRes val icon: Int,
    val title: CharSequence,
    val pendingIntent: PendingIntent,
    val remoteInputs: List<RemoteInput> = emptyList(),
    val allowGeneratedReplies: Boolean = true,
    val semanticAction: Int = NotificationCompat.Action.SEMANTIC_ACTION_NONE,
    val showsUserInterface: Boolean = true
)