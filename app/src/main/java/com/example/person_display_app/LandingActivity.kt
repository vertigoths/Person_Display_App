package com.example.person_display_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.person_display_app.ui.theme.Person_Display_AppTheme
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.person_display_app.model.Account
import com.example.person_display_app.model.TextFieldState
import com.google.firebase.auth.*

class LandingActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            Person_Display_AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Scaffold(topBar = {
                        TopAppBar() {
                            Text("Login Screen")
                        }
                    })
                    {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth())
                        {
                            var mContext = LocalContext.current

                            var emailState = remember { TextFieldState() }
                            var passwordState = remember { TextFieldState() }

                            MailEntry("E-mail:", emailState)
                            PasswordEntry("Password:", passwordState)

                            Row()
                            {
                                Button (onValidate = { register(auth, mContext, Account(emailState.text, passwordState.text)) }, "Register")
                                Button (onValidate = { login(auth, mContext, Account(emailState.text, passwordState.text)) }, "Login")
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MailEntry(label: String, emailState : TextFieldState = remember { TextFieldState() })
    {
        TextField(value = emailState.text, onValueChange = { emailState.text = it }, label = { Text(label) })
    }

    @Composable
    fun PasswordEntry(label: String, passwordState : TextFieldState = remember { TextFieldState() })
    {
        TextField(value = passwordState.text, onValueChange = { passwordState.text = it }, label = { Text(label) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
    }

    @Composable
    fun Button(onValidate: () -> Unit, content: String) {
        Button(
            onClick =  onValidate,
            content = { Text(content) }
        )
    }

    fun register(auth: FirebaseAuth, mContext: Context, account: Account)
    {
        if(!account.email.isNullOrEmpty() && !account.password.isNullOrEmpty())
        {
            auth.createUserWithEmailAndPassword(account.email, account.password).addOnCompleteListener{ task ->
                if(task.isSuccessful)
                {
                    Toast.makeText(mContext, "Your account has been created!", Toast.LENGTH_LONG).show()
                }
                else
                {
                    when (task.exception) {
                        is FirebaseAuthEmailException -> {
                            Toast.makeText(mContext, "E-mail address format is wrong!", Toast.LENGTH_LONG).show()
                        }
                        is FirebaseAuthWeakPasswordException -> {
                            Toast.makeText(mContext, "Password isn't strong!", Toast.LENGTH_LONG).show()
                        }
                        is FirebaseAuthUserCollisionException -> {
                            Toast.makeText(mContext, "E-mail is in already use!", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(mContext, "Something is wrong!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    fun login(auth: FirebaseAuth, mContext: Context, account: Account){
        if(!account.email.isNullOrEmpty() && !account.password.isNullOrEmpty())
        {
            auth.signInWithEmailAndPassword(account.email, account.password).addOnCompleteListener{ task ->
                if(task.isSuccessful)
                {
                    var intent = Intent(mContext, MainActivity::class.java)
                    intent.putExtra("email", account.email)
                    mContext.startActivity(intent)
                }
                else
                {
                    when (task.exception) {
                        is FirebaseAuthInvalidUserException -> {
                            Toast.makeText(mContext, "E-mail doesn't exists!", Toast.LENGTH_LONG).show()
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(mContext, "Password is wrong!", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(mContext, "Something is wrong!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}

