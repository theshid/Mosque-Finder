package com.shid.mosquefinder.Data.Model.Api

import com.shid.mosquefinder.Data.Model.Pojo.DeepL
import com.shid.mosquefinder.Data.Model.Pojo.Place
import com.shid.mosquefinder.Data.Model.Pojo.Translation
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url


interface ApiInterface {

    @GET
    fun getNearbyPlaces(@Url url:String): retrofit2.Call<Place>

    @POST("v2/translate")
    fun getTranslation(
        @Query("auth_key") key:String,
        @Query("text") inputText:String,
        @Query("target_lang") targetLg:String
    ): Call<DeepL>

}