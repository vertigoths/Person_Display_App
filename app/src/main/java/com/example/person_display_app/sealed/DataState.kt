package com.example.person_display_app.sealed

import com.example.person_display_app.model.User

sealed class DataState<out T> {
    class Success<T>(val data: T) : DataState<T>()
    class Failure<T>(val message: String) : DataState<Nothing>()
    object Loading : DataState<Nothing>()
    object Empty : DataState<Nothing>()
}
