package com.shid.mosquefinder.Data.Model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
class BeautifulMosques(var name:String="",var description:String="", var link:String="",
var pic:String="", var pic2:String="", var pic3:String="",var description_fr:String="") :
    Parcelable {
}