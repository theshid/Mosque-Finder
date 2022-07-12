package com.shid.mosquefinder.app.di

import android.content.res.Resources
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.shid.mosquefinder.data.api.DeeplApiInterface
import com.shid.mosquefinder.data.local.database.QuranDao
import com.shid.mosquefinder.data.model.User
import com.shid.mosquefinder.data.repository.*
import com.shid.mosquefinder.domain.repository.AzkharRepository
import com.shid.mosquefinder.domain.repository.ChapterRepository
import com.shid.mosquefinder.domain.repository.QuoteRepository
import dagger.Provides
import javax.inject.Singleton

object RemoteDataSourceModule {

    @Provides
    @Singleton
    fun provideAzkharTranslation(resource: Resources, api: DeeplApiInterface): AzkharRepository =
        AzkharRepositoryImpl(resource, api)

    @Provides
    @Singleton
    fun provideBlogRepository(firestore: FirebaseFirestore): BlogRepository =
        BlogRepository(firestore)

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
    fun provideQuoteRepository(database: FirebaseFirestore):QuoteRepository = QuoteRepositoryImpl(database)

    @Provides
    @Singleton
    fun provideChapterRepository(dao: QuranDao):ChapterRepository = ChapterRepositoryImpl(dao)
}