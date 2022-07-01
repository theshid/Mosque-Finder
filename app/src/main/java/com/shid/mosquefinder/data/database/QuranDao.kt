package com.shid.mosquefinder.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.shid.mosquefinder.data.database.entities.*

@Dao
interface QuranDao {
    @Insert
    suspend fun insertSurah(vararg surah: Surah)

    @Insert
    suspend fun insertAyah(vararg ayah: Ayah)

    @Insert
    suspend fun insertCategory(vararg category: Category)

    @Insert
    suspend fun insertChapter(vararg chapter: Chapter)

    @Insert
    suspend fun insertItem(vararg item: Item)

    @Insert
    suspend fun insertDivineName(vararg name: DivineName)

    @get:Query("SELECT * FROM surahs ORDER BY RANDOM() LIMIT 1")
    val randomSurah: Surah

    @get:Query("SELECT * FROM ayahs ORDER BY RANDOM() LIMIT 1")
    val randomAyah: Ayah

    @Query("SELECT * FROM surahs ORDER BY number")
    fun getSurahs():List<Surah>

    @Query("SELECT * FROM surahs WHERE number IN (:surahNumber)")
    fun getSurahByNumber(surahNumber: Int):Surah

    @Query("SELECT * FROM surahs WHERE number < :surahNumber")
    fun getSurahList(surahNumber: Int):List<Surah>

    @Query("SELECT * FROM ayahs WHERE surah_number IN (:surahNumber)")
    fun getAyah(surahNumber:Int):List<Ayah>

    @Query("SELECT * FROM category ")
    fun getCategories():List<Category>

    @Query("SELECT * FROM chapter WHERE category_id IN (:categoryId)")
    fun getChapters(categoryId:Int):List<Chapter>

    @Query("SELECT * FROM item WHERE chapter_id IN (:chapterId)")
    fun getItems(chapterId:Int):List<Item>

    @Query("SELECT * FROM noms ")
    fun getDivineNames():List<DivineName>

    @Query("UPDATE ayahs SET french_text=:french WHERE id = :verseId")
    fun updateAyah(french:String, verseId:Long)

    @Query("SELECT * FROM ayahs WHERE surah_number IN (:surahNumber) ORDER BY RANDOM() LIMIT 1")
    fun getRandomAyah(surahNumber:Int):Ayah
}