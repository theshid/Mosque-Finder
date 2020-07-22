package com.shid.mosquefinder


import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.Ui.Main.View.AuthActivity
import com.shid.mosquefinder.Utils.Common.USER
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var googleSignInClient: GoogleSignInClient? = null
    //private var messageTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val user: User = getUserFromIntent()
        initGoogleSignInClient()
        setMessageToMessageTextView(user)
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            goToAuthInActivity()
        }
    }

    private fun getUserFromIntent(): User {
        return intent.getSerializableExtra(USER) as User
    }

    private fun initGoogleSignInClient() {
        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }


    private fun setMessageToMessageTextView(user: User) {
        val message = "You are logged in as: " + user.name
       // messageTextView!!.text = message
        message_text_view.text = message
    }

    private fun goToAuthInActivity() {
        val intent = Intent(this@MainActivity, AuthActivity::class.java)
        startActivity(intent)
    }

    private fun signOut() {
        singOutFirebase()
        signOutGoogle()
    }

    private fun singOutFirebase() {
        firebaseAuth.signOut()
    }

    private fun signOutGoogle() {
        googleSignInClient!!.signOut()
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId === R.id.sign_out_button) {
            signOut()
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }

}