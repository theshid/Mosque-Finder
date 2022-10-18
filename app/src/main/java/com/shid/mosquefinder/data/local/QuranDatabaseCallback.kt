package com.shid.mosquefinder.data.local

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.di.CoroutinesScopesModule
import com.shid.mosquefinder.app.ui.main.views.LoadingActivity
import com.shid.mosquefinder.app.utils.helper_class.Constants
import com.shid.mosquefinder.app.utils.loadJsonArray
import com.shid.mosquefinder.data.local.database.QuranDao
import com.shid.mosquefinder.data.local.database.entities.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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
        scope.launch(Dispatchers.IO) {
            async { loadAyah(dao.get()) }.await()
            async { loadNames(dao.get()) }.await()
            async { loadSurahs(dao.get()) }.await()
            sendBroadcast()
        }

    }

    private fun sendBroadcast() {
        val intent = Intent(LoadingActivity.FILTER)
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
    }

    private suspend fun loadSurahs(surahDao: QuranDao) {
        val surahs = loadSurahsJsonArray()
        try {
            for (i in 0 until surahs!!.length()) {
                val surah = surahs.getJSONObject(i)

                try {
                    Timber.d("insertion surah")
                    surahDao.insertSurah(
                        SurahDb(
                            surah.getInt(Constants.SURAH_NUMBER),
                            surah.getString(Constants.SURAH_NAME),
                            surah.getString(Constants.SURAH_TRANSLITERATION_EN),
                            surah.getString(Constants.SURAH_TRANSLATION_EN),
                            surah.getInt(Constants.SURAH_TOTAL_VERSES),
                            surah.getString(Constants.SURAH_REVELATION_TYPE)
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

    private suspend fun loadNames(surahDao: QuranDao) {
        val categories = loadJsonArray(resources, R.raw.category, Constants.CATEGORIES)

        try {
            for (i in 0 until categories!!.length()) {
                val category = categories.getJSONObject(i)

                try {
                    Timber.d("Db:insertion Categories")
                    surahDao.insertCategory(
                        Category(
                            0,
                            category.getString(Constants.CATEGORY_NAME)

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

        val chapters = loadJsonArray(resources, R.raw.chapter, Constants.CHAPTERS)

        try {
            for (i in 0 until chapters!!.length()) {
                val chapter = chapters.getJSONObject(i)

                try {
                    Timber.d("Db: insertion chapters")
                    surahDao.insertChapter(
                        Chapter(
                            0,
                            chapter.getString(Constants.CHAPTER_NAME),
                            chapter.getInt(Constants.CATEGORY_ID)
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

        val divineNames = loadJsonArray(resources, R.raw.noms, Constants.DIVINE_NAMES)
        try {
            for (i in 0 until divineNames!!.length()) {
                val divineName = divineNames.getJSONObject(i)

                try {
                    Timber.d("Db: insertion divine names")
                    surahDao.insertDivineName(
                        DivineName(
                            0,
                            divineName.getString(Constants.DIVINE_NAME)
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

    private suspend fun loadAyah(surahDao: QuranDao) {
        val ayahs = loadJsonArray(resources, R.raw.quran, Constants.AYAHS)

        try {
            for (i in 0 until ayahs!!.length()) {
                val ayah = ayahs.getJSONObject(i)

                try {
                    Timber.d("Db: insertion ayah")
                    surahDao.insertAyah(
                        AyahDb(
                            0,
                            ayah.getInt(Constants.AYAH_SURAH_NUMBER),
                            ayah.getInt(Constants.AYAH_NUMBER),
                            ayah.getString(Constants.AYAH_TEXT),
                            ayah.getString(Constants.AYAH_TRANSLATION)

                        )
                    )
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Timber.e("error:${e.message}")
                }

            }
            /*val intent = Intent(LoadingActivity.FILTER)
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)*/
        } catch (e: JSONException) {
            e.printStackTrace()
            Timber.e("error:${e.message}")
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
            return json.getJSONArray(Constants.SURAHS)
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