package com.shid.mosquefinder.Model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class ClusterMarker: ClusterItem {

     val mComefromGooglePlace: Boolean
    private val mPosition: LatLng
    private val mTitle: String
    private val mSnippet: String
     var mPic: String
    get() = field

    constructor(lat: Double, lng: Double) {
        mPosition = LatLng(lat, lng)
        mTitle = ""
        mSnippet = ""
        mPic = ""
        mComefromGooglePlace = false
    }

    constructor(lat: Double, lng: Double, title: String, snippet: String, pic: String, place:Boolean) {
        mPosition = LatLng(lat, lng)
        mTitle = title
        mSnippet = snippet
        mPic = pic
        mComefromGooglePlace = place
    }

    override fun getPosition(): LatLng {
        return mPosition
    }

    override fun getTitle(): String {
        return mTitle
    }

    override fun getSnippet(): String {
        return mSnippet
    }



}