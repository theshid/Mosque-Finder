package com.shid.mosquefinder.app.utils.Network

import com.shid.mosquefinder.ConnectivityStateHolder

/**
 * When implemented by an Activity, it add hooks to network events thanks to ActivityLifecycleCallbacks
 * @see android.app.Application.ActivityLifecycleCallbacks
 */
interface NetworkConnectivityListener {

    /**
     * Put this at false to disable hooks ( enabled by default )
     */
    val shouldBeCalled: Boolean
        get() = true

    /**
     * Put this at false to disable hooks on resume ( enabled by default )
     */
    val checkOnResume: Boolean
        get() = true

    val isConnected: Boolean
        get() = ConnectivityStateHolder.isConnected


    fun networkConnectivityChanged(event: Event)
}
