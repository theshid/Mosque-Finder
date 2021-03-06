package com.shid.mosquefinder.Ui.Main.ViewModel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Utils.NetworkException
import com.shid.mosquefinder.Utils.NetworkUtil
import com.shid.mosquefinder.Utils.getCountryCode
import com.shid.mosquefinder.Utils.getErrorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FeedbackViewModel (application: Application) :ViewModel(){

    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val context: Context = application.applicationContext
    private val firebaseFeedbackRef: CollectionReference = database.collection("feedback")

    var showLoadingLiveData = MutableLiveData<Boolean>()
    var sendFeedbackSuccessfulLiveData = MutableLiveData<String>()
    var sendFeedbackFailedLiveData = MutableLiveData<String>()
    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun sendFeedback(email: String, message: String) {
        showLoadingLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (NetworkUtil.isOnline(context)) {
                    val feedback: MutableMap<String, String> = HashMap()
                    feedback["email"] = email
                    feedback["message"] = message
                    feedback["country"] =
                        getCountryCode(context)

                    firebaseFeedbackRef
                        .add(feedback)
                        .addOnSuccessListener {
                            onSendFeedbackSuccessful()
                        }
                        .addOnFailureListener {
                            val errorMessage = getErrorMessage(context, it)
                            onSendFeedbackFailed(errorMessage)
                            Log.e(
                                javaClass.simpleName,
                                "Error adding document",
                                it
                            )
                        }
                } else {
                    throw NetworkException(context.getString(R.string.network_error_message))
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