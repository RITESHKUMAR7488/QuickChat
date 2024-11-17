package com.example.quickchat.utility

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

import com.example.quickchat.constants.Constant
import com.example.quickchat.onboardingModule.models.UserModel
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class PreferenceManager @Inject constructor(@ApplicationContext val context: Context) {

    private var mPreferences: SharedPreferences = context.getSharedPreferences(
        Constant.AUTH,
        AppCompatActivity.MODE_PRIVATE
    )
    private var editor: SharedPreferences.Editor = mPreferences.edit()

    var isLoggedIn: Boolean
        get() = mPreferences.getBoolean(Constant.LOGGED_IN_EMAIL, false)
        set(emailLogin) {
            editor.putBoolean(Constant.LOGGED_IN_EMAIL, emailLogin)
            editor.commit()
        }


    var isGmailLoggedIn: Boolean
        get() = mPreferences.getBoolean(Constant.LOGGED_IN, false)
        set(loggedIn) {
            editor.putBoolean(Constant.LOGGED_IN, loggedIn)
            editor.commit()
        }



    var email: String?
        get()= mPreferences.getString(Constant.EMAIL,"")
        set(email){
            editor.putString(Constant.EMAIL,email)
            editor.commit()
        }

    var password: String?
        get()= mPreferences.getString(Constant.PASSWORD,"")
        set(password){
            editor.putString(Constant.PASSWORD,password)
            editor.commit()
        }

    var userId: String?
        get()= mPreferences.getString(Constant.AUTH_MODEL,"")
        set(userId){
            editor.putString(Constant.AUTH_MODEL,userId)
            editor.commit()
        }



    var userModel: UserModel?
        get() {
            val json = mPreferences.getString(Constant.USER_MODEL, null)
            return if (json != null) {
                Gson().fromJson(json, UserModel::class.java)
            } else {
                null
            }
        }
        set(value) {
            val json = Gson().toJson(value)
            editor.putString(Constant.USER_MODEL, json).apply()
        }





}