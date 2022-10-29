package com.shid.mosquefinder.app.ui.main.view_models

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.utils.functions.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    val application: Application,
    val database: FirebaseFirestore
) : ViewModel() {

    private val context: Context = application.applicationContext
    private val firebaseFeedbackRef: CollectionReference = database.collection("feedback")

    var showLoadingLiveData = MutableLiveData<Boolean>()
    var sendFeedbackSuccessfulLiveData = MutableLiveData<String>()
    var sendFeedbackFailedLiveData = MutableLiveData<String>()

    /*private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)*/

    fun sendFeedback(email: String, message: String, countryCode: String) {
        showLoadingLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val feedback: MutableMap<String, String> = HashMap()
                feedback["email"] = email
                feedback["message"] = message
                feedback["country"] =
                    countryCode

                firebaseFeedbackRef
                    .add(feedback)
                    .addOnSuccessListener {
                        onSendFeedbackSuccessful()
                    }
                    .addOnFailureListener {
                        val errorMessage = getErrorMessage(context, it)
                        onSendFeedbackFailed(errorMessage)
                        Timber.e(
                            "Error adding document $it"
                        )
                    }

            } catch (ex: Exception) {
                val errorMessage = getErrorMessage(context, ex)
                launch(Dispatchers.Main) {
                    showLoadingLiveData.postValue(false)
                    sendFeedbackFailedLiveData.postValue(errorMessage)
                }
            }
        }
    }

    private fun onSendFeedbackSuccessful() {
        viewModelScope.launch(Dispatchers.Main) {
            showLoadingLiveData.postValue(false)
            sendFeedbackSuccessfulLiveData.postValue(context.getString(R.string.feedback_send_successful))
        }
    }

    private fun onSendFeedbackFailed(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            showLoadingLiveData.postValue(false)
            sendFeedbackFailedLiveData.postValue(message)
        }
    }

}