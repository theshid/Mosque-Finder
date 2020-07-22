package com.shid.mosquefinder.Ui.Main.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.Data.Repository.SplashRepository


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