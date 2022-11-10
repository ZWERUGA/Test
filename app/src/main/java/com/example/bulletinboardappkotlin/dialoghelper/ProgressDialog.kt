package com.example.bulletinboardappkotlin.dialoghelper

import android.app.Activity
import android.app.AlertDialog
import com.example.bulletinboardappkotlin.databinding.ProgressDialogBinding

object ProgressDialog {
    fun createProgressDialog(activity: Activity) : AlertDialog {
        val builder = AlertDialog.Builder(activity)
        val rootDialogElement = ProgressDialogBinding.inflate(activity.layoutInflater)
        builder.setView(rootDialogElement.root)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }
}