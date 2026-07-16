package com.approagency.base.local.room.entity


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.approagency.base.model.session.Session


@Entity(
    tableName = "session"
)
data class SessionEntity(
    @PrimaryKey
    val id: String = "key",
    val approToken: String?,
    val accessToken: String?,
    val refreshToken: String?,
    val tokenType: String?,
    val expiresAt: Long?,
    val userId: String?,
    val phoneNumber: String?,
    val firstName: String?,
    val lastName: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isPremium: Boolean = false
) {
    fun toModel() =
        Session(
            id = id,
            approToken = approToken,
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = tokenType,
            expiresAt = expiresAt,
            userId = userId,
            phoneNumber = phoneNumber,
            firstName = firstName,
            lastName = lastName,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isPremium = isPremium,
        )
}