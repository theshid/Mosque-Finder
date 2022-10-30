package com.shid.mosquefinder.app.store

import dagger.assisted.AssistedFactory

@AssistedFactory
internal interface CrawlerStateSourceDelegateFactory {
    fun create(filename: String): CrawlerStateSourceDelegate
}