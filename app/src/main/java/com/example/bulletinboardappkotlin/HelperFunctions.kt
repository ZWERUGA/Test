package com.example.bulletinboardappkotlin

import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.Fragment

object HelperFunctions {
    fun toastMessage(activity: Activity, message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    fun toastMessage(activity: Fragment, message: String) {
        Toast.makeText(activity.context, message, Toast.LENGTH_SHORT).show()
    }
}