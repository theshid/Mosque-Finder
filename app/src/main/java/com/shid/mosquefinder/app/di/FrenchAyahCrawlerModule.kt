package com.shid.mosquefinder.app.di

import androidx.datastore.core.DataStore
import com.shid.mosquefinder.app.factory.AppConfigFactory
import com.shid.mosquefinder.app.store.CrawlerState
import com.shid.mosquefinder.app.store.CrawlerStateSourceDelegateFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object FrenchAyahCrawlerModule {

    @Provides
    @Singleton
    internal fun provideRawStore(
        delegateFactory: CrawlerStateSourceDelegateFactory,
        appConfigFactory: AppConfigFactory,
    ): DataStore<CrawlerState> {
        val store by delegateFactory.create(appConfigFactory.create().workerStateStoreName)
        return store
    }

    /*@Provides
    @Singleton
    internal fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(NotificationManager::class.java)
    }*/

}

