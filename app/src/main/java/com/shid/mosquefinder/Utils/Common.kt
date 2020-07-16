package com.shid.mosquefinder.Utils

import com.shid.mosquefinder.Api.ApiClient
import com.shid.mosquefinder.Api.ApiInterface

object Common {
    private val GOOGLE_API_URL = "https://maps.googleapis.com/"

    val googleApiService: ApiInterface
        get() = ApiClient.getClient(GOOGLE_API_URL).create(ApiInterface::class.java)
}