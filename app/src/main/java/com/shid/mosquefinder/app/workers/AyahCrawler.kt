package com.shid.mosquefinder.app.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shid.mosquefinder.app.factory.BackgroundInfoFactory
import com.shid.mosquefinder.app.factory.ForegroundInfoFactory
import com.shid.mosquefinder.app.utils.extensions.requireBody
import com.shid.mosquefinder.app.utils.helper_class.SharePref
import com.shid.mosquefinder.data.api.QuranApiInterface
import com.shid.mosquefinder.domain.usecases.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class AyahCrawler @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val surahService: QuranApiInterface,
    private val getLastSurahIdUseCase: GetLastSurahIdUseCase,
    private val saveLastSurahIdUseCase: SaveLastSurahIdUseCase,
    private val getIgnoredSurahIdUseCase: GetIgnoredSurahIdUseCase,
    private val registerIgnoredSurahIdUseCase: RegisterIgnoredSurahIdUseCase,
    private val updateAyahUseCase: UpdateAyahUseCase,
    private val foregroundInfoFactory: ForegroundInfoFactory,
    private val backgroundInfoFactory: BackgroundInfoFactory,
    private val sharedPref: SharePref
) : CoroutineWorker(context, params) {

    private var newestSurahId: Long = 0
    private var nextId: Long = 0
    private var counter: Long = 0
    override suspend fun doWork(): Result = try {
        withContext(Dispatchers.IO) {
            newestSurahId = 114

            if (newestSurahId == -1L) {
                error("Could not get latest strip id! Will retry!")
            }

            nextId = getLastSurahIdUseCase.getLastSurahId() + 1L
            Timber.d("last id: $nextId ")
            val initialId = nextId
            Timber.w("Starting crawler with initalId=$initialId")

            while (true) {
                with(foregroundInfoFactory.create(id, counter)) {
                    setForeground(this)
                }
                val stripResponse = surahService.getFrenchSurah(nextId)
                if (stripResponse.isSuccessful && stripResponse.body() != null) {
                    val strip = stripResponse.requireBody()
                    val ayahs = strip.data.verseResponse
                    if (ayahs.isNotEmpty()) {
                        for (ayah in ayahs) {
                            updateAyahUseCase(Pair(ayah.trans, ayah.verseNumber.toLong()))

                            Timber.d("insert in DB: ")
                        }
                        advance()
                    } else {
                        Timber.w("Registering $nextId as ignored one - invalid uri")
                        registerIgnoredSurahIdUseCase.addIgnored(nextId)
                        advance(skipCounter = true)
                    }
                } else {
                    when {
                        nextId > newestSurahId -> {
                            Timber.w("No new surahs available!")
                            break
                        }
                        getIgnoredSurahIdUseCase.getIgnoredIds().contains(nextId) -> {
                            Timber.w("Ignoring $nextId - one of the known bad entries")
                            advance(skipCounter = true)
                        }
                        stripResponse.code() == 404 -> {
                            Timber.w("Registering $nextId as ignored one - code 404")
                            registerIgnoredSurahIdUseCase.addIgnored(nextId)
                            advance(skipCounter = true)
                        }
                        else -> error("Strip with number=$nextId does not exist")
                    }
                }
            }
            Result.success()
        }
    } catch (ex: Exception) {
        Timber.d("exception type:${ex}")
        if (sharedPref.getIsAppInBackground()) {
            backgroundInfoFactory.createNotificationForRetry()
        }
        Timber.w("Exception while searching for comics! Are you offline?", ex)
        Result.retry()
    }

    private suspend fun advance(skipCounter: Boolean = false) {
        saveLastSurahIdUseCase.saveLastSurahId(nextId)
        nextId += 1
        counter += if (skipCounter) 0 else 1
        Timber.w("Next Id to be searched = $nextId")
    }
}
