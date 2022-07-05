package com.shid.mosquefinder.app.di

import android.content.Context
import android.content.res.Resources
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.utils.FusedLocationWrapper
import com.shid.mosquefinder.app.utils.SharePref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun providesResources(@ApplicationContext context: Context):Resources = context.resources

    @Provides
    @Singleton
    fun provideSharedPrefManager(@ApplicationContext context: Context): SharePref {
        return SharePref(context)
    }

    @Singleton
    @Provides
    fun providesCoroutineScope(): CoroutineScope {
        // Run this code when providing an instance of CoroutineScope
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Singleton
    @Provides
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Singleton
    @Provides
    fun provideFusedLocationWrapper(fusedLocationProviderClient: FusedLocationProviderClient):FusedLocationWrapper = FusedLocationWrapper(fusedLocationProviderClient)

    @Singleton
    @Provides
    fun provideGoogleSignInOptions(resources: Resources):GoogleSignInOptions =  GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(resources.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
}