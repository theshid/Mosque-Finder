package com.shid.mosquefinder.app.utils.helper_class

object Constants {

    //
    const val MOSQUE_COLLECTION_PATH = "mosques"

    //Blog fields
    const val BLOG_COLLECTION_PATH = "blog"
    const val BLOG_FIELD_TITLE = "title"
    const val BLOG_FIELD_TITLE_FR = "title_fr"
    const val BLOG_FIELD_AUTHOR = "author"
    const val BLOG_FIELD_BODY = "body"
    const val BLOG_FIELD_IMAGE = "image_link"
    const val BLOG_FIELD_BODY_FR = "body_fr"
    const val BLOG_FIELD_TAG = "tag"

    //Beautiful Mosques
    const val B_MOSQUE_COLLECTION = "beautiful-mosques"
    const val B_MOSQUE_NAME = "name"
    const val B_MOSQUE_DESCRIPTION = "description"
    const val B_MOSQUE_LINK = "link"
    const val B_MOSQUE_PIC = "pic"
    const val B_MOSQUE_PIC2 = "pic2"
    const val B_MOSQUE_PIC3 = "pic3"
    const val B_MOSQUE_DESCRIPTION_FR = "description_fr"

    //Quotes Repository
    const val QUOTE_COLLECTION = "quotes"
    const val QUOTE_AUTHOR = "author"
    const val QUOTE_TEXT = "quote"
    const val QUOTE_TRANSLATION = "quote_fr"


    //Json fields
    const val SURAHS = "surahs"
    const val SURAH_NUMBER = "number"
    const val SURAH_NAME = "name"
    const val SURAH_TRANSLITERATION_EN = "transliteration_en"
    const val SURAH_TRANSLATION_EN = "translation_en"
    const val SURAH_TOTAL_VERSES = "total_verses"
    const val SURAH_REVELATION_TYPE = "revelation_type"

    const val CATEGORIES = "categories"
    const val CATEGORY_NAME = "category_name"
    const val CHAPTERS = "chapters"
    const val CHAPTER_NAME = "chapter_name"
    const val CATEGORY_ID = "category_id"
    const val DIVINE_NAMES = "noms"
    const val DIVINE_NAME = "name"
    const val AYAHS = "ayahs"
    const val AYAH_SURAH_NUMBER = "surah_number"
    const val AYAH_NUMBER = "verse_number"
    const val AYAH_TEXT = "text"
    const val AYAH_TRANSLATION = "translation"

    //Intent extras
    const val EXTRA_SURAH_NUMBER = "surah_number"
    const val EXTRA_SURAH_NAME = "surah"
    const val EXTRA_SURAH_LINK = "link"
    const val EXTRA_SURAH_NUMBER_AYAH = "number"
    const val EXTRA_SURAH_NAME_AYAH = "surah_name"
    const val EXTRA_STATE_PLAYER = "state_player"
    const val EXTRA_USER = "user"
    const val EXTRA_CHAPTER = "chapter"
    const val EXTRA_CATEGORY = "category"
    const val EXTRA_SEARCH = "search_result"


    //Intent keys
    const val EXTRA_KEY_MESSAGE = "message"

    //Bundles
    const val BUNDLE_KEY_DURATION = "duration"
    const val BUNDLE_KEY_CURRENT_POSITION = "current_position"
    const val BUNDLE_KEY_PLAYER_STATUS = "play_status"
    const val BUNDLE_KEY_PLAYER_SEEK = "seek"
    const val BUNDLE_KEY_PLAYER_REPEAT = "repeat"
    const val BUNDLE_KEY_TEXT = "text"
    const val BUNDLE_KEY_SEARCH = "test"
    const val BUNDLE_KEY_CONNECTION = "LOST_CONNECTION"

    //Intent Filters
    const val INTENT_FILTER_MESSAGE_RECEIVER = "MyData"

    //Surah ViewModel
    const val PRAYER_NOW = "Now "
    const val PRAYER_TIME = "It's time to pray "

    //AyahActivity
    const val SURAH_STATE = "state"

    //Workers
    const val WORKER_TAG = "notification"
    const val WORKER_NAME = "Daily Ayah"

    //Version
    const val FRENCH_VERSION = "fr"

    //ExoPlayer
    const val PLAYER_TYPE = "default"

    //Service
    const val QARI_MISHARY = "Mishary bin Rashid Alafasy"

    //Player Command
    const val COMMAND_SEEK = "seek"
    const val COMMAND_REPEAT = "repeat"
    const val COMMAND_SURAH = "surah"
    const val EVENT_MEDIA_INFORMATION = "player_information"
    const val EVENT_PLAYER_PAUSE = "play_pause"
    const val EVENT_PLAYER_FINISH = "finish"

    //SharedPreferences
    const val PREF_FILENAME = "filename"
    const val PREF_POSITION_LAT = "position_lat"
    const val PREF_POSITION_LONG = "position_lon"
    const val PREF_USE_COUNT = "use_count"
    const val PREF_RATE = "rate"
    const val PREF_FIRST_TIME = "first_time"
    const val PREF_FIRST_TIME_AYAH = "first_time_ayah"
    const val PREF_FIRST_TIME_PRAYER = "first_time_prayer"
    const val PREF_ACTIVATE = "activate"
    const val PREF_FIRST_TIME_BIS = "first_time_bis"
    const val PREF_USER = "user"
    const val PREF_TOKEN = "token"
    const val PREF_REMINDER = "reminder"
    const val PREF_APP_STATE_KEY = "APP_STATE"

    const val notificationId = 77
    const val ACTION_RETRY = "ACTION_RETRY"


    //Date
    const val HOUR_PATTERN = "HH:mm"
    const val DATE_PATTERN = "EEE, MMM d, yyyy"

    //text
    const val EMAIL = "mosquefinder@gmail.com"
    const val MESSAGE_TYPE = "message/rfc822"


    //MOSQUE INPUT
    const val MOSQUE_NAME = "name"
    const val MOSQUE_POSITION = "position"
    const val MOSQUE_DOC_ID = "documentId"
    const val MOSQUE_REPORT = "report"


}