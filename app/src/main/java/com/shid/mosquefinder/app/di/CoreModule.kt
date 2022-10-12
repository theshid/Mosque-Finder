package com.shid.mosquefinder.app.di

import android.content.Context
import android.content.res.Resources
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.shid.mosquefinder.app.utils.helper_class.FusedLocationWrapper
import com.shid.mosquefinder.app.utils.helper_class.SharePref
import com.shid.mosquefinder.data.model.User
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    fun providesResources(@ApplicationContext context: Context): Resources = context.resources

    @Provides
    @Singleton
    fun provideSharedPrefManager(@ApplicationContext context: Context): SharePref {
        return SharePref(context)
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
            .requestIdToken("478378697299-gu5brmfqnlm6f9k2k0vigdakb4ki2l3f.apps.googleusercontent.com")
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
}