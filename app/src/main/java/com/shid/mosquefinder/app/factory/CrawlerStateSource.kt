package com.shid.mosquefinder.app.factory

import androidx.datastore.core.DataStore
import com.shid.mosquefinder.app.store.CrawlerState
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CrawlerStateSource @Inject constructor(actual: DataStore<CrawlerState>) :
    DataStore<CrawlerState> by actual