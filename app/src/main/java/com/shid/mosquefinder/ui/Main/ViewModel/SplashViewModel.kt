package com.shid.mosquefinder.ui.Main.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shid.mosquefinder.data.model.User
import com.shid.mosquefinder.data.repository.SplashRepository


class SplashViewModel constructor(application: Application) : AndroidViewModel(application) {
    private var splashRepository: SplashRepository? = null
    var isUserAuthenticatedLiveData: LiveData<User>? = null
    var userLiveData: MutableLiveData<User>? = null

    init {
        splashRepository = SplashRepository()
    }

    fun checkIfUserIsAuthenticated() {
        isUserAuthenticatedLiveData = splashRepository!!.checkIfUserIsAuthenticatedInFirebase()
    }

    fun setUid(uid: String) {
        userLiveData = splashRepository?.addUserToLiveData(uid)
    }
}