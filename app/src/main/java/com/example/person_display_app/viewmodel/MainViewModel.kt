import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.example.person_display_app.model.User
import com.example.person_display_app.sealed.DataState
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class MainViewModel : ViewModel()
{
    val response: MutableState<DataState<MutableList<User>>> = mutableStateOf(DataState.Empty)
    var counter: Int = 0
    var userSize: Int = 0

    init
    {
        fetchDataFromFirebase()
    }

    public fun fetchDataFromFirebase() {
        val tempList = mutableListOf<User>()
        response.value = DataState.Loading

        val colRef: CollectionReference = FirebaseFirestore.getInstance().collection("users")
        colRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful)
            {
                val users = task.result.toObjects(User:: class.java)
                val user = users[counter % users.size]

                userSize = users.size
                tempList.add(user)

                response.value = DataState.Success(tempList)
            }
            else
            {
                response.value = DataState.Failure<MutableList<User>>("Error")
            }
        }
    }
}