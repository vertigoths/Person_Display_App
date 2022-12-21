package com.example.person_display_app.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.person_display_app.model.User
import com.example.person_display_app.sealed.DataState
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class ImageViewModel : ViewModel()
{
    val response: MutableState<DataState<String>> = mutableStateOf(DataState.Empty)
    var counter: Int = 0

    init
    {
        fetchDataFromFirebase()
    }

    public fun fetchDataFromFirebase()
    {
        val mImageStorage: StorageReference = FirebaseStorage.getInstance().reference
        val ref = mImageStorage.child("Images")
        val listResult = ref.listAll()

        listResult.addOnSuccessListener { imageList ->
            imageList.items[counter % imageList.items.size].downloadUrl.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downUri = task.result
                    val imageUrl = downUri.toString()

                    response.value = DataState.Success(imageUrl)
                }
            }
        }
    }
}