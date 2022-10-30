package com.shid.mosquefinder.data.model

import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class User(var uid: String = "", var name: String? = "", var email: String? = "") :
    Serializable {

    val mUid: String = uid

    val mName: String? = name

    @SuppressWarnings("WeakerAccess")
    val mEmail: String? = email

    @Exclude
    var isAuthenticated: Boolean? = null

    @Exclude
    var isNew: Boolean? = null

    @Exclude
    var isCreated: Boolean? = null


}