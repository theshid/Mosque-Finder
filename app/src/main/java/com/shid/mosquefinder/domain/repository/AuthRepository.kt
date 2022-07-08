package com.shid.mosquefinder.domain.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.AuthCredential
import com.shid.mosquefinder.data.model.User
import com.shid.mosquefinder.app.utils.helper_class.Resource

interface AuthRepository {
    fun firebaseSignInWithGoogle(googleAuthCredential: AuthCredential?): MutableLiveData<User>

    fun createUserInFirestoreIfNotExists(authenticatedUser: User): MutableLiveData<User>

    fun returnStatusMsg(): LiveData<Resource<String>>
}