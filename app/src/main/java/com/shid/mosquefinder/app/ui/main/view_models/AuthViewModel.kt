package com.shid.mosquefinder.app.ui.main.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.shid.mosquefinder.app.utils.helper_class.Resource
import com.shid.mosquefinder.data.model.User
import com.shid.mosquefinder.data.repository.AuthRepositoryImpl
import com.shid.mosquefinder.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepositoryImpl: AuthRepository): ViewModel() {
    var authenticatedUserLiveData: LiveData<User>? = null
    var createdUserLiveData: LiveData<User>? = null

    fun signInWithGoogle(googleAuthCredential: AuthCredential?) {
        authenticatedUserLiveData =
            authRepositoryImpl.firebaseSignInWithGoogle(googleAuthCredential)
    }

    fun createUser(authenticatedUser: User) {
        createdUserLiveData = authRepositoryImpl.createUserInFirestoreIfNotExists(authenticatedUser)
    }

    fun retrieveStatusMsg(): LiveData<Resource<String>> {
        return authRepositoryImpl.returnStatusMsg()
    }
}