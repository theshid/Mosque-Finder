package com.shid.mosquefinder.data.local

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shid.mosquefinder.R
import com.shid.mosquefinder.data.local.database.QuranDao
import com.shid.mosquefinder.data.local.database.entities.*
import com.shid.mosquefinder.app.ui.main.views.LoadingActivity
import com.shid.mosquefinder.app.utils.loadJsonArray
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject

class QuranDatabaseCallback @Inject constructor(
    private val scope: CoroutineScope,
    private val resources: Resources,
    private val mContext: Context,
    private val dao: QuranDao
) : RoomDatabase.Callback() {


    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        scope.launch {
            async { loadAyah(dao, mContext) }.await()
            async { loadNames(dao) }.await()
            async { loadSurahs(dao) }.await()

        }

    }

    private suspend fun loadSurahs(surahDao: QuranDao) {
        GlobalScope.launch(Dispatchers.IO) {
            val surahs = loadSurahsJsonArray()
            try {
                for (i in 0 until surahs!!.length()) {
                    val surah = surahs.getJSONObject(i)

                    try {
                        Log.d("Db", "insertion surah")
                        surahDao.insertSurah(
                            SurahDb(
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
    }

    private suspend fun loadNames(surahDao: QuranDao) {
        GlobalScope.launch(Dispatchers.IO) {
            val categories = loadJsonArray(resources, R.raw.category, "categories")

            try {
                for (i in 0 until categories!!.length()) {
                    val category = categories.getJSONObject(i)

                    try {
                        Timber.d("Db:insertion Categories")
                        surahDao.insertCategory(
                            Category(
                                0,
                                category.getString("category_name")

                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }


                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            val chapters = loadJsonArray(resources, R.raw.chapter, "chapters")

            try {
                for (i in 0 until chapters!!.length()) {
                    val chapter = chapters.getJSONObject(i)

                    try {
                        Timber.d("Db: insertion chapters")
                        surahDao.insertChapter(
                            Chapter(
                                0,
                                chapter.getString("chapter_name"),
                                chapter.getInt("category_id")
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }


                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            val divineNames = loadJsonArray(resources, R.raw.noms, "noms")
            try {
                for (i in 0 until divineNames!!.length()) {
                    val divineName = divineNames.getJSONObject(i)

                    try {
                        Timber.d("Db: insertion divine names")
                        surahDao.insertDivineName(
                            DivineName(
                                0,
                                divineName.getString("name")
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
    }

    private suspend fun loadAyah(surahDao: QuranDao, context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            val ayahs = loadJsonArray(resources, R.raw.quran, "ayahs")

            try {
                for (i in 0 until ayahs!!.length()) {
                    val ayah = ayahs.getJSONObject(i)

                    try {
                        Timber.d("Db: insertion ayah")
                        surahDao.insertAyah(
                            Ayah(
                                0,
                                ayah.getInt("surah_number"), ayah.getInt("verse_number"),
                                ayah.getString("text"),
                                ayah.getString("translation")

                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
                val intent = Intent(LoadingActivity.FILTER)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

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