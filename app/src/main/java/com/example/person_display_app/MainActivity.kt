package com.example.person_display_app

import MainViewModel
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.person_display_app.model.User
import com.example.person_display_app.sealed.DataState
import com.example.person_display_app.ui.theme.Person_Display_AppTheme
import com.example.person_display_app.viewmodel.ImageViewModel
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val imageViewModel: ImageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Person_Display_AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                )
                {
                    Scaffold(topBar = {
                        TopAppBar() {
                            Text("Person Information")
                        }
                    },
                    bottomBar = {
                        BottomAppBar(
                            elevation = 0.dp,
                            modifier = Modifier.fillMaxWidth())
                        {
                            var mContext = LocalContext.current
                            BottomBar(mContext)
                        }
                    }) {
                        val sharedPreferences = getPreferences(MODE_PRIVATE)

                        viewModel.counter = sharedPreferences.getInt("ID", 0)
                        imageViewModel.counter = viewModel.counter

                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth())
                        {
                            SetImage(imageViewModel)
                            SetData(viewModel)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SetData(viewModel: MainViewModel) {
        when (val result = viewModel.response.value) {
            is DataState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is DataState.Success<*> -> {
                ShowLazyList(result.data as MutableList<User>)
            }
            is DataState.Failure<*> -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = result.message,
                        fontSize = MaterialTheme.typography.h5.fontSize,
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error Fetching data",
                        fontSize = MaterialTheme.typography.h5.fontSize,
                    )
                }
            }
        }
    }

    @Composable
    fun SetImage(viewModel: ImageViewModel)
    {
        when (val result = viewModel.response.value)
        {
            is DataState.Success<*> ->
            {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center)
                {
                    Image(
                        painter = rememberAsyncImagePainter(result.data as String),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(240.dp)
                            .height(240.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun Button(onValidate: () -> Unit, content: String) {
        Button(
            onClick =  onValidate,
            content = { Text(content) }
        )
    }

    @Composable
    fun ShowLazyList(users: MutableList<User>) {
        var mContext = LocalContext.current
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            items(users) { user ->
                TextView("Name:", user.name!!)
                TextView("Surname:", user.surname!!)
                Button(onValidate = { loadNextUser(mContext) }, "Load Next")
            }
        }
    }

    @Composable
    fun TextView(identifier: String, content: String){
        Text(
            "$identifier $content", textAlign = TextAlign.Center,
            modifier = Modifier.width(150.dp))
    }

    private fun loadNextUser(mContext: Context)
    {
        val sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("ID", sharedPreferences.getInt("ID", 0) + 1)
        editor.commit()

        viewModel.counter += 1
        imageViewModel.counter = viewModel.counter

        viewModel.fetchDataFromFirebase()
        imageViewModel.fetchDataFromFirebase()
    }

    @Composable
    private fun BottomBar(mContext: Context)
    {
        Row()
        {
            IconButton(onClick = {
                var intent = Intent(mContext, UploadActivity::class.java)

                intent.putExtra("userCount", viewModel.userSize)

                mContext.startActivity(intent)
            })
            {
                Icon(Icons.Default.AddCircle, "Add")
            }

            Spacer(Modifier.weight(1f))

            IconButton(onClick = {
                val email = intent.getStringExtra("email")
                val collection = FirebaseFirestore.getInstance().collection("usersLists")
                val document = collection.document(email.toString())
                val subCollection = document.collection("likedUsers")
                val hashMap = hashMapOf("ID" to viewModel.counter.toString())

                subCollection.document(viewModel.counter.toString()).set(hashMap)
            })
            {
                Icon(Icons.Default.Star, "Star")
            }
        }
    }
}

