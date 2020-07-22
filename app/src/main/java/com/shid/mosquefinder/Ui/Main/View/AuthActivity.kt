package com.shid.mosquefinder.Ui.Main.View

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.MainActivity
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Main.ViewModel.AuthViewModel
import com.shid.mosquefinder.Ui.Main.ViewModel.AuthViewModelFactory
import com.shid.mosquefinder.Utils.Common.RC_SIGN_IN
import com.shid.mosquefinder.Utils.Common.USER
import com.shid.mosquefinder.Utils.Common.logErrorMessage
import kotlinx.android.synthetic.main.activity_auth.*


class AuthActivity : AppCompatActivity() {

    private lateinit var authViewModel:AuthViewModel
    private lateinit var authViewModelFactory: AuthViewModelFactory
    private lateinit var googleSignInClient:GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        initSignInButton()
        initAuthViewModel()
        initGoogleSignInClient()
    }

    private fun initSignInButton() {
        google_sign_in_button.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun initAuthViewModel() {
        authViewModelFactory = AuthViewModelFactory(application)
        authViewModel= ViewModelProvider(this,authViewModelFactory).get(AuthViewModel(application)::class.java)
    }

    private fun initGoogleSignInClient() {
        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
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
        val googleTokenId = googleSignInAccount.idToken
        val googleAuthCredential =
            GoogleAuthProvider.getCredential(googleTokenId, null)
        signInWithGoogleAuthCredential(googleAuthCredential)
    }

    private fun signInWithGoogleAuthCredential(googleAuthCredential: AuthCredential) {
        authViewModel.signInWithGoogle(googleAuthCredential)
        authViewModel.authenticatedUserLiveData?.observe(this, Observer {
            if (it.isNew!!) {
                createNewUser(it)
            } else {
                goToMainActivity(it)
            }
        })

    }

    private fun createNewUser(authenticatedUser: User) {

        authViewModel.createUser(authenticatedUser)
        authViewModel.createdUserLiveData?.observe(this, Observer {
            if (it.isCreated!!) {
                toastMessage(it.name!!)
            }
            goToMainActivity(it)
        })

    }

    private fun toastMessage(name: String) {
        Toast.makeText(
            this,
            "Hi $name!\nYour account was successfully created.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun goToMainActivity(user: User) {
        val intent = Intent(this@AuthActivity, MainActivity::class.java)
        intent.putExtra(USER, user)
        startActivity(intent)
        finish()
    }
}