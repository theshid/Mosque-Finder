package com.shid.mosquefinder.Ui.Main.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.firebase.auth.AuthCredential
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.Data.Repository.AuthRepository
import com.shid.mosquefinder.Utils.Resource


class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var authRepository: AuthRepository
    var authenticatedUserLiveData: LiveData<User>? = null
    var createdUserLiveData: LiveData<User>? = null

    init {
        authRepository = AuthRepository()
    }

    fun signInWithGoogle(googleAuthCredential: AuthCredential?) {
        authenticatedUserLiveData = authRepository?.firebaseSignInWithGoogle(googleAuthCredential)
    }

    fun createUser(authenticatedUser: User) {
        createdUserLiveData = authRepository?.createUserInFirestoreIfNotExists(authenticatedUser)
    }

    fun retrieveStatusMsg(): LiveData<Resource<String>> {
        return authRepository.returnStatusMsg()
    }
}