package com.shid.mosquefinder.Ui.Main.View

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.ConnectivityStateHolder
import com.shid.mosquefinder.Data.database.entities.Ayah
import com.shid.mosquefinder.Data.database.entities.Surah
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.AyahViewModelFactory
import com.shid.mosquefinder.Ui.Main.Adapter.AyahAdapter
import com.shid.mosquefinder.Ui.Main.ViewModel.AyahViewModel
import com.shid.mosquefinder.Utils.Network.Event
import com.shid.mosquefinder.Utils.Network.NetworkEvents
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import kotlinx.android.synthetic.main.activity_ayah.*
import java.io.File

class AyahActivity : AppCompatActivity(), AyahAdapter.OnClickAyah, Player.EventListener {
    private lateinit var viewModel: AyahViewModel
    private var surahList: List<Surah>? = null
    private var previousSate = true
    private lateinit var ayahAdapter: AyahAdapter
    private lateinit var simpleExoplayer: SimpleExoPlayer
    private var playbackPosition: Long = 0
    private var ayahNumber = 1
    private var baseNumber = 0
    private val baseUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/$ayahNumber.mp3"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ayah)
        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }
        val surahNumber = intent.getIntExtra("surah_number", 1)

        setViewModel()
        setUI(surahNumber)

        setTransparentStatusBar()
        setNetworkMonitor()
    }


    private fun setViewModel() {
        viewModel = ViewModelProvider(
            this,
            AyahViewModelFactory(application)
        ).get(AyahViewModel::class.java)
    }

    private fun setUI(number_surah: Int) {
        ayahAdapter = AyahAdapter()
        viewModel.getSurahList(number_surah)
        viewModel.getAllAyah(number_surah)
        viewModel.getSurahInfo(number_surah)
        viewModel.listSurah.observe(this, Observer {
            surahList = it
            Log.d("Test", it.size.toString())
            if (it.isNotEmpty()) {
                calculateBase()
            }

        })
        viewModel.ayah.observe(this, Observer {
            if (it.isNotEmpty()) {
                ayahAdapter.setData(it)
                ayahAdapter.setOnItemClick(this)
                ayahRecycler.adapter = ayahAdapter
                ayahAdapter.notifyDataSetChanged()
            }

        })
        viewModel.surah.observe(this, Observer {
            surah_title.text = it.transliteration
            verse_number.text = it.totalVerses.toString() + " " + "Ayah"
        })


    }

    private fun calculateBase() {
        for (item in surahList!!) {
            baseNumber += item.totalVerses
        }
    }


    private fun setNetworkMonitor() {
        NetworkEvents.observe(this, androidx.lifecycle.Observer {

            if (it is Event.ConnectivityEvent)
                handleConnectivityChange()


        })
    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected && !previousSate) {
            // showSnackBar(textView, "The network is back !")
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_connected))
                .setMessage(getString(R.string.sneaker_msg_network))
                .sneakSuccess()
        }

        if (!ConnectivityStateHolder.isConnected && previousSate) {
            // showSnackBar(textView, "No Network !")
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_disconnected))
                .setMessage(getString(R.string.sneaker_msg_network_lost))
                .sneakError()
        }

        previousSate = ConnectivityStateHolder.isConnected
    }

    override fun onClickAyah(ayah: Ayah) {

        val verseNumber = baseNumber + ayah.verse_number
        Log.d("Test", "ayaNum:$verseNumber")
        initializePlayer(verseNumber)
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initializePlayer(ayaNum: Int) {
        simpleExoplayer = SimpleExoPlayer.Builder(this).build()
        val link = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/$ayaNum.mp3"
        preparePlayer(link, "default")
        exoplayerView.player = simpleExoplayer
        exoplayerView.visibility = View.VISIBLE
        exoplayerView.controllerShowTimeoutMs = 0;
        exoplayerView.controllerHideOnTouch = false;
        simpleExoplayer.playWhenReady = true
        simpleExoplayer.addListener(this)


    }



    private fun buildMediaItem(uri: Uri, type: String): MediaItem {
        return MediaItem.fromUri(uri)
    }

    private fun preparePlayer(videoUrl: String, type: String) {
        val uri = Uri.parse(videoUrl)
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
        else if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED)
            progressBar.visibility = View.INVISIBLE
        else if (playbackState == Player.STATE_ENDED)
            exoplayerView.visibility = View.GONE
    }
}