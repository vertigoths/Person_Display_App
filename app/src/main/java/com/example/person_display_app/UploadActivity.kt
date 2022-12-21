package com.example.person_display_app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.person_display_app.model.Account
import com.example.person_display_app.model.TextFieldState
import com.example.person_display_app.model.User
import com.example.person_display_app.ui.theme.Person_Display_AppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest

class UploadActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var selectImage: ActivityResultLauncher<String>
    private lateinit var newImageRef: StorageReference
    private lateinit var inputStream: InputStream

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let{
                val storage = Firebase.storage
                val storageRef = storage.reference

                val imagesRef = storageRef.child("Images")

                val userSize = intent.getIntExtra("userCount", 0)
                newImageRef = imagesRef.child(userSize.toString())

                inputStream = contentResolver.openInputStream(uri!!)!!
            }
        }

        setContent {
            Person_Display_AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Scaffold(topBar = {
                        TopAppBar() {
                            Text("Upload Screen")
                        }
                    })
                    {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth())
                        {
                            var mContext = LocalContext.current
                            var userSize = intent.getIntExtra("userCount", 0)

                            var nameState = remember { TextFieldState() }
                            var surnameState = remember { TextFieldState() }

                            TextEntry("Name:", nameState)
                            TextEntry("Surname:", surnameState)
                            Button (onValidate = { loadImage() }, "Upload Image")

                            Row()
                            {
                                Button (onValidate = { create(mContext, User(nameState.text, surnameState.text), userSize) }, "Create")
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun TextEntry(label: String, emailState : TextFieldState = remember { TextFieldState() })
    {
        TextField(value = emailState.text, onValueChange = { emailState.text = it }, label = { Text(label) })
    }

    @Composable
    fun Button(onValidate: () -> Unit, content: String) {
        Button(
            onClick =  onValidate,
            content = { Text(content) }
        )
    }

    private fun create(mContext: Context, user: User, userSize: Int)
    {
        if(!user.name.isNullOrEmpty() && !user.surname.isNullOrEmpty() && this::newImageRef.isInitialized)
        {
            val collection = FirebaseFirestore.getInstance().collection("users")
            val hashMap = hashMapOf("name" to user.name, "surname" to user.surname)

            collection.document(userSize.toString()).set(hashMap)
                .addOnSuccessListener {
                    Toast.makeText(mContext, "New user has been created!", Toast.LENGTH_LONG)
                }

            newImageRef.putStream(inputStream!!)
                .addOnSuccessListener {
                    finish()
                }
                .addOnFailureListener { exception ->
                    Log.e("3:", "Failed to upload image to Firebase Storage", exception)
                }
        }
        else
        {
            Toast.makeText(mContext, "You need to enter all the fields!", Toast.LENGTH_LONG)
        }
    }

    private fun md5(input: String): String
    {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    private fun loadImage()
    {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }

        selectImage.launch(intent.type)
    }
}