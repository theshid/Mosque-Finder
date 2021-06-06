package com.shid.mosquefinder.Ui.Main.View

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
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
import com.shid.mosquefinder.Ui.services.SurahDLService
import com.shid.mosquefinder.Utils.Network.Event
import com.shid.mosquefinder.Utils.Network.NetworkEvents
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import io.ghyeok.stickyswitch.widget.StickySwitch
import kotlinx.android.synthetic.main.activity_ayah.*
import org.jetbrains.annotations.NotNull
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
    private var surahName: String? = null
    private var surahNumber: Int? = null
    private lateinit var switch: StickySwitch
    companion object{
        val STATE_SURAH = "state"
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ayah)
        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }



        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            with(savedInstanceState) {
                // Restore value of members from saved state
                surahNumber = getInt(STATE_SURAH)

            }
        } else {
            // Probably initialize members with default values for a new instance
            surahNumber = intent.getIntExtra("surah_number", 1)
        }


        setViewModel()
        setUI(surahNumber!!)


        setNetworkMonitor()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.run {
            surahNumber?.let { putInt(STATE_SURAH, it) }

        }

        super.onSaveInstanceState(outState, outPersistentState)
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore state members from saved instance
        savedInstanceState?.run {
            surahNumber = getInt(STATE_SURAH)

        }

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
            surahName = it.transliteration
            verse_number.text = it.totalVerses.toString() + " " + "Ayah"
        })

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

        txt_play_all.setOnClickListener(View.OnClickListener {
            sendDataToPlayer()
        })

        play_all.setOnClickListener(View.OnClickListener {
            sendDataToPlayer()
        })

        fab.setOnClickListener(View.OnClickListener {
            if (checkIfFileExist()){
                Toast.makeText(this,getString(R.string.dl_available),Toast.LENGTH_LONG).show()
            } else{
                downloadDialog()
            }

        })

    }

    private fun checkIfFileExist():Boolean{
        val fileName = "$surahNumber-$surahName.mp3"
        val file = File(this.getExternalFilesDir(null).toString()+"/surahs/"+fileName)
        //Log.d("Test",this.getExternalFilesDir(null).toString())
        return file.exists()
    }

    private fun downloadDialog() {
        MaterialDialog(this).show {
            title(text = getString(R.string.title_dialog))
            message(text = getString(R.string.dialog_rate_app))
            positiveButton(text = getString(R.string.rate)) { dialog ->
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
        val intent = Intent(this, SurahDLService::class.java)
        intent.putExtra("link", surahUrl)
        intent.putExtra("number", surahNumber)
        intent.putExtra("surah", surahName)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
            Toast.makeText(this, getString(R.string.dl_started), Toast.LENGTH_LONG).show()
        } else {
            startService(intent)
            Toast.makeText(this, getString(R.string.dl_started), Toast.LENGTH_LONG).show()
        }
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
        val intent = Intent(this, MusicActivity::class.java)
        intent.putExtra("surah_name", surahName)
        intent.putExtra("surah_number", surahNumber)
        startActivity(intent)

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
        switch.visibility = View.VISIBLE
        switch.setDirection(StickySwitch.Direction.RIGHT)

        if (simpleExoplayer.isPlaying) {
            simpleExoplayer.stop()
            initializePlayer(verseNumber)
        } else {
            initializePlayer(verseNumber)
        }

        if (!ConnectivityStateHolder.isConnected && previousSate) {
            // showSnackBar(textView, "No Network !")
            // Toast.makeText(this,getString(R.string.internet_ayah),Toast.LENGTH_LONG).show()
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

        val link = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/$ayaNum.mp3"
        val linkSurah = ""

        preparePlayer(link, "default")
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
        else if (playbackState == Player.STATE_READY)
            progressBar.visibility = View.INVISIBLE
        else if (playbackState == Player.STATE_ENDED) {
            progressBar.visibility = View.INVISIBLE
            exoplayerView.visibility = View.GONE
            switch.visibility = View.GONE
        }

    }
}