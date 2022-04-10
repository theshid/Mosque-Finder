package com.shid.mosquefinder.Data.Model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Article(
    var title: String = "",
    var title_fr: String = "",
    var author: String = "",
    var body: String = "",
    var pic: String = "",
    var body_fr: String = ""
) :
    Parcelable {
}
