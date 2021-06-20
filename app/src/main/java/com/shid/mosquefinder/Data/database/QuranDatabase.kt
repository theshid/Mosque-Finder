package com.shid.mosquefinder.Data.database

import android.content.Context
import android.content.res.Resources
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shid.mosquefinder.Data.database.entities.Ayah
import com.shid.mosquefinder.Data.database.entities.Surah
import com.shid.mosquefinder.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

@Database(entities = [Surah::class, Ayah::class], version = 1,exportSchema = true)
abstract class QuranDatabase: RoomDatabase() {

    abstract fun surahDao(): QuranDao



    companion object {

        @Volatile
        private var INSTANCE: QuranDatabase? = null

        fun getDatabase(
            context: Context,
            coroutineScope: CoroutineScope,
            resources: Resources
        ): QuranDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuranDatabase::class.java,
                    "quran_database"
                )
                    .addCallback(QuranDatabaseCallback(coroutineScope, resources))
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

   /* val MIGRATION_1_2 = object : Migration(1, 2){
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE ayahs ADD COLUMN <new-column-name> <column-data-type>" )
        }
    }*/

    private class QuranDatabaseCallback(
        private val scope: CoroutineScope,
        private val resources: Resources
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    val teaDao = database.surahDao()
                    fillWithStartingData(teaDao)

                }
            }
        }


        private suspend fun fillWithStartingData(surahDao: QuranDao) {

            val surahs = loadSurahsJsonArray()
            val ayahs = loadAyahsJsonArray()

            try {
                for (i in 0 until ayahs!!.length()) {
                    val ayah = ayahs.getJSONObject(i)

                    try {
                        surahDao.insertAyah(
                            Ayah(0,
                                ayah.getInt("surah_number"), ayah.getInt("verse_number"),
                                ayah.getString("text"),
                                ayah.getString("translation")

                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }


                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            try {
                for (i in 0 until surahs!!.length()) {
                    val surah = surahs.getJSONObject(i)

                    try {
                        surahDao.insertSurah(
                            Surah(
                                surah.getInt("number"), surah.getString("name"),
                                surah.getString("transliteration_en"),
                                surah.getString("translation_en"),
                                surah.getInt("total_verses"),
                                surah.getString("revelation_type")
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }


                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        private fun loadAyahsJsonArray(): JSONArray?{
            val builder = StringBuilder()
            val `in` =
                resources.openRawResource(R.raw.quran)
            val reader =
                BufferedReader(InputStreamReader(`in`))
            var line: String?
            try {
                while (reader.readLine().also { line = it } != null) {
                    builder.append(line)
                }
                val json = JSONObject(builder.toString())
                return json.getJSONArray("ayahs")
            } catch (exception: IOException) {
                exception.printStackTrace()
            } catch (exception: JSONException) {
                exception.printStackTrace()
            }
            return null
        }

        private fun loadSurahsJsonArray(): JSONArray? {
            val builder = StringBuilder()
            val `in` =
                resources.openRawResource(R.raw.surah)
            val reader =
                BufferedReader(InputStreamReader(`in`))
            var line: String?
            try {
                while (reader.readLine().also { line = it } != null) {
                    builder.append(line)
                }
                val json = JSONObject(builder.toString())
                return json.getJSONArray("surahs")
            } catch (exception: IOException) {
                exception.printStackTrace()
            } catch (exception: JSONException) {
                exception.printStackTrace()
            }
            return null
        }
    }


}