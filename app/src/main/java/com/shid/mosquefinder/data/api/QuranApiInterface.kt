package com.shid.mosquefinder.data.api

import com.shid.mosquefinder.data.model.pojo.RootResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface QuranApiInterface {

    @GET("v1/surah/{surahNumber}/fr.hamidullah")
    suspend fun getFrenchSurah(@Path("surahNumber") numSurah:Long):Response<RootResponse>
}