package com.shid.mosquefinder.ui.Main.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.shid.mosquefinder.R
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var googleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        initGoogleSignInClient()
        btngo.setOnClickListener {
            signOutGoogle()
            singOutFirebase()
        }
    }

    private fun singOutFirebase() {
        firebaseAuth.signOut()
    }
    private fun initGoogleSignInClient() {
        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }
    private fun signOutGoogle() {
        googleSignInClient!!.signOut()
    }
}