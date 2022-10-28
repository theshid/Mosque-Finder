package com.shid.mosquefinder.app.utils.extensions

import android.net.Uri
import retrofit2.Response

fun <T> Response<T>.requireBody() = body() ?: error("Body does not exist!")

fun String.toUri(): Uri = Uri.parse(this)

