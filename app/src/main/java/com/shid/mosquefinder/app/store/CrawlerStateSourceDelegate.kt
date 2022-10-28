package com.shid.mosquefinder.app.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal class CrawlerStateSourceDelegate @AssistedInject constructor(
    @Assisted private val filename: String,
    private val serializer: CrawlerStateSerializer,
    @ApplicationContext val context: Context
) : ReadOnlyProperty<Any?, DataStore<CrawlerState>> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): DataStore<CrawlerState> {
        return dataStore(filename, serializer).getValue(context, property)
    }
}

