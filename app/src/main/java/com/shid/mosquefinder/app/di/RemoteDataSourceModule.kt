package com.shid.mosquefinder.app.di

import android.content.Context
import android.content.res.Resources
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.shid.mosquefinder.data.api.DeeplApiInterface
import com.shid.mosquefinder.data.api.QuranApiInterface
import com.shid.mosquefinder.data.local.database.QuranDao
import com.shid.mosquefinder.data.model.Api.GoogleApiInterface
import com.shid.mosquefinder.data.model.User
import com.shid.mosquefinder.data.repository.*
import com.shid.mosquefinder.domain.repository.AzkharRepository
import com.shid.mosquefinder.domain.repository.ChapterRepository
import com.shid.mosquefinder.domain.repository.QuoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteDataSourceModule {

    @Provides
    @Singleton
    fun provideAzkharTranslation(resource: Resources, api: DeeplApiInterface): AzkharRepository =
        AzkharRepositoryImpl(resource, api)

    @Provides
    @Singleton
    fun provideBlogRepository(firestore: FirebaseFirestore): BlogRepositoryImpl =
        BlogRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideSplashRepository(
        firebaseAuth: FirebaseAuth,
        rootRef: FirebaseFirestore,
        crashlytics: FirebaseCrashlytics,
        user: User
    ): SplashRepository = SplashRepository(firebaseAuth, rootRef, crashlytics, user)

    @Provides
    @Singleton
    fun provideQuoteRepository(database: FirebaseFirestore): QuoteRepository =
        QuoteRepositoryImpl(database)

    @Provides
    @Singleton
    fun provideChapterRepository(dao: QuranDao): ChapterRepository = ChapterRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideAyahRepository(dao: QuranDao, api: QuranApiInterface) = AyahRepositoryImpl(dao, api)

    @Provides
    @Singleton
    fun provideMapRepository(service: GoogleApiInterface, @ApplicationContext context: Context) =
        MapRepository(service, context)

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth, rootRef: FirebaseFirestore) =
        AuthRepositoryImpl(firebaseAuth, rootRef)

    @Provides
    @Singleton
    fun provideBeautifulMosqueRepository(firestore: FirebaseFirestore) = BeautifulMosquesRepository(firestore)
}