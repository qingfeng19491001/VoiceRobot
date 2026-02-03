package com.voicerobot.core.domain


sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>() {
        override fun toString() = "Success[data=$data]"
    }

    data class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "An unknown error occurred"
    ) : Result<Nothing>() {
        override fun toString() = "Error[exception=$exception]"
    }

    object Loading : Result<Nothing>() {
        override fun toString() = "Loading"
    }

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun error(exception: Throwable, message: String = exception.message ?: "An unknown error occurred"): Result<Nothing> = 
            Error(exception, message)
        fun <T> loading(): Result<T> = Loading
    }
}


inline fun <T, R> Result<T>.map(transform: (value: T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
        is Result.Loading -> Result.Loading
    }
}
