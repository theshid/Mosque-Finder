package com.shid.mosquefinder.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BeautifulMosques(
    var name: String = "",
    var description: String = "",
    var link: String = "",
    var pic: String = "",
    var pic2: String = "",
    var pic3: String = "",
    var description_fr: String = ""
) :
    Parcelable {
}