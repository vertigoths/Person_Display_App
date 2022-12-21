package com.example.person_display_app.model

data class DataOrException<T, E : Exception?>(
    var data: T? = null,
    var e: E? = null
)
