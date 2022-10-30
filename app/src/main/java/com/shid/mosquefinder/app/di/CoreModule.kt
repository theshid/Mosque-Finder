package com.shid.mosquefinder.app.di

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.utils.helper_class.Constants
import com.shid.mosquefinder.app.utils.helper_class.FusedLocationWrapper
import com.shid.mosquefinder.app.utils.helper_class.SharePref
import com.shid.mosquefinder.data.model.User
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
        }
    }

    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun providesResources(@ApplicationContext context: Context): Resources = context.resources

    @Provides
    @Singleton
    fun provideSharedPrefManager(@ApplicationContext context: Context): SharePref {
        return SharePref(context)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(Constants.PREF_FILENAME, Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Singleton
    @Provides
    fun provideFusedLocationWrapper(fusedLocationProviderClient: FusedLocationProviderClient): FusedLocationWrapper =
        FusedLocationWrapper(fusedLocationProviderClient)

    @Singleton
    @Provides
    fun provideGoogleSignInOptions(resources: Resources): GoogleSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(resources.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

    @Singleton
    @Provides
    fun provideGoogleSignInClient(
        googleSignInOptions: GoogleSignInOptions,
        @ApplicationContext context: Context
    ): GoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    @Singleton
    @Provides
    fun provideUser(): User = User()

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(NotificationManager::class.java)
    }

    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager {
        return context.getSystemService(AlarmManager::class.java)
    }
}