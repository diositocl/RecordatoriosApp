package com.example.recordatoriosapp.data.remote

// Resultado controlado para las llamadas de red
sealed interface RemoteResult<out T> {
    data class Success<T>(val data: T) : RemoteResult<T>
    data object NoConnection : RemoteResult<Nothing>
    data object Timeout : RemoteResult<Nothing>
    data class HttpError(val code: Int, val message: String) : RemoteResult<Nothing>
    data class InvalidData(val cause: Throwable) : RemoteResult<Nothing>
}