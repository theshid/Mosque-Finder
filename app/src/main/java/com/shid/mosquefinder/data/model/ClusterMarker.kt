package com.shid.mosquefinder.data.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import kotlinx.android.parcel.Parcelize

@Parcelize
class ClusterMarker(
    var lat: Double,
    var lng: Double,
    private var title: String,
    private var snippet: String,
    var pic: String,
    var place: Boolean,
    var distance: Double
) : ClusterItem, Parcelable {
    val distanceFromUser: Double = distance
    val isMarkerFromGooglePlace: Boolean = place
    private val mPosition: LatLng = LatLng(lat, lng)
    private val mTitle: String = title
    private val mSnippet: String = snippet
    var mPic: String = pic

    override fun getSnippet(): String? {
        return mSnippet
    }

    override fun getTitle(): String? {
        return mTitle
    }

    /*  constructor(lat: Double, lng: Double) {
          mPosition = LatLng(lat, lng)
          mTitle = ""
          mSnippet = ""
          mPic = ""
          isMarkerFromGooglePlace = false
          distanceFromUser = 0.0
      }*/

    /* constructor(lat: Double, lng: Double, title: String, snippet: String, pic: String, place:Boolean,distance:Double) {
         mPosition = LatLng(lat, lng)
         mTitle = title
         mSnippet = snippet
         mPic = pic
         isMarkerFromGooglePlace = place
         distanceFromUser = distance
     }*/

    override fun getPosition(): LatLng {
        return mPosition
    }

    /* override fun getTitle(): String {
         return mTitle
     }

     override fun getSnippet(): String {
         return mSnippet
     }*/


}