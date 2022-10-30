package com.shid.mosquefinder.app.di

import com.shid.mosquefinder.app.utils.helper_class.CoroutinesQualifiers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object CoroutinesScopesModule {
    @ApplicationScope
    @Singleton
    @Provides
    fun providesCoroutineScope(
        @CoroutinesQualifiers.DefaultDispatcher defaultDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + defaultDispatcher)

    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class ApplicationScope
}