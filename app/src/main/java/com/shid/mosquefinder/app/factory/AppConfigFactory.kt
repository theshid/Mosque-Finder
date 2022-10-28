package com.shid.mosquefinder.app.factory





import com.shid.mosquefinder.BuildConfig
import com.shid.mosquefinder.app.utils.extensions.toUri
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigFactory @Inject constructor() {

    private val instance by lazy {
        AppConfig(
            databaseName = BuildConfig.DB_NAME,
            databaseCacheSize = BuildConfig.DB_CACHE_SIZE,
            workerStateStoreName = BuildConfig.WORKER_STATE_STORE_NAME,
            hostName = BuildConfig.HOST_NAME.toUri(),
            dateFormat = DateTimeFormatter.ofPattern(BuildConfig.DATE_FORMAT)
        )
    }

    fun create(): AppConfig = instance

}