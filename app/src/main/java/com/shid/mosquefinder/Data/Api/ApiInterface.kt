package com.shid.mosquefinder.Data.Model.Api

import com.shid.mosquefinder.Data.Model.Pojo.Place
import retrofit2.http.GET
import retrofit2.http.Url


interface ApiInterface {

    @GET
    fun getNearbyPlaces(@Url url:String): retrofit2.Call<Place>
}