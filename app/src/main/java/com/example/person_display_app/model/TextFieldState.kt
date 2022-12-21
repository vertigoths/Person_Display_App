package com.example.person_display_app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TextFieldState()
{
    var text: String by mutableStateOf("")
}