package com.shid.mosquefinder.data.local

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.di.CoroutinesScopesModule
import com.shid.mosquefinder.data.local.database.QuranDao
import com.shid.mosquefinder.data.local.database.entities.*
import com.shid.mosquefinder.app.ui.main.views.LoadingActivity
import com.shid.mosquefinder.app.utils.loadJsonArray
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Provider

class QuranDatabaseCallback @Inject constructor(
    @CoroutinesScopesModule.ApplicationScope private val scope: CoroutineScope,
    private val resources: Resources,
    @ApplicationContext private val mContext: Context,
    private val dao: Provider<QuranDao>
) : RoomDatabase.Callback() {


    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Timber.d("Called")
        scope.launch(Dispatchers.IO) {
            /*withContext(Dispatchers.Default) {
                loadAyah(
                    dao.get(),
                    mContext
                )
            }*/
            async { loadAyah(dao.get()) }.await()
            async { loadNames(dao.get()) }.await()
            async { loadSurahs(dao.get()) }.await()
            val intent = Intent(LoadingActivity.FILTER)
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
        }

    }

    private suspend fun loadSurahs(surahDao: QuranDao) {
        GlobalScope.launch(Dispatchers.IO) {
            val surahs = loadSurahsJsonArray()
            try {
                for (i in 0 until surahs!!.length()) {
                    val surah = surahs.getJSONObject(i)

                    try {
                        Timber.d("insertion surah")
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
                        Timber.e("error:${e.message}")
                        e.printStackTrace()
                    }


                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Timber.e("error:${e.message}")
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
                        Timber.e("error:${e.message}")
                    }


                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Timber.e("error:${e.message}")
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
                        Timber.e("error:${e.message}")
                    }


                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Timber.e("error:${e.message}")
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
                        Timber.e("error:${e.message}")
                    }


                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Timber.e("error:${e.message}")
            }
        }
    }

    private suspend fun loadAyah(surahDao: QuranDao) {
        GlobalScope.launch(Dispatchers.IO) {
            val ayahs = loadJsonArray(resources, R.raw.quran, "ayahs")

            try {
                for (i in 0 until ayahs!!.length()) {
                    val ayah = ayahs.getJSONObject(i)

                    try {
                        Timber.d("Db: insertion ayah")
                        surahDao.insertAyah(
                            AyahDb(
                                0,
                                ayah.getInt("surah_number"), ayah.getInt("verse_number"),
                                ayah.getString("text"),
                                ayah.getString("translation")

                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Timber.e("error:${e.message}")
                    }

                }
                /*val intent = Intent(LoadingActivity.FILTER)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)*/
            } catch (e: JSONException) {
                e.printStackTrace()
                Timber.e("error:${e.message}")
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
            Timber.e("error:${exception.message}")
        } catch (exception: JSONException) {
            exception.printStackTrace()
            Timber.e("error:${exception.message}")
        }
        return null
    }


}