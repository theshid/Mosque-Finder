package com.shid.mosquefinder.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shid.mosquefinder.data.local.database.daos.AzkharDao
import com.shid.mosquefinder.data.local.database.daos.QuranDao
import com.shid.mosquefinder.data.local.database.entities.*

@Database(
    entities = [SurahDb::class, AyahDb::class, Category::class, Chapter::class, DivineName::class,
        Item::class], version = 3, exportSchema = true
)
abstract class QuranDatabase : RoomDatabase() {

    abstract fun surahDao(): QuranDao
    abstract fun azkharDao(): AzkharDao

    companion object {
        const val DATABASE_NAME: String = "quran_database"
    }
}