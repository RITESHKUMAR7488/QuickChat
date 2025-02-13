package com.example.quickchat.utility

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import javax.inject.Inject

open class BaseFragment : Fragment(){
     @Inject
     lateinit var commonUtil: CommonUtil

     @Inject
     lateinit var preferenceManager: PreferenceManager

}