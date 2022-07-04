package com.shid.mosquefinder.app.ui.main.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.ConnectivityStateHolder
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.Base.FeedbackViewModelFactory
import com.shid.mosquefinder.app.ui.main.view_models.FeedbackViewModel
import com.shid.mosquefinder.app.utils.Network.Event
import com.shid.mosquefinder.app.utils.Network.NetworkEvents
import com.shid.mosquefinder.app.utils.hideKeyboard
import kotlinx.android.synthetic.main.activity_feedback.*

class FeedbackActivity : AppCompatActivity() {
    private lateinit var feedbackViewModel: FeedbackViewModel
    private lateinit var feedbackViewModelFactory: FeedbackViewModelFactory
    private var previousSate = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }


        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent)
                handleConnectivityChange()
        })


        setViewModel()
        setOnClick()
    }
    override fun onResume() {
        super.onResume()
        handleConnectivityChange()
    }

    private fun setOnClick() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        sendButton.setOnClickListener {
            sendFeedback()
        }
    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected && !previousSate) {
            //showSnackBar(textView, "The network is back !")
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_connected))
                .setMessage(getString(R.string.sneaker_msg_network))
                .sneakSuccess()

        }

        if (!ConnectivityStateHolder.isConnected && previousSate) {
            //showSnackBar(textView, "No Network !")
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_disconnected))
                .setMessage(getString(R.string.sneaker_msg_network_lost))
                .sneakError()

        }

        previousSate = ConnectivityStateHolder.isConnected
    }

    private fun sendFeedback() {
        clearError()
        hideKeyboard(this)
        val email = emailEdit.text.toString()
        val message = feedbackEdit.text.toString()

        if (!isValidEmail(email)) {
            emailInputLayout.error = getString(R.string.feedback_email_error)
            return
        }

        if (message.isBlank()) {
            feedbackInputLayout.error = getString(R.string.feedback_feedback_error)
            return
        }
        feedbackViewModel.sendFeedback(email, message)
    }

    private fun clearError() {
        emailInputLayout.error = ""
        feedbackInputLayout.error = ""
    }

    private fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    private fun setViewModel() {
        feedbackViewModelFactory = FeedbackViewModelFactory(application)
        feedbackViewModel =ViewModelProvider(this,feedbackViewModelFactory).get(FeedbackViewModel(application)::class.java)

        feedbackViewModel.showLoadingLiveData.observe(this, observerShowLoading())
        feedbackViewModel.sendFeedbackSuccessfulLiveData.observe(this, observerSendFeedbackSuccessful())
        feedbackViewModel.sendFeedbackFailedLiveData.observe(this, observerSendFeedbackFailed())
    }

    /** Observers **/

    private fun observerShowLoading() = Observer<Boolean> {
        loadView.visibility = if (it) View.VISIBLE else View.GONE
    }

    private fun observerSendFeedbackSuccessful() = Observer<String> {
        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun observerSendFeedbackFailed() = Observer<String> {
        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
    }
}