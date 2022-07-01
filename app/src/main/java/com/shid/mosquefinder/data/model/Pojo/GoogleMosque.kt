package com.shid.mosquefinder.data.model.Pojo

class GoogleMosque (
    var latitude:Double = 0.0,
    var longitude:Double = 0.0,
    var placeId:String= "jdieo",
    var placeName: String = "mosque",
    var rating:Long = 3,
    var types:List<String> = listOf(),
    var vicinity:String = "default"
){
    fun Googlemosque(){

    }
}