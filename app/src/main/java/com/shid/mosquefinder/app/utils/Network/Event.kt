package com.shid.mosquefinder.app.utils.Network

sealed class Event {
    class ConnectivityEvent(val isConnected: Boolean) : Event()
}