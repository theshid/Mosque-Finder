package com.shid.mosquefinder.app.utils.extensions

import android.view.View

internal fun View.show(){
    this.visibility = View.VISIBLE
}

internal fun View.hide(){
    this.visibility = View.INVISIBLE
}

internal fun View.remove(){
    this.visibility = View.GONE
}