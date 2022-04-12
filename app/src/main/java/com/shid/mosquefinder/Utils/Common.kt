package com.shid.mosquefinder.Utils

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.shid.mosquefinder.Data.Api.ApiClient
import com.shid.mosquefinder.Data.Model.Api.ApiInterface
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object Common {
    private const val GOOGLE_API_URL = "https://maps.googleapis.com/"
    private const val DEEPL_API_URL = " https://api-free.deepl.com/"
    private const val QURAN_API_URL = "http://api.alquran.cloud/"
    var RC_SIGN_IN = 123
    var USER = "user"
    var USERS = "users"
    var USER_LAT = "user_lat"
    var USER_LG = "user_lg"
    var TAG = "FirebaseAuthAppTag"


    fun logErrorMessage(errorMessage: String?) {
        Log.d(TAG, errorMessage.toString())
    }


    val googleApiService: ApiInterface
        get() = ApiClient.getClient(GOOGLE_API_URL).create(ApiInterface::class.java)

    val deeplApiService: ApiInterface
        get() = ApiClient.getClient(DEEPL_API_URL).create(ApiInterface::class.java)

    val frenchQuranApiService: ApiInterface
        get() = ApiClient.getClient(QURAN_API_URL).create(ApiInterface::class.java)

    suspend fun retrievePushId(context: Context):String?{
        return suspendCoroutine { cont ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if (!it.isSuccessful){
                    cont.resume(null)
                }
                val newPushId = it.result
                if (!newPushId.isNullOrBlank()){
                    val sharePref = SharePref(context)
                    sharePref.saveFirebaseToken(newPushId)
                }
                cont.resume(newPushId)
            }
        }
    }
}