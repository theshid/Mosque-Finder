package com.shid.mosquefinder.data.model

import com.google.firebase.firestore.GeoPoint

class Mosque (

    var name: String ="",

    var position: GeoPoint = GeoPoint(0.0, 0.0),


    var documentId: String = "0",

    var report: Long = 0


) {


    fun Mosque(){

    }
    override fun toString(): String {
        return "MosqueLocation{" +
                "geo_point=" + position +
                ", name=" + name +
                '}'
    }

}