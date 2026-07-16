package com.approagency.base.model.session

import com.approagency.base.local.room.entity.SessionEntity

data class Session(
    val id: String = ID,
    val approToken: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val tokenType: String? = null,
    val expiresAt: Long? = null,
    val userId: String? = null,
    val phoneNumber: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val isPremium: Boolean = false
) {
    val fullName: String
        get() = listOfNotNull(
            firstName, lastName
        ).filter {
            it.isNotBlank()
        }.joinToString(" ")

    fun isExpired(
        now: Long = System.currentTimeMillis()
    ): Boolean {
        return expiresAt?.let {
            now >= it
        } ?: false
    }

    fun toEntity() =
        SessionEntity(
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
            isPremium = isPremium
        )

    companion object {
        const val ID = "key"
    }
}