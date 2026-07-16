package com.approagency.base.session

import com.approagency.base.local.room.dao.SessionDao
import com.approagency.base.local.room.entity.SessionEntity
import com.approagency.base.model.session.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SessionManager(
    private val dao: SessionDao,
) {
    private val mutex = Mutex()
    private val sessionScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    suspend fun getSession(): Session? {
        return dao.get()?.toModel()
    }

    fun loading() {
        setState(SessionState.Loading)
    }

    private fun setState(newState: SessionState) {
        _state.value = newState
    }

    fun login(session: Session) {
        sessionScope.launch {
            mutex.withLock {
                val old = dao.get(session.id)
                val now = System.currentTimeMillis()

                dao.insert(
                    SessionEntity(
                        id = session.id.ifBlank { Session.ID },
                        approToken = session.approToken,
                        accessToken = session.accessToken,
                        refreshToken = session.refreshToken,
                        tokenType = session.tokenType,
                        expiresAt = session.expiresAt,
                        userId = session.userId,
                        phoneNumber = session.phoneNumber,
                        firstName = session.firstName,
                        lastName = session.lastName,
                        createdAt = old?.createdAt ?: now,
                        updatedAt = now
                    )
                )

                setState(SessionState.Login(session))
            }
        }
    }

    fun updateTokens(
        id: String = Session.ID,
        approToken: String?,
        accessToken: String?,
        refreshToken: String?,
        expiresAt: Long?
    ) {
        sessionScope.launch {
            mutex.withLock {
                val current = dao.get() ?: return@launch

                dao.insert(
                    current.copy(
                        id = id,
                        approToken = approToken,
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        expiresAt = expiresAt,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun updateUser(
        id: String = Session.ID,
        userId: String?,
        phoneNumber: String?,
        firstName: String?,
        lastName: String?
    ) {
        sessionScope.launch {
            mutex.withLock {
                val current = dao.get() ?: return@launch

                dao.insert(
                    current.copy(
                        id = id,
                        userId = userId,
                        phoneNumber = phoneNumber,
                        firstName = firstName,
                        lastName = lastName,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun logout() {
        sessionScope.launch {
            mutex.withLock {
                dao.clear()

                setState(SessionState.Logout)
            }
        }
    }
}