package com.shid.mosquefinder.data.local.database.daos

import androidx.room.Dao
import androidx.room.Insert
import com.shid.mosquefinder.data.local.database.entities.Category
import com.shid.mosquefinder.data.local.database.entities.Chapter
import com.shid.mosquefinder.data.local.database.entities.DivineName
import com.shid.mosquefinder.data.local.database.entities.Item

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