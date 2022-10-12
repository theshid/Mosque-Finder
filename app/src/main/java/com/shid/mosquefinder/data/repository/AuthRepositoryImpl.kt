package com.shid.mosquefinder.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.shid.mosquefinder.app.utils.helper_class.Resource
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common.USERS
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common.logErrorMessage
import com.shid.mosquefinder.data.model.User
import com.shid.mosquefinder.domain.repository.AuthRepository
import javax.inject.Inject


class AuthRepositoryImpl @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    val rootRef: FirebaseFirestore
) : AuthRepository {

    private val usersRef = rootRef.collection(USERS)
    private val statusMsg: MutableLiveData<Resource<String>> = MutableLiveData()

    @Inject
    lateinit var crashlytics: FirebaseCrashlytics


    override fun firebaseSignInWithGoogle(googleAuthCredential: AuthCredential?): MutableLiveData<User> {
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

    override fun createUserInFirestoreIfNotExists(authenticatedUser: User): MutableLiveData<User> {
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

    override fun returnStatusMsg(): LiveData<Resource<String>> {
        return statusMsg
    }
}