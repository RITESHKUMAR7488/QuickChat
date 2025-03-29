package com.example.quickchat.utility


import android.util.Log
import com.google.firebase.auth.FirebaseAuth

object FirebaseTokenHelper {
    fun getFirebaseIdToken(result: (String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.getIdToken(true)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val idToken = task.result?.token
                    Log.d("FIREBASE_ID_TOKEN", "Generated Token: $idToken")

                    result(idToken)
                } else {
                    Log.e("FIREBASE_ID_TOKEN", "Failed to generate token", task.exception)
                    result(null)
                }
            }
    }
}
