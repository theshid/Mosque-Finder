package com.shid.mosquefinder.utils

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.shid.mosquefinder.data.api.ApiClient
import com.shid.mosquefinder.data.model.Api.ApiInterface
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object Common {
    private const val GOOGLE_API_URL = "https://maps.googleapis.com/"
    private const val DEEPL_API_URL = " https://api-free.deepl.com/"
    private const val QURAN_API_URL = "http://api.alquran.cloud/"
    const val FAJR = "Fajr"
    const val DHUR = "Dhur"
    const val ASR = "Asr"
    const val MAGHRIB = "Maghrib"
    const val ISHA = "Isha"

    const val FAJR_STATE = "Fajr_STATE"
    const val DHUR_STATE = "Dhur_STATE"
    const val ASR_STATE = "Asr_STATE"
    const val MAGHRIB_STATE = "Maghrib_STATE"
    const val ISHA_STATE = "Isha_STATE"

    const val FAJR_INDEX = 0
    const val DHUR_INDEX = 1
    const val ASR_INDEX = 2
    const val MAGHRIB_INDEX = 3
    const val ISHA_INDEX = 4

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