package com.shid.mosquefinder.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.shid.mosquefinder.data.local.database.entities.*
import com.shid.mosquefinder.domain.model.Ayah
import com.shid.mosquefinder.domain.model.Surah
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM surahs ORDER BY number")
    suspend fun getSurahs():List<SurahDb>

    @Query("SELECT * FROM surahs WHERE number IN (:surahNumber)")
    fun getSurahByNumber(surahNumber: Int):SurahDb

    @Query("SELECT * FROM surahs WHERE number < :surahNumber")
    fun getSurahList(surahNumber: Int):List<SurahDb>

    @Query("SELECT * FROM ayahs WHERE surah_number IN (:surahNumber)")
    fun getAyah(surahNumber:Int):List<AyahDb>

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
    fun getRandomAyah(surahNumber:Int):AyahDb
}