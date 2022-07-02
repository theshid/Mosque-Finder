package com.shid.mosquefinder.ui.main.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.firebase.auth.AuthCredential
import com.shid.mosquefinder.data.model.User
import com.shid.mosquefinder.data.repository.AuthRepositoryImpl
import com.shid.mosquefinder.utils.Resource


class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var authRepositoryImpl: AuthRepositoryImpl
    var authenticatedUserLiveData: LiveData<User>? = null
    var createdUserLiveData: LiveData<User>? = null

    init {
        authRepositoryImpl = AuthRepositoryImpl()
    }

    fun signInWithGoogle(googleAuthCredential: AuthCredential?) {
        authenticatedUserLiveData = authRepositoryImpl?.firebaseSignInWithGoogle(googleAuthCredential)
    }

    fun createUser(authenticatedUser: User) {
        createdUserLiveData = authRepositoryImpl?.createUserInFirestoreIfNotExists(authenticatedUser)
    }

    fun retrieveStatusMsg(): LiveData<Resource<String>> {
        return authRepositoryImpl.returnStatusMsg()
    }
}