package com.approagency.base.model.ui.notification

import android.app.Notification
import android.app.NotificationManager
import android.media.AudioAttributes
import android.net.Uri
import androidx.annotation.ColorInt

data class NotificationChannelConfig(
    val id: String,
    val name: String,
    val description: String? = null,
    val importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
    val groupId: String? = null,
    val showBadge: Boolean = true,
    val enableLights: Boolean = false,
    @param:ColorInt val lightColor: Int? = null,
    val enableVibration: Boolean = true,
    val vibrationPattern: LongArray? = null,
    val sound: Uri? = null,
    val useDefaultSound: Boolean = true,
    val audioUsage: Int = AudioAttributes.USAGE_NOTIFICATION,
    val bypassDnd: Boolean = false,
    val lockscreenVisibility: Int = Notification.VISIBILITY_PRIVATE
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotificationChannelConfig

        if (importance != other.importance) return false
        if (showBadge != other.showBadge) return false
        if (enableLights != other.enableLights) return false
        if (lightColor != other.lightColor) return false
        if (enableVibration != other.enableVibration) return false
        if (useDefaultSound != other.useDefaultSound) return false
        if (audioUsage != other.audioUsage) return false
        if (bypassDnd != other.bypassDnd) return false
        if (lockscreenVisibility != other.lockscreenVisibility) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (groupId != other.groupId) return false
        if (!vibrationPattern.contentEquals(other.vibrationPattern)) return false
        if (sound != other.sound) return false

        return true
    }

    override fun hashCode(): Int {
        var result = importance
        result = 31 * result + showBadge.hashCode()
        result = 31 * result + enableLights.hashCode()
        result = 31 * result + (lightColor ?: 0)
        result = 31 * result + enableVibration.hashCode()
        result = 31 * result + useDefaultSound.hashCode()
        result = 31 * result + audioUsage
        result = 31 * result + bypassDnd.hashCode()
        result = 31 * result + lockscreenVisibility
        result = 31 * result + id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (groupId?.hashCode() ?: 0)
        result = 31 * result + (vibrationPattern?.contentHashCode() ?: 0)
        result = 31 * result + (sound?.hashCode() ?: 0)
        return result
    }
}