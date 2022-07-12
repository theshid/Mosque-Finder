package com.shid.mosquefinder.app.ui.main.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shid.mosquefinder.data.model.User
import com.shid.mosquefinder.data.repository.SplashRepository
import javax.inject.Inject


class SplashViewModel @Inject constructor(private val splashRepository: SplashRepository) : ViewModel() {
    var isUserAuthenticatedLiveData: LiveData<User>? = null
    var userLiveData: MutableLiveData<User>? = null

    fun checkIfUserIsAuthenticated() {
        isUserAuthenticatedLiveData = splashRepository.checkIfUserIsAuthenticatedInFirebase()
    }

    fun setUid(uid: String) {
        userLiveData = splashRepository.addUserToLiveData(uid)
    }
}