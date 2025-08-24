package com.azgear.sendmoney.core.utils

sealed class ResultState<out T> {
    object Idle : ResultState<Nothing>()
    object Loading : ResultState<Nothing>()
    data class Success<T>(val data: T) : ResultState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : ResultState<Nothing>()
}

inline fun <T> ResultState<T>.onSuccess(action: (T) -> Unit): ResultState<T> {
    if (this is ResultState.Success) action(data)
    return this
}

inline fun <T> ResultState<T>.onError(action: (String, Throwable?) -> Unit): ResultState<T> {
    if (this is ResultState.Error) action(message, throwable)
    return this
}

inline fun <T> ResultState<T>.onLoading(action: () -> Unit): ResultState<T> {
    if (this is ResultState.Loading) action()
    return this
}