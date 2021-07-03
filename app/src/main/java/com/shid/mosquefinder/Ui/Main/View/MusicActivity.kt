package com.shid.mosquefinder.Ui.Main.View

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.services.MusicService
import com.shid.mosquefinder.Utils.formatTimeInMillisToString
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import kotlinx.android.synthetic.main.activity_music.*
import java.io.File

class MusicActivity : AppCompatActivity() {

    private lateinit var playPauseButton: AppCompatImageView
    private lateinit var seekBar: AppCompatSeekBar
    private lateinit var mMediaBrowserCompat: MediaBrowserCompat
    private var mediaController: MediaControllerCompat? = null
    private lateinit var btn: Button
    private lateinit var repeatBtn: AppCompatImageView
    private lateinit var txtTotal: TextView
    private lateinit var txtProgress: TextView
    private var isRepeat = false
    private var statePlayer:Boolean = false
    private var surahName:String ?= null
    private var surahNumber:Int ?= null

    private val mp4Url =
        "https://media.blubrry.com/muslim_central_quran/podcasts.qurancentral.com/mishary-rashid-alafasy/mishary-rashid-alafasy-001-muslimcentral.com.mp3"
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
                Log.d("onConnected", "Controller Connected")
            }

            override fun onConnectionFailed() {
                super.onConnectionFailed()
                Log.d("onConnectionFailed", "Connection Failed")

            }


        }
    private val mControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onSessionEvent(event: String, extras: Bundle) {
            if (event == "player_information") {
                /*Log.d("Test", "value:" + formatTimeInMillisToString(extras.getLong("me")))
                Log.d("Test", "valuemiufg:" + formatTimeInMillisToString(extras.getLong("m")))*/
                val currentPosition = extras.getLong("current_position")
                val duration = extras.getLong("duration")
                seekBar.max = duration.toInt()
                _time.postValue(currentPosition)
                txtTotal.text = formatTimeInMillisToString(duration)
                txtProgress.text = formatTimeInMillisToString(currentPosition)

            } else if (event == "finish") {

                playPauseButton.setImageResource(R.drawable.ic_play_vector)
                val intent = Intent(this@MusicActivity,MusicService::class.java)
                stopService(intent)

            } else if (event == "play_pause"){
                val status = extras.getBoolean("play_status")
                if (status){
                    playPauseButton.setImageResource(R.drawable.ic_pause_vector)
                } else{
                    playPauseButton.setImageResource(R.drawable.ic_play_vector)
                }
            }
        }


    }

    fun playSurah() {
        mediaController = MediaControllerCompat.getMediaController(this@MusicActivity)


            val state = mediaController!!.playbackState.state

            // if it is not playing then what are you waiting for ? PLAY !
            if (state == PlaybackStateCompat.STATE_PAUSED || state == PlaybackStateCompat.STATE_STOPPED ||
                state == PlaybackStateCompat.STATE_NONE
            ) {
                var formatNumber:String ?= null
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
                if (checkIfFileExist()){
                    val filePath = this.getExternalFilesDir(null).toString()+"/surahs/$surahNumber-$surahName.mp3"
                    mediaController!!.transportControls.playFromUri(Uri.fromFile(File(filePath)),null)
                    Toast.makeText(this,"Reading File",Toast.LENGTH_LONG).show()
                }else{
                    mediaController!!.transportControls.playFromUri(Uri.parse(surahUrl), null)
                }
                playPauseButton.setImageResource(R.drawable.ic_pause_vector)
                song_player_progress_bar.visibility = View.GONE

            }
            // you are playing ? knock it off !
            else if (state == PlaybackStateCompat.STATE_PLAYING ||

                state == PlaybackStateCompat.STATE_CONNECTING
            ) {
                mediaController!!.transportControls.pause()
                playPauseButton.setImageResource(R.drawable.ic_play_vector)
                song_player_progress_bar.visibility = View.GONE

            } else if (state == PlaybackStateCompat.STATE_BUFFERING ){
                song_player_progress_bar.visibility = View.VISIBLE
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
        surahName = intent.getStringExtra("surah_name")
        surahNumber = intent.getIntExtra("surah_number",1)
        setUI()
        setMediaBrowser()

        statePlayer = intent.getBooleanExtra("state_player",false)
        Log.d("Test", "value of state:$statePlayer")
        setPlayPauseBtnFromIntent(statePlayer)
        setClickListeners()
        trackTimeObserver()


    }

    private fun sendSurahToService(){
        val b = Bundle()
        b.putString("surah",surahName)
        mediaController?.sendCommand("surah",b,null)
    }

    private fun trackTimeObserver() {
        time.observe(this@MusicActivity, Observer {
            seekBar.progress = it.toInt()
        })
    }

    private fun setClickListeners() {
        repeatBtn.setOnClickListener(View.OnClickListener {
            if(isRepeat){
                repeatBtn.setImageResource(R.drawable.ic_repeat_black_vector)
                isRepeat = false
                val b = Bundle()
                b.putBoolean("repeat",isRepeat)
                mediaController?.sendCommand("repeat",b,null)
            }else{
                repeatBtn.setImageResource(R.drawable.ic_repeat_one_color_primary_vector)
                isRepeat = true
                val b = Bundle()
                b.putBoolean("repeat",isRepeat)
                mediaController?.sendCommand("repeat",b,null)
            }
        })

        btn_back.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
    }

    private fun setPlayPauseBtnFromIntent(statePlayer: Boolean) {
        if (statePlayer){
            playPauseButton.setImageResource(R.drawable.ic_pause_vector)
        } else if (!statePlayer){
            playPauseButton.setImageResource(R.drawable.ic_play_vector)
        }
    }

    private fun checkIfFileExist():Boolean{
        val fileName = "$surahNumber-$surahName.mp3"
        val file = File(this.getExternalFilesDir(null).toString()+"/surahs/"+fileName)
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


    private fun setUI(){
        seekBar = findViewById(R.id.song_player_progress_seek_bar)
        playPauseButton = findViewById(R.id.song_player_toggle_image_view)
        txtTotal = findViewById(R.id.song_player_total_time_text_view)
        txtProgress = findViewById(R.id.song_player_passed_time_text_view)
        repeatBtn = findViewById(R.id.song_player_repeat_image_view)

        song_player_singer_name_text_view.text = "Mishary bin Rashid Alafasy"
        song_player_title_text_view.text = surahName

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val b = Bundle()
                    b.putLong("seek", progress.toLong())
                    mediaController?.sendCommand("seek", b, null)

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