package com.shid.mosquefinder.app.ui.main.views

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder
import com.elconfidencial.bubbleshowcase.BubbleShowCaseSequence
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.main.adapters.AyahAdapter
import com.shid.mosquefinder.app.ui.main.states.AyahViewState
import com.shid.mosquefinder.app.ui.main.states.SurahViewState
import com.shid.mosquefinder.app.ui.main.view_models.AyahViewModel
import com.shid.mosquefinder.app.ui.models.AyahPresentation
import com.shid.mosquefinder.app.ui.models.SurahPresentation
import com.shid.mosquefinder.app.ui.services.SurahDLService
import com.shid.mosquefinder.app.utils.extensions.showToast
import com.shid.mosquefinder.app.utils.extensions.startActivity
import com.shid.mosquefinder.app.utils.extensions.startService
import com.shid.mosquefinder.app.utils.helper_class.Constants
import com.shid.mosquefinder.app.utils.helper_class.SharePref
import com.shid.mosquefinder.app.utils.hide
import com.shid.mosquefinder.app.utils.network.ConnectivityStateHolder
import com.shid.mosquefinder.app.utils.show
import com.shid.mosquefinder.app.utils.showSnackbar
import com.shid.mosquefinder.data.local.database.entities.SurahDb
import com.shid.mosquefinder.data.model.pojo.VerseResponse
import dagger.hilt.android.AndroidEntryPoint
import io.ghyeok.stickyswitch.widget.StickySwitch
import kotlinx.android.synthetic.main.activity_ayah.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AyahActivity : BaseActivity(), AyahAdapter.OnClickAyah, Player.EventListener {
    private val viewModel: AyahViewModel by viewModels()
    private var surahDbList: List<SurahDb>? = null
    private lateinit var ayahAdapter: AyahAdapter
    private lateinit var simpleExoplayer: SimpleExoPlayer
    private var playbackPosition: Long = 0
    private var ayahNumber = 1
    private var baseNumber = 0
    private var surahName: String? = null
    private var surahNumber: Int? = null

    @Inject
    lateinit var sharedPref: SharePref
    private var isFirstTime: Boolean? = null
    private lateinit var switch: StickySwitch
    private var verseResponseList: List<VerseResponse>? = null


    private val STATE_SURAH = Constants.SURAH_STATE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ayah)

        isFirstTime = sharedPref.loadFirstTimeAyah()

        if (savedInstanceState != null) {
            with(savedInstanceState) {
                // Restore value of members from saved state
                surahNumber = getInt(STATE_SURAH)
            }
        } else {
            surahNumber = intent.getIntExtra(Constants.EXTRA_SURAH_NUMBER, 1)
        }

        setUI(surahNumber!!)
        setViewStates(surahNumber!!)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            surahNumber?.let { putInt(STATE_SURAH, it) }
            Timber.d("saved surah number to saveInstance")

        }
        super.onSaveInstanceState(outState)
    }


    private fun activateShowcase() {
        BubbleShowCaseSequence()
            .addShowCase(
                BubbleShowCaseBuilder(this) //Activity instance
                    .title(getString(R.string.bubble_aya_title1)) //Any title for the bubble view
                    .targetView(play_all) //View to point out
                    .description(getString(R.string.bubble_ayah_des1))
                    .backgroundColorResourceId(R.color.colorPrimary)
                    .imageResourceId(R.drawable.logo2)
                    .textColorResourceId(R.color.colorWhite)
            ) //First BubbleShowCase to show
            .addShowCase(
                BubbleShowCaseBuilder(this) //Activity instance
                    .title(getString(R.string.bubble_ayah_title3)) //Any title for the bubble view
                    .targetView(fab) //View to point out
                    .description(getString(R.string.bubble_ayah_desc3))
                    .backgroundColorResourceId(R.color.colorPrimary)
                    .imageResourceId(R.drawable.logo2)
                    .textColorResourceId(R.color.colorWhite)
            )
            .show() //Display the ShowCaseSequence
    }

    private fun setViewStates(number_surah: Int) {
        viewModel.surahByNumberViewState.observe(this) { state ->
            state.surah?.let {
                surah_title.text = it.transliteration
                surahName = it.transliteration
                verse_number.text = it.totalVerses.toString() + " " + "Ayah"
            }
        }

        viewModel.ayahViewState.observe(this) { state ->
            handleAyahLoading(state)
            state.ayahs?.let { list ->
                if (list.isNotEmpty()) {
                    ayahAdapter.setData(list)
                    if (Locale.getDefault().language.contentEquals(Constants.FRENCH_VERSION)) {
                        if (list[0].frenchTranslation == null || list[0].frenchTranslation == "empty"
                        ) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                viewModel.getFrenchSurah(number_surah)
                                delay(2000)
                                viewModel.translation.observe(this@AyahActivity, Observer {
                                    if (it.isNotEmpty()) {
                                        ayahAdapter.setFrenchAyahList(it)
                                    }
                                })
                            }

                        }
                    }
                }

            }
            handleAyahError(state)
        }

        viewModel.surahsViewState.observe(this) { state ->
            handleSurahLoading(state)
            state.surahs?.let { list ->
                if (list.isNotEmpty()) {
                    calculateBase(list)
                }
            }
            handleSurahError(state)
        }
    }

    private fun handleAyahError(state: AyahViewState) {
        state.error?.run {
            showSnackbar(ayahRecycler, getString(this.message), isError = true)
        }
    }

    private fun handleAyahLoading(state: AyahViewState) {
        if (state.isLoading) {
            progressBar.show()
        } else {
            progressBar.hide()
        }
    }

    private fun handleSurahError(state: SurahViewState) {
        state.error?.run {
            showSnackbar(
                ayahRecycler, getString(this.message), isError = true
            )
        }
    }

    private fun handleSurahLoading(surahViewState: SurahViewState) {
        if (surahViewState.isLoading) {
            progressBar.show()
        } else {
            progressBar.hide()
        }
    }

    private fun setViewModelData(number_surah: Int) {
        viewModel.getAyahs(number_surah)
        viewModel.getSurahs()
        viewModel.getSurahByNumber(number_surah)
    }


    private fun setUI(number_surah: Int) {
        if (isFirstTime == true) {
            activateShowcase()
            sharedPref.setFirstTimeAyah(false)
        }
        ayahAdapter = AyahAdapter(viewModel)
        ayahRecycler.adapter = ayahAdapter
        ayahAdapter.setOnItemClick(this@AyahActivity)

        setViewModelData(number_surah)


        /*viewModel.listSurahDb.observe(this, Observer {
            surahDbList = it
            Log.d("Test", it.size.toString())
            if (it.isNotEmpty()) {
                calculateBase()
            }

        })*/
        /*viewModel.ayah.observe(this, Observer { ayahDbList ->
            if (ayahDbList.isNotEmpty()) {
                if (Locale.getDefault().language.contentEquals(Constants.FRENCH_VERSION)) {
                    if (ayahDbList[0].frenchTranslation == null || ayahDbList[0].frenchTranslation.equals(
                            "empty"
                        )
                    ) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            viewModel.getFrenchSurah(number_surah)
                            delay(2000)
                            viewModel.translation.observe(this@AyahActivity, Observer {
                                if (it.isNotEmpty()) {
                                    ayahAdapter.setFrenchAyahList(it)
                                }
                            })
                            *//*ayahRecycler.adapter = ayahAdapter
                            ayahAdapter.setData(ayahDbList)
                            ayahAdapter.setOnItemClick(this@AyahActivity)*//*
                        }

                    }
                }

                //ayahAdapter.setData(ayahDbList)

            }

        })*/

        /*viewModel.surah.observe(this, Observer {
            if (it != null) {
                surah_title.text = it.transliteration
                surahName = it.transliteration
                verse_number.text = it.totalVerses.toString() + " " + "Ayah"
            }

        })*/



        switch = findViewById(R.id.switch_control)
        switch.visibility = View.GONE
        switch.onSelectedChangeListener = object : StickySwitch.OnSelectedChangeListener {
            override fun onSelectedChange(direction: StickySwitch.Direction, text: String) {
                Log.d("TAG", "Now Selected : " + direction.name + ", Current Text : " + text);
                if (direction == StickySwitch.Direction.LEFT) {
                    simpleExoplayer.pause()
                } else {
                    simpleExoplayer.play()
                }
            }

        }

        setClickListeners()

    }

    private fun setClickListeners() {
        txt_play_all.setOnClickListener {
            sendDataToPlayer()
        }

        play_all.setOnClickListener {
            sendDataToPlayer()
        }

        fab.setOnClickListener {
            if (checkIfFileExist()) {
                showToast(getString(R.string.dl_available))
            } else {
                downloadDialog()
            }

        }

        btn_back.setOnClickListener {
            onBackPressed()
        }
    }

    private fun checkIfFileExist(): Boolean {
        val fileName = "$surahNumber-$surahName.mp3"
        val file = File(this.getExternalFilesDir(null).toString() + "/surahs/" + fileName)
        //Log.d("Test",this.getExternalFilesDir(null).toString())
        return file.exists()
    }

    private fun downloadDialog() {
        MaterialDialog(this).show {
            title(text = getString(R.string.title_dialog))
            message(text = getString(R.string.surah_dl))
            positiveButton(text = getString(R.string.yes)) { dialog ->
                dialog.cancel()
                initializeService()
            }
            negativeButton(text = getString(R.string.cancel)) { dialog ->
                dialog.cancel()

            }
            icon(R.drawable.logo2)
        }
    }

    private fun initializeService() {
        val number = formatSurahNumber()
        val surahUrl =
            "https://media.blubrry.com/muslim_central_quran/podcasts.qurancentral.com/mishary" +
                    "-rashid-alafasy/mishary-rashid-alafasy-$number-muslimcentral.com.mp3"
        startService<SurahDLService> {
            putExtra(Constants.EXTRA_SURAH_NAME, surahName)
            putExtra(Constants.EXTRA_SURAH_LINK, surahUrl)
            putExtra(Constants.EXTRA_SURAH_NUMBER_AYAH, surahNumber)
        }
        showToast(getString(R.string.dl_started))
        /*val intent = Intent(this, SurahDLService::class.java)
        intent.putExtra("link", surahUrl)
        intent.putExtra("number", surahNumber)
        intent.putExtra("surah", surahName)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
            Toast.makeText(this, getString(R.string.dl_started), Toast.LENGTH_LONG).show()
        } else {
            startService(intent)
            Toast.makeText(this, getString(R.string.dl_started), Toast.LENGTH_LONG).show()
        }*/
    }

    private fun formatSurahNumber(): String {
        var formatNumber: String? = null
        formatNumber = when {
            surahNumber!! in 1..9 -> {
                "00$surahNumber"
            }
            surahNumber!! in 10..99 -> {
                "0$surahNumber"
            }
            else -> {
                surahNumber.toString()
            }
        }
        return formatNumber
    }

    private fun sendDataToPlayer() {
        startActivity<MusicActivity> {
            putExtra(Constants.EXTRA_SURAH_NAME_AYAH, surahName)
            putExtra(Constants.EXTRA_SURAH_NUMBER, surahNumber)
        }
    }

    private fun calculateBase(surahList: List<SurahPresentation>) {
        for (item in surahList) {
            baseNumber += item.totalVerses
        }
    }

    private fun initializeExoPlayer(ayahNumber: Int) {
        if (simpleExoplayer.isPlaying) {
            simpleExoplayer.stop()
            initializePlayer(ayahNumber)
        } else {
            initializePlayer(ayahNumber)
        }
    }

    override fun onClickAyah(ayah: AyahPresentation) {

        val verseNumber = baseNumber + ayah.verse_number
        Timber.d("ayaNum:$verseNumber")
        switch.visibility = View.VISIBLE
        switch.setDirection(StickySwitch.Direction.RIGHT)

        initializeExoPlayer(verseNumber)

        if (!ConnectivityStateHolder.isConnected && previousSate) {
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_disconnected))
                .setMessage(getString(R.string.internet_ayah))
                .sneakError()

            exoplayerView.visibility = View.GONE
        }

    }

    override fun onStart() {
        super.onStart()
        simpleExoplayer = SimpleExoPlayer.Builder(this).build()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initializePlayer(ayaNum: Int) {

        val link = "http://cdn.islamic.network/quran/audio/128/ar.alafasy/$ayaNum.mp3"
        preparePlayer(link, Constants.PLAYER_TYPE)
        exoplayerView.player = simpleExoplayer
        //exoplayerView.visibility = View.VISIBLE
        exoplayerView.controllerShowTimeoutMs = 0;
        exoplayerView.controllerHideOnTouch = false;
        simpleExoplayer.playWhenReady = true
        simpleExoplayer.addListener(this)
    }


    private fun buildMediaItem(uri: Uri, type: String): MediaItem {
        return MediaItem.fromUri(uri)
    }

    private fun preparePlayer(ayahUrl: String, type: String) {
        val uri = Uri.parse(ayahUrl)
        Timber.d("link:$ayahUrl")
        /* val evictor = LeastRecentlyUsedCacheEvictor((100 * 1024 * 1024).toLong())
         val databaseProvider: DatabaseProvider = ExoDatabaseProvider(this)

         val simpleCache = SimpleCache(File(this.cacheDir, "media"), evictor, databaseProvider)


         val mediaSource = ProgressiveMediaSource.Factory(
             simpleCache?.let {
                 CacheDataSource.Factory().setCache(it)
             }
         )
             .createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)))*/
        val mediaItem = buildMediaItem(uri, type)
        //simpleExoplayer.setMediaSource(mediaSource)
        simpleExoplayer.setMediaItem(mediaItem)
        simpleExoplayer.prepare()

    }

    private fun releasePlayer() {
        playbackPosition = simpleExoplayer.currentPosition
        simpleExoplayer.release()
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        // handle error
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING)
            progressBar.visibility = View.VISIBLE
        else if (playbackState == Player.STATE_READY)
            progressBar.visibility = View.INVISIBLE
        else if (playbackState == Player.STATE_ENDED) {
            progressBar.visibility = View.INVISIBLE
            exoplayerView.visibility = View.GONE
            switch.visibility = View.GONE
        }

    }
}