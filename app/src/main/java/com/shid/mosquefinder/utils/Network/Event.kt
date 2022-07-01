package com.shid.mosquefinder.utils.Network

sealed class Event {
    class ConnectivityEvent(val isConnected: Boolean) : Event()
}