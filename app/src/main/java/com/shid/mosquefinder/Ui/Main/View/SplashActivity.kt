package com.shid.mosquefinder.Ui.Main.View

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.MainActivity
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Main.ViewModel.SplashViewModel
import com.shid.mosquefinder.Ui.Main.ViewModel.SplashViewModelFactory
import com.shid.mosquefinder.Utils.Common.USER


class SplashActivity : AppCompatActivity() {
    lateinit var splashViewModel: SplashViewModel
    lateinit var splashViewModelFactory: SplashViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_splash)
        initSplashViewModel();
        checkIfUserIsAuthenticated();
    }

    private fun checkIfUserIsAuthenticated() {
        splashViewModel!!.checkIfUserIsAuthenticated()
        splashViewModel!!.isUserAuthenticatedLiveData!!.observe(this,
            Observer { user: User ->
                if (!user.isAuthenticated!!) {
                    goToAuthInActivity()
                    finish()
                } else {
                    getUserFromDatabase(user.uid)
                }
            }
        )
    }

    private fun getUserFromDatabase(uid: String) {
        splashViewModel!!.setUid(uid)
        splashViewModel!!.userLiveData!!.observe(
            this,
            Observer { user: User? ->
                goToMainActivity(user)
                finish()
            }
        )
    }

    private fun goToMainActivity(user: User?) {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        intent.putExtra(USER, user)
        startActivity(intent)
    }

    private fun goToAuthInActivity() {
        val intent = Intent(this@SplashActivity, AuthActivity::class.java)
        startActivity(intent)
    }

    private fun initSplashViewModel() {
        splashViewModelFactory = SplashViewModelFactory(application)
        splashViewModel = ViewModelProvider(this,splashViewModelFactory).get(SplashViewModel(application)::class.java)
    }
}