package com.shid.mosquefinder.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

internal inline fun <reified T : Activity> Context.startActivity(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    block(intent)
    startActivity(intent)
}

internal fun Context.showToast(message: String,duration:Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, duration).show()
}

internal fun Context.loadColor(@ColorRes colorRes: Int):Int{
    return ContextCompat.getColor(this,colorRes)
}