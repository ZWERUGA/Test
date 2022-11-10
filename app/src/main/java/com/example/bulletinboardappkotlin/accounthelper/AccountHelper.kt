package com.example.bulletinboardappkotlin.accounthelper

import android.util.Log
import android.widget.Toast
import com.example.bulletinboardappkotlin.MainActivity
import com.example.bulletinboardappkotlin.HelperFunctions.toastMessage
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.dialoghelper.GoogleAccountConsts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*

private const val TAG = "AccountHelper"

class AccountHelper(private val activity: MainActivity) {
    private lateinit var signInClient: GoogleSignInClient

    // region Email
    fun signUpWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            activity.mAuth.currentUser?.delete()?.addOnCompleteListener { task_delete ->
                if (task_delete.isSuccessful) {
                    activity.mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signUpWithEmailSuccessful(task.result.user!!)
                            } else {
                                signUpWithEmailException(task.exception!!, email, password)
                            }
                        }
                }
            }
        }
    }

    private fun signUpWithEmailSuccessful(user: FirebaseUser) {
        sendEmailVerification(user)
        activity.updateUI(user)
    }

    private fun signUpWithEmailException(e: Exception, email: String, password: String) {
        if (e is FirebaseAuthUserCollisionException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
                linkEmailWithGoogleAccount(email, password)
            }
        } else if (e is FirebaseAuthInvalidCredentialsException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                toastMessage(
                    activity, activity.resources
                        .getString(R.string.error_invalid_email)
                )
            }
        }
        if (e is FirebaseAuthWeakPasswordException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                toastMessage(
                    activity, activity.resources
                        .getString(R.string.error_weak_password)
                )
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            activity.mAuth.currentUser?.delete()?.addOnCompleteListener { task_delete ->
                if (task_delete.isSuccessful) {
                    activity.mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                activity.updateUI(task.result?.user!!)
                                toastMessage(
                                    activity, activity.resources.getString(
                                        R.string.sign_in_done
                                    )
                                )
                            } else {
                                signInWithEmailException(task.exception!!)
                            }
                        }
                }
            }
        }
    }

    private fun signInWithEmailException(e: Exception) {
        if (e is FirebaseAuthInvalidCredentialsException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                toastMessage(
                    activity, activity.resources
                        .getString(R.string.error_invalid_email)
                )
            } else if (e.errorCode ==
                FirebaseAuthConstants.ERROR_WRONG_PASSWORD
            ) {
                toastMessage(activity, activity.resources.getString(R.string.error_wrong_password))
            }
        } else if (e is FirebaseAuthInvalidUserException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                toastMessage(
                    activity, activity.resources
                        .getString(R.string.error_user_not_found)
                )
            }
        }
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                toastMessage(
                    activity, activity.resources.getString(
                        R.string.send_verification_email_done
                    )
                )
            } else {
                toastMessage(
                    activity, activity.resources.getString(
                        R.string.send_verification_email_error
                    )
                )
            }
        }
    }
    // endregion

    private fun linkEmailWithGoogleAccount(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        if (activity.mAuth.currentUser != null) {
            activity.mAuth.currentUser?.linkWithCredential(credential)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        toastMessage(
                            activity, activity.resources
                                .getString(R.string.link_email_with_google_done)
                        )
                    }
                }
        } else {
            toastMessage(
                activity, activity.resources
                    .getString(R.string.enter_to_account_with_google)
            )
        }

    }

    // region Google
    private fun getSignClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail().build()
        return GoogleSignIn.getClient(activity, gso)
    }

    fun signInWithGoogle() {
        signInClient = getSignClient()
        val intent = signInClient.signInIntent
        activity.googleSignInLauncher.launch(intent)
    }

    fun signInFirebaseWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        activity.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                activity.mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        toastMessage(activity, "Sign in done! (Google)")
                        activity.updateUI(task.result?.user)
                    } else {
                        Log.d(TAG, "Google exception: ${task.exception}")
                    }
                }
            }
        }

    }

    fun signOutGoogle() {
        getSignClient().signOut()
    }
    // endregion

    fun signInAnonymously(listener: Listener) {
        activity.mAuth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                listener.onComplete()
                toastMessage(activity, "Вы вошли как 'Гость'")
            } else {
                toastMessage(activity, "Не удалось войти как 'Гость'")
            }
        }
    }

    interface Listener {
        fun onComplete()
    }
}