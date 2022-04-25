package com.shid.mosquefinder.Utils

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.shid.mosquefinder.R
import com.skydoves.transformationlayout.onTransformationEndContainer

fun AppCompatActivity.onTransformationEndContainerApplyParams() {
    onTransformationEndContainer(intent.getParcelableExtra("com.skydoves.transformationlayout"))
}

inline fun <reified VM : ViewModel> AppCompatActivity.viewModelOf(
    factory: ViewModelProvider.Factory
) = ViewModelProvider(this, factory).get(VM::class.java)


internal fun Activity.showSnackbar(view: View, message: String, isError: Boolean = false,duration:Int = 0 ) {
    val sb = Snackbar.make(view,message, duration)

    if (isError){
        sb.setBackgroundTint(loadColor(R.color.colorRed))
            .setTextColor(loadColor(R.color.white))
            .show()
    } else{
        sb.setBackgroundTint(loadColor(R.color.colorPrimary))
            .setTextColor(loadColor(R.color.white))
            .show()
    }

if (duration == -2){
    Handler(Looper.getMainLooper()).postDelayed({
        sb.dismiss()
    }, 10000)
}

}