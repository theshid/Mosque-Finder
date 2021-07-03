package com.shid.mosquefinder.Data.Model.Api

import com.shid.mosquefinder.Data.Model.Pojo.*
import retrofit2.Call
import retrofit2.http.*


interface ApiInterface {

    @GET
    fun getNearbyPlaces(@Url url:String): retrofit2.Call<Place>

    @POST("v2/translate")
    fun getTranslation(
        @Query("auth_key") key:String,
        @Query("text") inputText:String,
        @Query("target_lang") targetLg:String
    ): Call<DeepL>

    @GET("v1/surah/{surahNumber}/fr.hamidullah")
    fun getFrenchSurah(@Path("surahNumber") numSurah:Int):Call<Root>

}