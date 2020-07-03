package com.shid.mosquefinder.Model

import com.google.android.gms.maps.model.Polyline
import com.google.maps.model.DirectionsLeg

class PolylineData(var polyline: Polyline, var leg: DirectionsLeg) {

    var mPolyline:Polyline = polyline
    var mLeg: DirectionsLeg = leg

    override fun toString(): String {
        return "PolylineData{" +
                "polyline=" + polyline +
                ", leg=" + leg +
                '}'
    }
}