package com.shid.mosquefinder.Utils

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.skydoves.transformationlayout.onTransformationEndContainer

fun AppCompatActivity.onTransformationEndContainerApplyParams() {
    onTransformationEndContainer(intent.getParcelableExtra("com.skydoves.transformationlayout"))
}

inline fun <reified VM : ViewModel> AppCompatActivity.viewModelOf(
    factory: ViewModelProvider.Factory
) = ViewModelProvider(this, factory).get(VM::class.java)