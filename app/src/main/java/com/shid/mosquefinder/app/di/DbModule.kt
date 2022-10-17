package com.shid.mosquefinder.app.di

import android.content.Context
import android.content.res.Resources
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shid.mosquefinder.data.local.database.QuranDao
import com.shid.mosquefinder.data.local.database.QuranDatabase
import com.shid.mosquefinder.data.local.QuranDatabaseCallback
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Singleton
    @Provides
    @Synchronized
    fun provideAppDb(@ApplicationContext context: Context, quranDatabaseCallback: QuranDatabaseCallback): QuranDatabase {
        val MIGRATION_1_2 = object : Migration(1, 2){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ayahs ADD COLUMN french_text TEXT" )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `category` (`id` INTEGER NOT NULL," +
                        " `category_name` TEXT NOT NULL, " +
                        "PRIMARY KEY(`id`))");
                database.execSQL("CREATE TABLE `chapter` (`id` INTEGER NOT NULL," +
                        " `chapter_name` TEXT NOT NULL, " +
                        " `category_id` INTEGER NOT NULL, PRIMARY KEY(`id`))");
                database.execSQL("CREATE TABLE `item` (`id` INTEGER NOT NULL, " +
                        "`item_translation` TEXT NOT NULL, " +
                        "`chapter_id` INTEGER NOT NULL, PRIMARY KEY(`id`))");
                database.execSQL("CREATE TABLE `noms` (`id` INTEGER NOT NULL," +
                        " `name` TEXT NOT NULL, " +
                        "PRIMARY KEY(`id`))")
            }
        }


        return  Room
            .databaseBuilder(context, QuranDatabase::class.java, QuranDatabase.DATABASE_NAME)
            .addMigrations(MIGRATION_1_2,MIGRATION_2_3)
            .addCallback(quranDatabaseCallback)
            .setJournalMode(RoomDatabase.JournalMode.AUTOMATIC)
            .build()
    }

    @Provides
    @Singleton
    fun provideQuranDatabaseCallback(scope: CoroutineScope,resources: Resources,
    @ApplicationContext context: Context, dao: Provider<QuranDao>):QuranDatabaseCallback{
        return QuranDatabaseCallback(scope,resources,context,dao)
    }


    @Provides
    @Singleton
    fun provideSurahDao(db:QuranDatabase): QuranDao = db.surahDao()
}