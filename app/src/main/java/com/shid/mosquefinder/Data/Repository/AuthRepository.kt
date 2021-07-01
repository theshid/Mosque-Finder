package com.shid.mosquefinder.Data.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.Utils.Common.USERS
import com.shid.mosquefinder.Utils.Common.logErrorMessage
import com.shid.mosquefinder.Utils.Resource


class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val rootRef = FirebaseFirestore.getInstance()
    private val usersRef = rootRef.collection(USERS)
    private val statusMsg: MutableLiveData<Resource<String>> = MutableLiveData()
    val crashlytics = FirebaseCrashlytics.getInstance()


    fun firebaseSignInWithGoogle(googleAuthCredential: AuthCredential?): MutableLiveData<User>? {
        val authenticatedUserMutableLiveData: MutableLiveData<User> = MutableLiveData<User>()
        firebaseAuth.signInWithCredential(googleAuthCredential!!)
            .addOnCompleteListener { authTask: Task<AuthResult> ->
                if (authTask.isSuccessful) {
                    val isNewUser =
                        authTask.result!!.additionalUserInfo!!.isNewUser
                    val firebaseUser = firebaseAuth.currentUser
                    if (firebaseUser != null) {
                        val uid = firebaseUser.uid
                        val name = firebaseUser.displayName
                        val email = firebaseUser.email
                        val user = User(uid, name, email)
                        user.isNew = isNewUser
                        authenticatedUserMutableLiveData.value = user
                    }
                } else {
                    logErrorMessage(authTask.exception!!.message)
                    crashlytics.recordException(authTask.exception!!)
                    statusMsg.postValue(
                        Resource.error(
                            authTask.exception.toString(),
                            "could not authenticate, check internet"
                        )
                    )
                }
            }
        return authenticatedUserMutableLiveData
    }

    fun createUserInFirestoreIfNotExists(authenticatedUser: User): MutableLiveData<User>? {
        val newUserMutableLiveData = MutableLiveData<User>()
        val uidRef =
            usersRef.document(authenticatedUser.uid)
        uidRef.get()
            .addOnCompleteListener { uidTask: Task<DocumentSnapshot?> ->
                if (uidTask.isSuccessful) {
                    val document = uidTask.result
                    if (!document!!.exists()) {
                        uidRef.set(authenticatedUser)
                            .addOnCompleteListener { userCreationTask: Task<Void?> ->
                                if (userCreationTask.isSuccessful) {
                                    authenticatedUser.isCreated = true
                                    newUserMutableLiveData.setValue(authenticatedUser)
                                } else {
                                    logErrorMessage(userCreationTask.exception!!.message)
                                }
                            }
                    } else {
                        newUserMutableLiveData.setValue(authenticatedUser)
                    }
                } else {
                    logErrorMessage(uidTask.exception!!.message)
                    crashlytics.recordException(uidTask.exception!!)
                    statusMsg.postValue(
                        Resource.error(
                            uidTask.exception.toString(),
                            "could not register, check internet"
                        )
                    )
                }
            }
        return newUserMutableLiveData
    }

    fun returnStatusMsg(): LiveData<Resource<String>> {
        return statusMsg
    }
}