package com.shid.mosquefinder.app.ui.base

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.shid.mosquefinder.app.utils.NetworkUtil

open class BaseActivity : AppCompatActivity(){

    protected fun onNetworkChange(block: (Boolean) -> Unit) {
        NetworkUtil.getNetworkStatus(this)
            .observe(this, Observer { isConnected ->
                block(isConnected)
            })
    }
}