package com.shid.mosquefinder.app.ui.main.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.main.view_models.AuthViewModel
import com.shid.mosquefinder.app.ui.onboardingscreen.feature.onboarding.OnBoardingActivity
import com.shid.mosquefinder.app.utils.enums.Status
import com.shid.mosquefinder.app.utils.extensions.showToast
import com.shid.mosquefinder.app.utils.extensions.startActivity
import com.shid.mosquefinder.app.utils.helper_class.FusedLocationWrapper
import com.shid.mosquefinder.app.utils.helper_class.SharePref
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common.LOCATION_PERMISSION_REQUEST_CODE
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common.USER
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common.logErrorMessage
import com.shid.mosquefinder.app.utils.helper_class.singleton.GsonParser
import com.shid.mosquefinder.app.utils.helper_class.singleton.PermissionUtils
import com.shid.mosquefinder.data.model.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : BaseActivity() {

    var userPosition: LatLng? = null
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    @Inject
    lateinit var googleSignInOptions: GoogleSignInOptions

    @Inject
    lateinit var sharePref: SharePref
    private var isFirstTime: Boolean? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    @Inject
    lateinit var fusedLocationWrapper: FusedLocationWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        isFirstTime = sharePref.loadFirstTime()
        checkIfPermissionIsActive()
        initSignInButton()
        setObservers()

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun permissionCheck(fusedLocationWrapper: FusedLocationWrapper) {
        if (PermissionUtils.isAccessFineLocationGranted(this)) {
            getUserLocation(fusedLocationWrapper)

        } else {
            showToast(getString(R.string.toast_permission))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun getUserLocation(fusedLocationWrapper: FusedLocationWrapper) {
        this.lifecycleScope.launch {
            val location = fusedLocationWrapper.awaitLastLocation()
            userPosition = LatLng(location.latitude, location.longitude)
            userPosition?.let {
                sharePref.saveUserPosition(LatLng(it.latitude, it.longitude))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun checkIfPermissionIsActive() {
        when {
            PermissionUtils.isAccessFineLocationGranted(this) -> {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                        permissionCheck(fusedLocationWrapper)
                    }
                    else -> {
                        PermissionUtils.showGPSNotEnabledDialog(this)
                    }
                }
            }
            else -> {
                PermissionUtils.requestAccessFineLocationPermission(
                    this,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun setObservers() {

        authViewModel.retrieveStatusMsg().observe(this, androidx.lifecycle.Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                }
                Status.LOADING -> {

                }
                Status.ERROR -> {
                    //Handle Error
                    Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                    Timber.d(it.message.toString())
                }
            }
        })


    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    when {
                        PermissionUtils.isLocationEnabled(this) -> {
                            permissionCheck(fusedLocationWrapper)
                        }
                        else -> {
                            PermissionUtils.showGPSNotEnabledDialog(this)
                        }
                    }
                } else {
                    showToast(getString(R.string.location_permission_not_granted))
                }
            }
        }
    }

    private fun initSignInButton() {
        google_sign_in_button.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val task =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val googleSignInAccount =
                        task.getResult(ApiException::class.java)
                    googleSignInAccount?.let { getGoogleAuthCredential(it) }
                } catch (e: ApiException) {
                    logErrorMessage(e.message)
                }

            }
        }

    private fun getGoogleAuthCredential(googleSignInAccount: GoogleSignInAccount) {
        Timber.d("getGoogleAuthCredential: ")
        val googleTokenId = googleSignInAccount.idToken
        val googleAuthCredential =
            GoogleAuthProvider.getCredential(googleTokenId, null)
        signInWithGoogleAuthCredential(googleAuthCredential)
    }

    private fun signInWithGoogleAuthCredential(googleAuthCredential: AuthCredential) {
        Timber.d("signInWithGoogleAuthCredential: ")
        authViewModel.signInWithGoogle(googleAuthCredential)
        authViewModel.authenticatedUserLiveData?.observe(this, Observer {
            if (it.isNew!!) {
                Timber.d("new: ")
                createNewUser(it)
            } else {
                Timber.d("go to home: ")
                goToHomeActivity(it)
            }
        })

    }

    private fun createNewUser(authenticatedUser: User) {

        authViewModel.createUser(authenticatedUser)
        authViewModel.createdUserLiveData?.observe(this, Observer {
            if (it.isCreated!!) {
                toastMessage(it.name!!)
            }
            goToOnBoardingActivity(it)
        })

    }

    private fun toastMessage(name: String) {
        Toast.makeText(
            this,
            String.format(resources.getString(R.string.toast_auth_salutation, name)),
            Toast.LENGTH_LONG
        ).show()

    }

    private fun goToOnBoardingActivity(user: User) {
        startActivity<OnBoardingActivity> {
            putExtra(USER, user)
        }
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun goToHomeActivity(user: User) {
        val convertUserJson = GsonParser.gsonParser?.toJson(user)
        convertUserJson?.let { sharePref.saveUser(convertUserJson) }
        if (isFirstTime == true) {
            startActivity<LoadingActivity>()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        } else {
            startActivity<HomeActivity> {
                putExtra(USER, user)
            }
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

    }
}