package com.example.quickchat.utility

import android.app.Activity
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import javax.inject.Inject

class MotionToastUtil @Inject constructor()  {

    fun showSuccessToast(activity: Activity, message: String, duration: Long=MotionToast.LONG_DURATION)  {
        MotionToast.createToast(
            activity,
            "Success",
            message,
            MotionToastStyle.SUCCESS,
            MotionToast.GRAVITY_BOTTOM,
            duration,
            null
        )

    }
    fun showFailureToast(activity:Activity,message: String,duration: Long=MotionToast.LONG_DURATION) {
       MotionToast.createToast(

           activity,
           "Failed",
           message,
           MotionToastStyle.ERROR,
           MotionToast.GRAVITY_BOTTOM,
           duration,
           null
       )

    }

    fun showInfoToast(activity: Activity, message: String, duration: Long = MotionToast.LONG_DURATION) {
        MotionToast.createColorToast(
            activity,
            "Info",
            message,
            MotionToastStyle.INFO,
            MotionToast.GRAVITY_BOTTOM,
            duration,
            null
        )
    }
    fun showWarningToast(activity: Activity, message: String, duration: Long = MotionToast.LONG_DURATION) {
        MotionToast.createColorToast(
            activity,
            "Warning",
            message,
            MotionToastStyle.WARNING,
            MotionToast.GRAVITY_BOTTOM,
            duration,
            null
        )
    }
}




