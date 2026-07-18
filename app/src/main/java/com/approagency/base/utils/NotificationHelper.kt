package com.approagency.base.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.approagency.base.model.ui.notification.NotificationChannelConfig
import com.approagency.base.model.ui.notification.NotificationChannelGroupConfig
import com.approagency.base.model.ui.notification.NotificationRequest
import java.util.concurrent.atomic.AtomicInteger

class NotificationHelper(
    context: Context
) {
    private val context = context.applicationContext
    private val manager = NotificationManagerCompat.from(this.context)
    private val systemManager =
        this.context.getSystemService(NotificationManager::class.java)

    private val idGenerator = AtomicInteger(
        (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    )

    companion object {
        const val REQUEST_CODE = 9001
    }

    @SuppressLint("AnnotateVersionCheck")
    fun isRequired(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    fun hasPermission(): Boolean {
        val permissionGranted =
            !isRequired() ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

        return permissionGranted &&
                NotificationManagerCompat
                    .from(context)
                    .areNotificationsEnabled()
    }

    fun shouldShowPermissionRationale(activity: Activity): Boolean {
        return isRequired() &&
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
    }

    fun requestPermission(
        activity: Activity,
        requestCode: Int = REQUEST_CODE
    ) {
        if (!isRequired() || hasPermission()) return

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            requestCode
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun openNotificationSettings(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    fun areNotificationsEnabled(): Boolean {
        return manager.areNotificationsEnabled()
    }

    fun createChannelGroup(
        config: NotificationChannelGroupConfig
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        systemManager.createNotificationChannelGroup(
            NotificationChannelGroup(
                config.id,
                config.name
            ).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    description = config.description
                }
            }
        )
    }

    fun createChannelGroups(
        groups: List<NotificationChannelGroupConfig>
    ) {
        groups.forEach(::createChannelGroup)
    }

    fun createChannel(
        config: NotificationChannelConfig
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(config.audioUsage)
            .build()

        systemManager.createNotificationChannel(
            NotificationChannel(
                config.id,
                config.name,
                config.importance
            ).apply {
                description = config.description
                group = config.groupId
                setShowBadge(config.showBadge)
                enableLights(config.enableLights)
                config.lightColor?.let(::setLightColor)
                enableVibration(config.enableVibration)
                vibrationPattern = config.vibrationPattern
                setBypassDnd(config.bypassDnd)
                lockscreenVisibility = config.lockscreenVisibility

                when {
                    config.sound != null -> {
                        setSound(config.sound, audioAttributes)
                    }

                    config.useDefaultSound -> {
                        setSound(
                            RingtoneManager.getDefaultUri(
                                RingtoneManager.TYPE_NOTIFICATION
                            ),
                            audioAttributes
                        )
                    }

                    else -> {
                        setSound(null, null)
                    }
                }
            }
        )
    }

    fun createChannels(
        channels: List<NotificationChannelConfig>
    ) {
        channels.forEach(::createChannel)
    }

    fun deleteChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            systemManager.deleteNotificationChannel(channelId)
        }
    }

    fun deleteChannelGroup(groupId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            systemManager.deleteNotificationChannelGroup(groupId)
        }
    }

    fun build(
        request: NotificationRequest
    ): Notification {
        val builder = NotificationCompat.Builder(
            context,
            request.channelId
        )
            .setSmallIcon(request.smallIcon)
            .setContentTitle(request.title)
            .setContentText(request.text)
            .setSubText(request.subText)
            .setContentInfo(request.info)
            .setTicker(request.ticker)
            .setAutoCancel(request.autoCancel)
            .setOngoing(request.ongoing)
            .setOnlyAlertOnce(request.onlyAlertOnce)
            .setSilent(request.silent)
            .setLocalOnly(request.localOnly)
            .setShowWhen(request.showWhen)
            .setWhen(request.timestamp)
            .setUsesChronometer(request.usesChronometer)
            .setChronometerCountDown(request.chronometerCountDown)
            .setPriority(request.priority)
            .setVisibility(request.visibility)
            .setGroup(request.groupKey)
            .setGroupSummary(request.groupSummary)
            .setGroupAlertBehavior(request.groupAlertBehavior)
            .setSortKey(request.sortKey)
            .setBadgeIconType(request.badgeIconType)
            .setDefaults(request.defaults)
            .setProgress(
                request.progressMax,
                request.progress,
                request.progressIndeterminate
            )

        request.style?.let(builder::setStyle)
        request.largeIcon?.let(builder::setLargeIcon)
        request.color?.let(builder::setColor)
        request.colorized?.let(builder::setColorized)
        request.contentIntent?.let(builder::setContentIntent)
        request.deleteIntent?.let(builder::setDeleteIntent)

        request.fullScreenIntent?.let {
            builder.setFullScreenIntent(it, true)
        }

        request.category?.let(builder::setCategory)
        request.number?.let(builder::setNumber)
        request.timeoutAfter?.let(builder::setTimeoutAfter)
        request.shortcutId?.let(builder::setShortcutId)
        request.sound?.let(builder::setSound)
        request.vibrationPattern?.let(builder::setVibrate)
        request.publicVersion?.let(builder::setPublicVersion)
        request.customContentView?.let(builder::setCustomContentView)
        request.customBigContentView?.let(builder::setCustomBigContentView)
        request.customHeadsUpContentView?.let(
            builder::setCustomHeadsUpContentView
        )

        request.people.forEach(builder::addPerson)

        request.actions.forEach { action ->
            val actionBuilder = NotificationCompat.Action.Builder(
                action.icon,
                action.title,
                action.pendingIntent
            )
                .setAllowGeneratedReplies(
                    action.allowGeneratedReplies
                )
                .setSemanticAction(action.semanticAction)
                .setShowsUserInterface(
                    action.showsUserInterface
                )

            action.remoteInputs.forEach(
                actionBuilder::addRemoteInput
            )

            builder.addAction(actionBuilder.build())
        }

        builder.apply(request.builder)

        return builder.build()
    }

    @SuppressLint("MissingPermission")
    fun show(
        request: NotificationRequest
    ): Int? {
        if (!hasPermission()) return null

        val notificationId =
            request.id ?: idGenerator.incrementAndGet()

        val notification = build(request)

        if (request.tag == null) {
            manager.notify(
                notificationId,
                notification
            )
        } else {
            manager.notify(
                request.tag,
                notificationId,
                notification
            )
        }

        return notificationId
    }

    fun update(
        request: NotificationRequest
    ): Int? {
        requireNotNull(request.id)
        return show(request)
    }

    fun cancel(id: Int) {
        manager.cancel(id)
    }

    fun cancel(
        tag: String,
        id: Int
    ) {
        manager.cancel(tag, id)
    }

    fun cancelAll() {
        manager.cancelAll()
    }
}