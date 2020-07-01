package com.shid.mosquefinder.Model

import com.google.android.gms.maps.model.Polyline
import com.google.maps.model.DirectionsLeg

class PolylineData constructor(var polyline: Polyline, var leg: DirectionsLeg) {

    override fun toString(): String {
        return "PolylineData{" +
                "polyline=" + polyline +
                ", leg=" + leg +
                '}'
    }
}