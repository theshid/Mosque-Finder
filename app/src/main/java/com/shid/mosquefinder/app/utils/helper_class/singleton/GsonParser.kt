package com.shid.mosquefinder.app.utils.helper_class.singleton

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object GsonParser {
    private var gson: Gson? = null
    val gsonParser: Gson?
        get() {
            if (gson == null) {
                val builder = GsonBuilder()
                gson = builder.create()
            }
            return gson
        }


}