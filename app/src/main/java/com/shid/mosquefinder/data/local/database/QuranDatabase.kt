package com.shid.mosquefinder.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shid.mosquefinder.data.local.database.entities.*

@Database(entities = [SurahDb::class, Ayah::class, Category::class, Chapter::class, DivineName::class,
                     Item::class], version = 3,exportSchema = true)
abstract class QuranDatabase: RoomDatabase() {

    abstract fun surahDao(): QuranDao
    abstract fun azkharDao(): AzkharDao

    companion object {

        const val DATABASE_NAME: String = "quran_database"
       /* @Volatile
        private var INSTANCE: QuranDatabase? = null

        fun getDatabase(
            context: Context,
            coroutineScope: CoroutineScope,
            resources: Resources
        ): QuranDatabase {
            val MIGRATION_1_2 = object : Migration(1, 2){
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE ayahs ADD COLUMN french_text TEXT" )
                }
            }

            val MIGRATION_2_3 = object : Migration(2, 3) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE `category` (`id` INTEGER NOT NULL," +
                            " `category_name` TEXT NOT NULL, " +
                            "PRIMARY KEY(`id`))");
                    database.execSQL("CREATE TABLE `chapter` (`id` INTEGER NOT NULL," +
                            " `chapter_name` TEXT NOT NULL, " +
                            " `category_id` INTEGER NOT NULL, PRIMARY KEY(`id`))");
                    database.execSQL("CREATE TABLE `item` (`id` INTEGER NOT NULL, " +
                            "`item_translation` TEXT NOT NULL, " +
                            "`chapter_id` INTEGER NOT NULL, PRIMARY KEY(`id`))");
                    database.execSQL("CREATE TABLE `noms` (`id` INTEGER NOT NULL," +
                            " `name` TEXT NOT NULL, " +
                            "PRIMARY KEY(`id`))")
                }
            }
            Timber.d("inside DB")

            //context.sendBroadcast(intent)
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
                    .addCallback(QuranDatabaseCallback(coroutineScope, resources,context))
                    .addMigrations(MIGRATION_1_2,MIGRATION_2_3)
                    .setJournalMode(JournalMode.AUTOMATIC)
                    .build()
                INSTANCE = instance
                return instance
            }
        }*/
    }



   /* private class QuranDatabaseCallback(
        private val scope: CoroutineScope,
        private val resources: Resources,
        private val mContext:Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    val surahDao = database.surahDao()
                    //val azkharDao = database.azkharDao()
                    fillWithStartingData(surahDao,mContext)

                }
            }
        }


        private suspend fun fillWithStartingData(surahDao: QuranDao,context: Context) {
            GlobalScope.launch(Dispatchers.IO){
                val surahs = loadSurahsJsonArray()
                try {
                    for (i in 0 until surahs!!.length()) {
                        val surah = surahs.getJSONObject(i)

                        try {
                            Log.d("Db","insertion surah")
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

            GlobalScope.launch(Dispatchers.IO){
                val categories = loadJsonArray(resources,R.raw.category,"categories")

                try {
                    for (i in 0 until categories!!.length()) {
                        val category = categories.getJSONObject(i)

                        try {
                            Timber.d("Db:insertion Categories")
                            surahDao.insertCategory(
                                Category(0,
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

                val chapters = loadJsonArray(resources,R.raw.chapter,"chapters")

                try {
                    for (i in 0 until chapters!!.length()) {
                        val chapter = chapters.getJSONObject(i)

                        try {
                            Timber.d("Db: insertion chapters")
                            surahDao.insertChapter(
                                Chapter(0,
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

                val divineNames = loadJsonArray(resources,R.raw.noms,"noms")
                try {
                    for (i in 0 until divineNames!!.length()) {
                        val divineName = divineNames.getJSONObject(i)

                        try {
                            Timber.d("Db: insertion divine names")
                            surahDao.insertDivineName(
                                DivineName(0,
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

            GlobalScope.launch(Dispatchers.IO){
                val ayahs = loadJsonArray(resources,R.raw.quran,"ayahs")

                try {
                    for (i in 0 until ayahs!!.length()) {
                        val ayah = ayahs.getJSONObject(i)

                        try {
                            Timber.d("Db: insertion ayah")
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

    }*/



}