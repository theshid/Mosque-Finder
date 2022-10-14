package com.shid.mosquefinder.data.api

import com.shid.mosquefinder.data.model.pojo.DeepLResponse
import com.shid.mosquefinder.data.model.pojo.Place
import com.shid.mosquefinder.data.model.pojo.RootResponse
import retrofit2.Call
import retrofit2.http.*


interface GoogleApiInterface {

    @GET
    fun getNearbyPlaces(@Url url: String): retrofit2.Call<Place>

    @GET
    fun getGoogleNearbyPlaces(@Url url: String): Place

    @POST("v2/translate")
    fun getTranslation(
        @Query("auth_key") key: String,
        @Query("text") inputText: String,
        @Query("target_lang") targetLg: String
    ): Call<DeepLResponse>

    @GET("v1/surah/{surahNumber}/fr.hamidullah")
    fun getFrenchSurah(@Path("surahNumber") numSurah: Int): Call<RootResponse>

}