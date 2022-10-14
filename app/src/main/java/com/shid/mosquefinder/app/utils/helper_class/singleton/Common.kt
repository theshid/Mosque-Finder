package com.shid.mosquefinder.app.utils.helper_class.singleton

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.shid.mosquefinder.app.utils.helper_class.SharePref
import com.shid.mosquefinder.data.api.ApiClient
import com.shid.mosquefinder.data.api.GoogleApiInterface
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object Common {
    const val GOOGLE_API_URL = "https://maps.googleapis.com/"
    const val DEEPL_API_URL = " https://api-free.deepl.com/"
    const val QURAN_API_URL = "http://api.alquran.cloud/"

    const val GOOGLE = "Google"
    const val DEEPL = "DeepL"
    const val QURAN = "Quran"

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

    const val LOCATION_PERMISSION_REQUEST_CODE = 999


    fun logErrorMessage(errorMessage: String?) {
        Log.d(TAG, errorMessage.toString())
    }


    val googleApiService: GoogleApiInterface
        get() = ApiClient.getClient(GOOGLE_API_URL).create(GoogleApiInterface::class.java)

    val deeplApiService: GoogleApiInterface
        get() = ApiClient.getClient(DEEPL_API_URL).create(GoogleApiInterface::class.java)

    val frenchQuranApiService: GoogleApiInterface
        get() = ApiClient.getClient(QURAN_API_URL).create(GoogleApiInterface::class.java)

    suspend fun retrievePushId(context: Context): String? {
        return suspendCoroutine { cont ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if (!it.isSuccessful) {
                    cont.resume(null)
                }
                val newPushId = it.result
                if (!newPushId.isNullOrBlank()) {
                    val sharePref = SharePref(context)
                    sharePref.saveFirebaseToken(newPushId)
                }
                cont.resume(newPushId)
            }
        }
    }
}