package com.shid.mosquefinder.Utils.Network

sealed class Event {
    class ConnectivityEvent(val isConnected: Boolean) : Event()
}