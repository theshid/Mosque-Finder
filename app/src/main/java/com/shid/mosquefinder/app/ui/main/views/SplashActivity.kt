package com.shid.mosquefinder.app.ui.main.views

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.data.model.User
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.SplashViewModelFactory
import com.shid.mosquefinder.app.ui.main.view_models.SplashViewModel
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common.USER
import com.shid.mosquefinder.app.utils.helper_class.singleton.PermissionUtils
import com.shid.mosquefinder.app.utils.setTransparentStatusBar


class SplashActivity : AppCompatActivity() {
    private lateinit var splashViewModel: SplashViewModel
    private lateinit var splashViewModelFactory: SplashViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        initSplashViewModel()
        setTransparentStatusBar()

        checkIfPermissionActive()

        val handler = Handler()
        handler.postDelayed({
            checkIfUserIsAuthenticated();
        }, 3000)

    }

    private fun checkIfPermissionActive() {
        if (PermissionUtils.isAccessFineLocationGranted(this)) {


        } else {
            Toast.makeText(this, getString(R.string.toast_permission), Toast.LENGTH_LONG).show()
        }
    }


    private fun checkIfUserIsAuthenticated() {
        splashViewModel.checkIfUserIsAuthenticated()
        splashViewModel.isUserAuthenticatedLiveData!!.observe(this,
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
        splashViewModel.setUid(uid)
        splashViewModel.userLiveData!!.observe(
            this,
            Observer { user: User? ->
                goToHomeActivity(user)
                Log.d("Splash", "user:" + user?.email)
                finish()
            }
        )
    }

    private fun goToHomeActivity(user: User?) {
        val intent = Intent(this@SplashActivity, HomeActivity::class.java)
        intent.putExtra(USER, user)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun goToAuthInActivity() {
        val intent = Intent(this@SplashActivity, AuthActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun initSplashViewModel() {
        splashViewModelFactory =
            SplashViewModelFactory(application)
        splashViewModel = ViewModelProvider(
            this,
            splashViewModelFactory
        ).get(SplashViewModel(application)::class.java)
    }
}