package com.shid.mosquefinder.Utils

import kotlinx.coroutines.*

fun doAsync(block: suspend CoroutineScope.() -> Unit) = GlobalScope.launch(block = block)
suspend fun <T> uiThread(block: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.Main, block)