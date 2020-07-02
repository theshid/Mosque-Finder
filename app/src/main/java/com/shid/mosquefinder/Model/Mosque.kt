package com.shid.mosquefinder.Model

import com.google.firebase.firestore.GeoPoint

class Mosque constructor(

    var name: String,

    var position: GeoPoint


) {
    override fun toString(): String? {
        return "MosqueLocation{" +
                "geo_point=" + position +
                ", name=" + name +
                '}'
    }

}