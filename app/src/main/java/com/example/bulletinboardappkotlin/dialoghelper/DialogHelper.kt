package com.example.bulletinboardappkotlin.dialoghelper

import android.app.AlertDialog
import android.view.View
import com.example.bulletinboardappkotlin.MainActivity
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.accounthelper.AccountHelper
import com.example.bulletinboardappkotlin.databinding.SignUpInDialogBinding
import com.example.bulletinboardappkotlin.HelperFunctions.toastMessage

class DialogHelper(private val activity: MainActivity) {
    val accountHelper = AccountHelper(activity)

    fun createSignDialog(index: Int) {
        val builder = AlertDialog.Builder(activity)
        val rootDialogElement = SignUpInDialogBinding.inflate(activity.layoutInflater)

        builder.setView(rootDialogElement.root)
        setDialogState(index, rootDialogElement)
        val dialog = builder.create()

        rootDialogElement.btSignUpIn.setOnClickListener {
            setOnClickSignUpInButton(index, rootDialogElement, dialog)
        }

        rootDialogElement.btForgetPassword.setOnClickListener {
            setOnClickForgetPasswordButton(rootDialogElement, dialog)
        }

        rootDialogElement.btGoogleAuth.setOnClickListener {
            accountHelper.signInWithGoogle()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setDialogState(index: Int, rootDialogElement: SignUpInDialogBinding) {
        if (index == DialogConsts.SIGN_UP_STATE) {
            rootDialogElement.tvSignUpInTitle.text =
                activity.resources.getString(R.string.account_sign_up_title)
            rootDialogElement.btSignUpIn.text =
                activity.resources.getString(R.string.sign_up_action)
            rootDialogElement.btGoogleAuth.text =
                activity.resources.getString(R.string.sign_up_with_google_action)
        } else {
            rootDialogElement.tvSignUpInTitle.text =
                activity.resources.getString(R.string.account_sign_in_title)
            rootDialogElement.btSignUpIn.text =
                activity.resources.getString(R.string.sign_in_action)
            rootDialogElement.btGoogleAuth.text =
                activity.resources.getString(R.string.sign_in_with_google_action)
            rootDialogElement.btForgetPassword.visibility = View.VISIBLE
        }
    }

    private fun setOnClickSignUpInButton(index: Int,
                                         rootDialogElement: SignUpInDialogBinding,
                                         dialog: AlertDialog?) {
        val email = rootDialogElement.etTextEmailAddress.text.toString()
        val password = rootDialogElement.etTextPassword.text.toString()

        if (index == DialogConsts.SIGN_UP_STATE) {
            accountHelper.signUpWithEmail(email, password)
        } else {
            accountHelper.signInWithEmail(email, password)
        }

        dialog?.dismiss()
    }

    private fun setOnClickForgetPasswordButton(rootDialogElement: SignUpInDialogBinding,
                                               dialog: AlertDialog?) {
        if (rootDialogElement.etTextEmailAddress.text.isNotEmpty()) {
            val email = rootDialogElement.etTextEmailAddress.text.toString()
            activity.mAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toastMessage(activity, activity.resources.getString(
                        R.string.send_email_reset_password_done))
                }
            }
            dialog?.dismiss()
        } else {
            rootDialogElement.tvForgetPasswordTitle.visibility = View.VISIBLE
        }
    }
}