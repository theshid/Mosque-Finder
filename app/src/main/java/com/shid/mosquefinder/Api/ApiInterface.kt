package com.shid.mosquefinder.Api

import com.google.maps.model.LatLng
import com.google.maps.model.PlacesSearchResponse
import com.shid.mosquefinder.App
import com.shid.mosquefinder.Model.Pojo.Place
import com.shid.mosquefinder.R
import com.squareup.okhttp.Call
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url


interface ApiInterface {

    @GET
    fun getNearbyPlaces(@Url url:String): retrofit2.Call<Place>
}