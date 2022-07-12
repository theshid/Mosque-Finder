package com.shid.mosquefinder.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.shid.mosquefinder.data.model.User
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common.USERS
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common.logErrorMessage
import timber.log.Timber
import javax.inject.Inject


@SuppressWarnings("ConstantConditions")
class SplashRepository @Inject constructor(private val firebaseAuth:FirebaseAuth, rootRef:FirebaseFirestore, private val crashlytics:FirebaseCrashlytics, val user: User){
   // private val user: User = User()
    private val usersRef = rootRef.collection(USERS)


    fun checkIfUserIsAuthenticatedInFirebase(): MutableLiveData<User> {
        val isUserAuthenticateInFirebaseMutableLiveData: MutableLiveData<User> =
            MutableLiveData<User>()
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            user.isAuthenticated = false
            Timber.d( user.isAuthenticated.toString())
            isUserAuthenticateInFirebaseMutableLiveData.setValue(user)
        } else {
            user.uid = firebaseUser.uid
            user.isAuthenticated = true
            isUserAuthenticateInFirebaseMutableLiveData.setValue(user)
        }
        return isUserAuthenticateInFirebaseMutableLiveData
    }

    fun addUserToLiveData(uid: String?): MutableLiveData<User> {
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