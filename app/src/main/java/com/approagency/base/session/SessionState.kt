package com.approagency.base.session

import com.approagency.base.model.session.Session

sealed interface SessionState{
    data object Loading:SessionState

    data object Logout:SessionState

    data class Login(
        val session:Session
    ):SessionState
}