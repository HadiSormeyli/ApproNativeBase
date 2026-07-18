package com.approagency.base.model.network

import com.approagency.base.model.ui.UiState

sealed class Resource<out T> {
    class Success<T>(val data: T) : Resource<T>()
    class Error<T>(val error: Failure, val data: T? = null) : Resource<T>()
    class Loading<T>(val data: T? = null) : Resource<T>()


    fun toUiState(): UiState<T> {
        return when (this) {
            is Success -> UiState.Success(data)
            is Error -> UiState.Error(error, data)
            is Loading -> UiState.Loading(data)
        }
    }
}