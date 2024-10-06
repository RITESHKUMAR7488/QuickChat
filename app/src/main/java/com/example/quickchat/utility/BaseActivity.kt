package com.example.quickchat.utility

import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

open class BaseActivity : AppCompatActivity(){
     @Inject
     lateinit var commonUtil: CommonUtil

     @Inject
     lateinit var preferenceManager: PreferenceManager

}