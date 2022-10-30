package com.shid.mosquefinder.app.utils.helper_class.singleton

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object NetworkUtil {

    @Suppress("DEPRECATION")
    fun isOnline(context: Context): Boolean {
        val cm: ConnectivityManager? =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (cm != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                val ni = cm.activeNetworkInfo
                return ni?.isConnectedOrConnecting == true
            } else {
                val n = cm.activeNetwork
                if (n != null) {
                    val nc: NetworkCapabilities? = cm.getNetworkCapabilities(n)
                    return nc?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false ||
                            nc?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
                }
            }
        }
        return false
    }

    fun getNetworkStatus(context: Context): LiveData<Boolean> {
        val isAvailableLiveData = MutableLiveData<Boolean>()
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nr = NetworkRequest.Builder()

        cm.registerNetworkCallback(nr.build(), object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                isAvailableLiveData.postValue(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                isAvailableLiveData.postValue(false)

            }

        })
        return isAvailableLiveData
    }
}