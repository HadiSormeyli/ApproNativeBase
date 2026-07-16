package com.approagency.base.model

import com.approagency.base.model.network.Failure

sealed class UiState<out T> {
    class Success<T>(val data: T) : UiState<T>()
    class Error<T>(val error: Failure, val data: T? = null) : UiState<T>()
    class Loading<T>(val data: T? = null) : UiState<T>()
    class Idle<T> : UiState<T>()
}