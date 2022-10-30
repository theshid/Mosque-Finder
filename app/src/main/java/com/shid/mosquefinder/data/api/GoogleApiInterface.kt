package com.shid.mosquefinder.data.api

import com.shid.mosquefinder.data.model.pojo.Place
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url


interface GoogleApiInterface {

    @GET
    fun getNearbyPlaces(@Url url: String): Call<Place>

}