package com.shid.mosquefinder.app.ui.main.views

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.services.MusicService
import com.shid.mosquefinder.app.utils.extensions.showToast
import com.shid.mosquefinder.app.utils.formatTimeInMillisToString
import com.shid.mosquefinder.app.utils.helper_class.Constants.BUNDLE_KEY_CURRENT_POSITION
import com.shid.mosquefinder.app.utils.helper_class.Constants.BUNDLE_KEY_DURATION
import com.shid.mosquefinder.app.utils.helper_class.Constants.BUNDLE_KEY_PLAYER_REPEAT
import com.shid.mosquefinder.app.utils.helper_class.Constants.BUNDLE_KEY_PLAYER_SEEK
import com.shid.mosquefinder.app.utils.helper_class.Constants.BUNDLE_KEY_PLAYER_STATUS
import com.shid.mosquefinder.app.utils.helper_class.Constants.COMMAND_REPEAT
import com.shid.mosquefinder.app.utils.helper_class.Constants.COMMAND_SEEK
import com.shid.mosquefinder.app.utils.helper_class.Constants.COMMAND_SURAH
import com.shid.mosquefinder.app.utils.helper_class.Constants.EVENT_MEDIA_INFORMATION
import com.shid.mosquefinder.app.utils.helper_class.Constants.EVENT_PLAYER_FINISH
import com.shid.mosquefinder.app.utils.helper_class.Constants.EVENT_PLAYER_PAUSE
import com.shid.mosquefinder.app.utils.helper_class.Constants.EXTRA_STATE_PLAYER
import com.shid.mosquefinder.app.utils.helper_class.Constants.EXTRA_SURAH_NAME
import com.shid.mosquefinder.app.utils.helper_class.Constants.EXTRA_SURAH_NAME_AYAH
import com.shid.mosquefinder.app.utils.helper_class.Constants.EXTRA_SURAH_NUMBER
import com.shid.mosquefinder.app.utils.helper_class.Constants.QARI_MISHARY
import com.shid.mosquefinder.app.utils.remove
import kotlinx.android.synthetic.main.activity_music.*
import timber.log.Timber
import java.io.File

class MusicActivity : BaseActivity() {

    private lateinit var playPauseButton: AppCompatImageView
    private lateinit var seekBar: AppCompatSeekBar
    private lateinit var mMediaBrowserCompat: MediaBrowserCompat
    private lateinit var repeatBtn: AppCompatImageView
    private lateinit var txtTotal: TextView
    private lateinit var txtProgress: TextView

    private var mediaController: MediaControllerCompat? = null
    private var isRepeat = false
    private var statePlayer: Boolean = false
    private var surahName: String? = null
    private var surahNumber: Int? = null

    var _time = MutableLiveData<Long>()
    val time: LiveData<Long>
        get() = _time
    private val connectionCallback: MediaBrowserCompat.ConnectionCallback =
        object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {

                // The browser connected to the session successfully, use the token to create the controller
                super.onConnected()
                mMediaBrowserCompat.sessionToken.also { token ->
                    val mediaController = MediaControllerCompat(this@MusicActivity, token)
                    MediaControllerCompat.setMediaController(this@MusicActivity, mediaController)
                }
                playSurah()
                playPauseBuild()
                Timber.d("Controller Connected")
            }

            override fun onConnectionFailed() {
                super.onConnectionFailed()
                Timber.d("Connection Failed")

            }


        }
    private val mControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onSessionEvent(event: String, extras: Bundle) {
            if (event == EVENT_MEDIA_INFORMATION) {
                val currentPosition = extras.getLong(BUNDLE_KEY_CURRENT_POSITION)
                val duration = extras.getLong(BUNDLE_KEY_DURATION)
                seekBar.max = duration.toInt()
                _time.postValue(currentPosition)
                txtTotal.text = formatTimeInMillisToString(duration)
                txtProgress.text = formatTimeInMillisToString(currentPosition)

            } else if (event == EVENT_PLAYER_FINISH) {
                playPauseButton.setImageResource(R.drawable.ic_play_vector)
                val intent = Intent(this@MusicActivity, MusicService::class.java)
                stopService(intent)

            } else if (event == EVENT_PLAYER_PAUSE) {
                val status = extras.getBoolean(BUNDLE_KEY_PLAYER_STATUS)
                if (status) {
                    playPauseButton.setImageResource(R.drawable.ic_pause_vector)
                } else {
                    playPauseButton.setImageResource(R.drawable.ic_play_vector)
                }
            }
        }


    }

    fun playSurah() {
        mediaController = MediaControllerCompat.getMediaController(this@MusicActivity)


        val state = mediaController!!.playbackState.state

        // if it is not playing then what are you waiting for ? PLAY !
        when (state) {
            PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.STATE_NONE -> {
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
                sendSurahToService()
                val surahUrl =
                    "https://media.blubrry.com/muslim_central_quran/podcasts.qurancentral.com" +
                            "/mishary-rashid-alafasy/mishary-rashid-alafasy-$formatNumber-muslimcentral.com.mp3"
                if (checkIfFileExist()) {
                    val filePath = this.getExternalFilesDir(null)
                        .toString() + "/surahs/$surahNumber-$surahName.mp3"
                    mediaController!!.transportControls.playFromUri(
                        Uri.fromFile(File(filePath)),
                        null
                    )
                    showToast(getString(R.string.read_file))
                } else {
                    mediaController!!.transportControls.playFromUri(Uri.parse(surahUrl), null)
                }
                playPauseButton.setImageResource(R.drawable.ic_pause_vector)
                song_player_progress_bar.remove()

            }
            // you are playing ? knock it off !
            PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_CONNECTING -> {
                mediaController!!.transportControls.pause()
                playPauseButton.setImageResource(R.drawable.ic_play_vector)
                song_player_progress_bar.remove()

            }
            PlaybackStateCompat.STATE_BUFFERING -> {
                song_player_progress_bar.show()
            }
        }

        mediaController!!.registerCallback(mControllerCallback)

    }

    fun playPauseBuild() {
        mediaController = MediaControllerCompat.getMediaController(this@MusicActivity)

        playPauseButton.setOnClickListener {
            playSurah()
        }

        mediaController!!.registerCallback(mControllerCallback)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)
        surahName = intent.getStringExtra(EXTRA_SURAH_NAME_AYAH)
        surahNumber = intent.getIntExtra(EXTRA_SURAH_NUMBER, 1)
        statePlayer = intent.getBooleanExtra(EXTRA_STATE_PLAYER, false)
        setUI()
        setMediaBrowser()
        setPlayPauseBtnFromIntent(statePlayer)
        setClickListeners()
        trackTimeObserver()
    }

    private fun sendSurahToService() {
        val b = Bundle()
        b.putString(EXTRA_SURAH_NAME, surahName)
        mediaController?.sendCommand(COMMAND_SURAH, b, null)
    }

    private fun trackTimeObserver() {
        time.observe(this@MusicActivity, Observer {
            seekBar.progress = it.toInt()
        })
    }

    private fun setClickListeners() {
        repeatBtn.setOnClickListener {
            if (isRepeat) {
                repeatBtn.setImageResource(R.drawable.ic_repeat_black_vector)
                isRepeat = false
                val b = Bundle()
                b.putBoolean(BUNDLE_KEY_PLAYER_REPEAT, isRepeat)
                mediaController?.sendCommand(COMMAND_REPEAT, b, null)
            } else {
                repeatBtn.setImageResource(R.drawable.ic_repeat_one_color_primary_vector)
                isRepeat = true
                val b = Bundle()
                b.putBoolean(BUNDLE_KEY_PLAYER_REPEAT, isRepeat)
                mediaController?.sendCommand(COMMAND_REPEAT, b, null)
            }
        }

        btn_back.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
    }

    private fun setPlayPauseBtnFromIntent(statePlayer: Boolean) {
        if (statePlayer) {
            playPauseButton.setImageResource(R.drawable.ic_pause_vector)
        } else if (!statePlayer) {
            playPauseButton.setImageResource(R.drawable.ic_play_vector)
        }
    }

    private fun checkIfFileExist(): Boolean {
        val fileName = "$surahNumber-$surahName.mp3"
        val file = File(this.getExternalFilesDir(null).toString() + "/surahs/" + fileName)
        //Log.d("Test",this.getExternalFilesDir(null).toString())
        return file.exists()
    }

    private fun setMediaBrowser() {
        val componentName = ComponentName(this, MusicService::class.java)
        // initialize the browser
        mMediaBrowserCompat = MediaBrowserCompat(
            this, componentName, //Identifier for the service
            connectionCallback,
            null
        )
    }


    private fun setUI() {
        seekBar = findViewById(R.id.song_player_progress_seek_bar)
        playPauseButton = findViewById(R.id.song_player_toggle_image_view)
        txtTotal = findViewById(R.id.song_player_total_time_text_view)
        txtProgress = findViewById(R.id.song_player_passed_time_text_view)
        repeatBtn = findViewById(R.id.song_player_repeat_image_view)

        song_player_singer_name_text_view.text = QARI_MISHARY
        song_player_title_text_view.text = surahName

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val b = Bundle()
                    b.putLong(BUNDLE_KEY_PLAYER_SEEK, progress.toLong())
                    mediaController?.sendCommand(COMMAND_SEEK, b, null)

                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // seekBar?.progress?.toLong()?.let { mediaController?.transportControls?.seekTo(it) }
            }

        })
    }

    override fun onStart() {
        super.onStart()
        // connect the controllers again to the session
        // without this connect() you won't be able to start the service neither control it with the controller
        mMediaBrowserCompat.connect()
        //bindMusicService()
    }

    override fun onStop() {
        super.onStop()
        // Release the resources
        disconnectMedia()
    }

    private fun disconnectMedia() {
        val controllerCompat = MediaControllerCompat.getMediaController(this)
        controllerCompat?.unregisterCallback(mControllerCallback)
        mMediaBrowserCompat.disconnect()
    }

}