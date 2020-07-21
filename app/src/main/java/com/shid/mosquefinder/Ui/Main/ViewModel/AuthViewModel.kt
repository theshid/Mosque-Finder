package com.shid.mosquefinder.Ui.Main.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.firebase.auth.AuthCredential
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.Data.Repository.AuthRepository


class AuthViewModel :AndroidViewModel {
    private var authRepository: AuthRepository? = null
    var authenticatedUserLiveData: LiveData<User>? = null
    var createdUserLiveData: LiveData<User>? = null

     constructor(application: Application?) : super(application!!) {
        authRepository = AuthRepository()
    }

    fun signInWithGoogle(googleAuthCredential: AuthCredential?) {
        authenticatedUserLiveData = authRepository?.firebaseSignInWithGoogle(googleAuthCredential)
    }
}