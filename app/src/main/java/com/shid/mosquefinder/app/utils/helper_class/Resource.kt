package com.shid.mosquefinder.app.utils.helper_class

import com.shid.mosquefinder.app.utils.enums.Status

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
//utility class that will be responsible to communicate the current state of Network Call to the UI Layer.
    companion object {

        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }

    }

}