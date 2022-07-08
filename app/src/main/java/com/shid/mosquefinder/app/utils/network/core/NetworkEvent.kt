package com.shid.mosquefinder.app.utils.network.core

import android.net.LinkProperties
import android.net.NetworkCapabilities
import com.shid.mosquefinder.app.utils.network.NetworkState

sealed class NetworkEvent {
    abstract val  state: NetworkState

    class AvailabilityEvent(override val state: NetworkState, val oldNetworkAvailability : Boolean, val oldConnectivity:Boolean): NetworkEvent()
    class NetworkCapabilityEvent(override val state: NetworkState, val old: NetworkCapabilities?) : NetworkEvent()
    class LinkPropertyChangeEvent(override val state: NetworkState, val old: LinkProperties?) : NetworkEvent()
}