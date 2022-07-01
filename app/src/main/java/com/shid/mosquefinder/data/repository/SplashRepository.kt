package com.shid.mosquefinder.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.shid.mosquefinder.data.model.User
import com.shid.mosquefinder.utils.Common.USERS
import com.shid.mosquefinder.utils.Common.logErrorMessage


@SuppressWarnings("ConstantConditions")
class SplashRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val user: User = User()
    private val rootRef = FirebaseFirestore.getInstance()
    private val usersRef = rootRef.collection(USERS)
    val crashlytics = FirebaseCrashlytics.getInstance()


    fun checkIfUserIsAuthenticatedInFirebase(): MutableLiveData<User>? {
        val isUserAuthenticateInFirebaseMutableLiveData: MutableLiveData<User> =
            MutableLiveData<User>()
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            user.isAuthenticated = false
            Log.d("Error", user.isAuthenticated.toString())
            isUserAuthenticateInFirebaseMutableLiveData.setValue(user)
        } else {
            user.uid = firebaseUser.uid
            user.isAuthenticated = true
            isUserAuthenticateInFirebaseMutableLiveData.setValue(user)
        }
        return isUserAuthenticateInFirebaseMutableLiveData
    }

    fun addUserToLiveData(uid: String?): MutableLiveData<User>? {
        val userMutableLiveData: MutableLiveData<User> = MutableLiveData<User>()
        usersRef.document(uid!!).get()
            .addOnCompleteListener { userTask: Task<DocumentSnapshot?> ->
                if (userTask.isSuccessful) {
                    val document = userTask.result
                    if (document!!.exists()) {
                        val user: User = document.toObject(User::class.java)!!
                        userMutableLiveData.value = user
                    }
                } else {
                    logErrorMessage(userTask.exception!!.message)
                    Log.d("Error", userTask.exception!!.message!!)
                }
            }.addOnFailureListener {
                crashlytics.recordException(it)
            }
        return userMutableLiveData
    }
}