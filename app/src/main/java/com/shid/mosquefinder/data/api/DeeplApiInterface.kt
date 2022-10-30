package com.shid.mosquefinder.data.api

import com.shid.mosquefinder.data.model.pojo.DeepLResponse
import retrofit2.http.POST
import retrofit2.http.Query

interface DeeplApiInterface {

    @POST("v2/translate")
    suspend fun getTranslation(
        @Query("auth_key") key: String,
        @Query("text") inputText: String,
        @Query("target_lang") targetLg: String
    ): DeepLResponse
}