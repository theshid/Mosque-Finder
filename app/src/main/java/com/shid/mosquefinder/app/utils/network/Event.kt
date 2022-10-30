package com.shid.mosquefinder.app.utils.network

sealed class Event {
    class ConnectivityEvent(val isConnected: Boolean) : Event()
}