package com.shid.mosquefinder.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.shid.mosquefinder.data.local.database.entities.*

@Dao
interface QuranDao {
    @Insert
    suspend fun insertSurah(vararg surahDb: SurahDb)

    @Insert
    suspend fun insertAyah(vararg ayah: AyahDb)

    @Insert
    suspend fun insertCategory(vararg category: Category)

    @Insert
    suspend fun insertChapter(vararg chapter: Chapter)

    @Insert
    suspend fun insertItem(vararg item: Item)

    @Insert
    suspend fun insertDivineName(vararg name: DivineName)

    @get:Query("SELECT * FROM surahs ORDER BY RANDOM() LIMIT 1")
    val randomSurahDb: SurahDb

    @get:Query("SELECT * FROM ayahs ORDER BY RANDOM() LIMIT 1")
    val randomAyah: AyahDb

    @Transaction
    @Query("SELECT * FROM surahs ORDER BY number")
    suspend fun getSurahs(): List<SurahDb>

    @Transaction
    @Query("SELECT * FROM surahs WHERE number IN (:surahNumber)")
    suspend fun getSurahByNumber(surahNumber: Int): SurahDb

    @Transaction
    @Query("SELECT * FROM surahs WHERE number < :surahNumber")
    suspend fun getSurahList(surahNumber: Int): List<SurahDb>

    @Transaction
    @Query("SELECT * FROM ayahs WHERE surah_number IN (:surahNumber)")
    suspend fun getAyah(surahNumber: Int): List<AyahDb>

    @Query("SELECT * FROM category ")
    fun getCategories(): List<Category>

    @Query("SELECT * FROM chapter WHERE category_id IN (:categoryId)")
    fun getChapters(categoryId: Int): List<Chapter>

    @Query("SELECT * FROM item WHERE chapter_id IN (:chapterId)")
    fun getItems(chapterId: Int): List<Item>

    @Query("SELECT * FROM noms ")
    fun getDivineNames(): List<DivineName>

    @Query("UPDATE ayahs SET french_text=:french WHERE id = :verseId")
    fun updateAyah(french: String, verseId: Long)

    @Query("SELECT * FROM ayahs WHERE surah_number IN (:surahNumber) ORDER BY RANDOM() LIMIT 1")
    fun getRandomAyah(surahNumber: Int): AyahDb
}