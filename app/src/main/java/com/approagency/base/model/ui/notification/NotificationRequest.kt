package com.approagency.base.model.ui.notification

import android.app.Notification
import android.app.PendingIntent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.Person

data class NotificationRequest(
    val id: Int? = null,
    val tag: String? = null,
    val channelId: String,
    @param:DrawableRes val smallIcon: Int,
    val title: CharSequence? = null,
    val text: CharSequence? = null,
    val subText: CharSequence? = null,
    val info: CharSequence? = null,
    val ticker: CharSequence? = null,
    val style: NotificationCompat.Style? = null,
    val largeIcon: Bitmap? = null,
    @param:ColorInt val color: Int? = null,
    val colorized: Boolean? = null,
    val contentIntent: PendingIntent? = null,
    val deleteIntent: PendingIntent? = null,
    val fullScreenIntent: PendingIntent? = null,
    val actions: List<NotificationAction> = emptyList(),
    val people: List<Person> = emptyList(),
    val publicVersion: Notification? = null,
    val customContentView: RemoteViews? = null,
    val customBigContentView: RemoteViews? = null,
    val customHeadsUpContentView: RemoteViews? = null,
    val autoCancel: Boolean = true,
    val ongoing: Boolean = false,
    val onlyAlertOnce: Boolean = false,
    val silent: Boolean = false,
    val localOnly: Boolean = false,
    val showWhen: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val usesChronometer: Boolean = false,
    val chronometerCountDown: Boolean = false,
    val priority: Int = NotificationCompat.PRIORITY_DEFAULT,
    val visibility: Int = NotificationCompat.VISIBILITY_PRIVATE,
    val category: String? = null,
    val groupKey: String? = null,
    val groupSummary: Boolean = false,
    val groupAlertBehavior: Int = NotificationCompat.GROUP_ALERT_ALL,
    val sortKey: String? = null,
    val number: Int? = null,
    val badgeIconType: Int = NotificationCompat.BADGE_ICON_NONE,
    val timeoutAfter: Long? = null,
    val shortcutId: String? = null,
    val sound: Uri? = null,
    val vibrationPattern: LongArray? = null,
    val defaults: Int = NotificationCompat.DEFAULT_ALL,
    val progressMax: Int = 0,
    val progress: Int = 0,
    val progressIndeterminate: Boolean = false,
    val builder: NotificationCompat.Builder.() -> Unit = {}
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotificationRequest

        if (id != other.id) return false
        if (smallIcon != other.smallIcon) return false
        if (color != other.color) return false
        if (colorized != other.colorized) return false
        if (autoCancel != other.autoCancel) return false
        if (ongoing != other.ongoing) return false
        if (onlyAlertOnce != other.onlyAlertOnce) return false
        if (silent != other.silent) return false
        if (localOnly != other.localOnly) return false
        if (showWhen != other.showWhen) return false
        if (timestamp != other.timestamp) return false
        if (usesChronometer != other.usesChronometer) return false
        if (chronometerCountDown != other.chronometerCountDown) return false
        if (priority != other.priority) return false
        if (visibility != other.visibility) return false
        if (groupSummary != other.groupSummary) return false
        if (groupAlertBehavior != other.groupAlertBehavior) return false
        if (number != other.number) return false
        if (badgeIconType != other.badgeIconType) return false
        if (timeoutAfter != other.timeoutAfter) return false
        if (defaults != other.defaults) return false
        if (progressMax != other.progressMax) return false
        if (progress != other.progress) return false
        if (progressIndeterminate != other.progressIndeterminate) return false
        if (tag != other.tag) return false
        if (channelId != other.channelId) return false
        if (title != other.title) return false
        if (text != other.text) return false
        if (subText != other.subText) return false
        if (info != other.info) return false
        if (ticker != other.ticker) return false
        if (style != other.style) return false
        if (largeIcon != other.largeIcon) return false
        if (contentIntent != other.contentIntent) return false
        if (deleteIntent != other.deleteIntent) return false
        if (fullScreenIntent != other.fullScreenIntent) return false
        if (actions != other.actions) return false
        if (people != other.people) return false
        if (publicVersion != other.publicVersion) return false
        if (customContentView != other.customContentView) return false
        if (customBigContentView != other.customBigContentView) return false
        if (customHeadsUpContentView != other.customHeadsUpContentView) return false
        if (category != other.category) return false
        if (groupKey != other.groupKey) return false
        if (sortKey != other.sortKey) return false
        if (shortcutId != other.shortcutId) return false
        if (sound != other.sound) return false
        if (!vibrationPattern.contentEquals(other.vibrationPattern)) return false
        if (builder != other.builder) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + smallIcon
        result = 31 * result + (color ?: 0)
        result = 31 * result + (colorized?.hashCode() ?: 0)
        result = 31 * result + autoCancel.hashCode()
        result = 31 * result + ongoing.hashCode()
        result = 31 * result + onlyAlertOnce.hashCode()
        result = 31 * result + silent.hashCode()
        result = 31 * result + localOnly.hashCode()
        result = 31 * result + showWhen.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + usesChronometer.hashCode()
        result = 31 * result + chronometerCountDown.hashCode()
        result = 31 * result + priority
        result = 31 * result + visibility
        result = 31 * result + groupSummary.hashCode()
        result = 31 * result + groupAlertBehavior
        result = 31 * result + (number ?: 0)
        result = 31 * result + badgeIconType
        result = 31 * result + (timeoutAfter?.hashCode() ?: 0)
        result = 31 * result + defaults
        result = 31 * result + progressMax
        result = 31 * result + progress
        result = 31 * result + progressIndeterminate.hashCode()
        result = 31 * result + (tag?.hashCode() ?: 0)
        result = 31 * result + channelId.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + (subText?.hashCode() ?: 0)
        result = 31 * result + (info?.hashCode() ?: 0)
        result = 31 * result + (ticker?.hashCode() ?: 0)
        result = 31 * result + (style?.hashCode() ?: 0)
        result = 31 * result + (largeIcon?.hashCode() ?: 0)
        result = 31 * result + (contentIntent?.hashCode() ?: 0)
        result = 31 * result + (deleteIntent?.hashCode() ?: 0)
        result = 31 * result + (fullScreenIntent?.hashCode() ?: 0)
        result = 31 * result + actions.hashCode()
        result = 31 * result + people.hashCode()
        result = 31 * result + (publicVersion?.hashCode() ?: 0)
        result = 31 * result + (customContentView?.hashCode() ?: 0)
        result = 31 * result + (customBigContentView?.hashCode() ?: 0)
        result = 31 * result + (customHeadsUpContentView?.hashCode() ?: 0)
        result = 31 * result + (category?.hashCode() ?: 0)
        result = 31 * result + (groupKey?.hashCode() ?: 0)
        result = 31 * result + (sortKey?.hashCode() ?: 0)
        result = 31 * result + (shortcutId?.hashCode() ?: 0)
        result = 31 * result + (sound?.hashCode() ?: 0)
        result = 31 * result + (vibrationPattern?.contentHashCode() ?: 0)
        result = 31 * result + builder.hashCode()
        return result
    }
}
