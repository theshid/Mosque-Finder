package com.shid.mosquefinder.Utils

import android.util.Log
import com.shid.mosquefinder.Data.Model.Api.ApiClient
import com.shid.mosquefinder.Data.Model.Api.ApiInterface

object Common {
    private val GOOGLE_API_URL = "https://maps.googleapis.com/"
    var RC_SIGN_IN = 123
    var USER = "user"
    var USERS = "users"
    var TAG = "FirebaseAuthAppTag"

    fun logErrorMessage(errorMessage: String?) {
        Log.d(TAG, errorMessage)
    }


    val googleApiService: ApiInterface
        get() = ApiClient.getClient(GOOGLE_API_URL).create(
            ApiInterface::class.java)
}