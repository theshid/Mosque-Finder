package com.shid.mosquefinder.data.api

import com.shid.mosquefinder.data.model.pojo.Root
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface QuranApiInterface {

    @GET("v1/surah/{surahNumber}/fr.hamidullah")
    fun getFrenchSurah(@Path("surahNumber") numSurah:Int):Root
}