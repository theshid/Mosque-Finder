package com.shid.mosquefinder.Model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class ClusterMarker(
    position: LatLng,
    title: String,
    snippet: String,
    var iconPic: String = "default"
) : ClusterItem {

    private var picture = iconPic
        get() = field


    override fun getSnippet(): String? {
        return snippet
    }

    override fun getTitle(): String? {
        return title
    }

    override fun getPosition(): LatLng {
        return position
    }


}