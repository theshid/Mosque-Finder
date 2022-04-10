package com.shid.mosquefinder.Utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast

internal inline fun <reified T : Activity> Context.startActivity(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    block(intent)
    startActivity(intent)
}

internal fun Context.showToast(message: String,duration:Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, duration).show()
}