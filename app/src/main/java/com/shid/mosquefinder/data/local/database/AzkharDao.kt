package com.shid.mosquefinder.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import com.shid.mosquefinder.data.local.database.entities.*

@Dao
interface AzkharDao {
    @Insert
    suspend fun insertCategory(vararg category: Category)

    @Insert
    suspend fun insertChapter(vararg chapter: Chapter)

    @Insert
    suspend fun insertItem(vararg item: Item)

    @Insert
    suspend fun insertDivineName(vararg name: DivineName)
}