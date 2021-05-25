package com.shid.mosquefinder.Ui.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.shid.mosquefinder.Ui.Main.View.MusicActivity
import com.shid.mosquefinder.R

private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"

class MusicService : MediaBrowserServiceCompat(), Player.EventListener {


    var h: Handler? = null
    var isRepeat = false
    private var state_player: Boolean? = null
    private var mMediaSession: MediaSessionCompat? = null
    private lateinit var mStateBuilder: PlaybackStateCompat.Builder
    private lateinit var playerNotificationManager: PlayerNotificationManager
    private lateinit var context: Context
    private var notificationId = 123;
    private var channelId = "channelId"
    private var mExoPlayer: SimpleExoPlayer? = null
    private var oldUri: Uri? = null


    private val mMediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            uri?.let {
                // val mediaSource = extractMediaSourceFromUri(uri)
                val mediaItem = buildMediaItem(uri)
                if (uri != oldUri) {
                    play(mediaItem)
                    /*val b = Bundle()
                    b.putLong("yo", mExoPlayer!!.contentDuration)
                    mMediaSession?.sendSessionEvent("lol", b)*/

                } else play() // this song was paused so we don't need to reload it
                oldUri = uri
            }
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            super.onCommand(command, extras, cb)

            if (command == "seek") {
                extras?.let { mExoPlayer?.seekTo(it.getLong("seek")) }
            } else if (command == "repeat") {
                extras?.let { isRepeat = it.getBoolean("repeat") }
                if (isRepeat) {
                    mExoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
                } else {
                    mExoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
                }
            }
        }

        override fun onPause() {
            super.onPause()
            pause()
        }

        override fun onStop() {
            super.onStop()
            stop()
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        initializePlayer()
        //initializeExtractor()
        initializeAttributes()


        mMediaSession = MediaSessionCompat(baseContext, "tag for debugging").apply {
            // Enable callbacks from MediaButtons and TransportControls
            /*setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )*/
            // Set initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            mStateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)

            setPlaybackState(mStateBuilder.build())

            // methods that handle callbacks from a media controller
            setCallback(mMediaSessionCallback)

            // Set the session's token so that client activities can communicate with it
            setSessionToken(sessionToken)
            isActive = true
        }

        h = Handler(Looper.myLooper()!!)

    }

    private val runner = object : Runnable {
        override fun run() {
            val information = sendPositionAndDuration()
            mMediaSession?.sendSessionEvent("player_information", information)
            //Log.d("Test","Service"+mExoPlayer?.currentPosition)
            h?.postDelayed(this, 1000)
        }

    }

    private fun sendPositionAndDuration(): Bundle {
        val b = Bundle()
        b.putLong("current_position", mExoPlayer!!.currentPosition)
        b.putLong("duration", mExoPlayer!!.duration)
        return b
    }

    private fun startHandler() {
        h?.postDelayed(runner, 0)
    }

    private fun cancelHandler() {
        h?.removeCallbacks(runner)
    }

    private var mAttrs: AudioAttributes? = null

    private fun play(mediaItem: MediaItem) {
        if (mExoPlayer == null) initializePlayer()
        mExoPlayer?.apply {
            // AudioAttributes here from exoplayer package !!!
            mAttrs?.let { initializeAttributes() }
            // In 2.9.X you don't need to manually handle audio focus :D
            setAudioAttributes(mAttrs!!, true)
            setMediaItem(mediaItem)
            prepare()
            play()

        }
        startService(Intent(context, MusicService::class.java))
        initializeNotification(mExoPlayer!!)
        playerNotificationManager.setPlayer(mExoPlayer)

    }

    private fun initializeNotification(player: SimpleExoPlayer) {
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            this,
            channelId,
            R.string.channel_name,
            R.string.channel_desc,
            notificationId,
            object : PlayerNotificationManager.MediaDescriptionAdapter {


                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    // return pending intent
                    val intent = Intent(context, MusicActivity::class.java)
                    intent.putExtra("state_player", state_player)
                    return PendingIntent.getActivity(
                        context, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }

                //pass description here
                override fun getCurrentContentText(player: Player): String? {
                    return "Description"
                }

                //pass title (mostly playing audio name)
                override fun getCurrentContentTitle(player: Player): String {
                    return "Title"
                }

                // pass image as bitmap
                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? {
                    return null
                }
            },
            object : PlayerNotificationManager.NotificationListener {

                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    onGoing: Boolean
                ) {

                    startForeground(notificationId, notification)

                }

                override fun onNotificationCancelled(
                    notificationId: Int,
                    dismissedByUser: Boolean
                ) {
                    stopSelf()
                }

            }
        )
        //attach player to playerNotificationManager
        playerNotificationManager.setPlayer(player)

    }

    private fun play() {
        mExoPlayer?.apply {
            mExoPlayer?.playWhenReady = true
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
            mMediaSession?.isActive = true
        }
        startHandler()
    }

    private fun initializePlayer() {
        mExoPlayer = SimpleExoPlayer.Builder(this).build()
        mExoPlayer!!.addListener(this)
        // mExoPlayer!!.setThrowsWhenUsingWrongThread(false)
    }

    private fun pause() {
        mExoPlayer?.apply {
            playWhenReady = false
            if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                //Log.d("Test", "value of state: pause" + mExoPlayer!!.playbackState)
            }

        }
    }

    private fun stop() {
        // release the resources when the service is destroyed
        mExoPlayer?.playWhenReady = false
        mExoPlayer?.release()
        mExoPlayer = null
        updatePlaybackState(PlaybackStateCompat.STATE_NONE)
        mMediaSession?.isActive = false
        mMediaSession?.release()
        cancelHandler()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        state_player = isPlaying
        val b = Bundle()
        b.putBoolean("play_status", state_player!!)
        mMediaSession?.sendSessionEvent("play_pause",b)
        //   Log.d("Test", "value of state: override$isPlaying")
    }

    override fun onPlaybackStateChanged(state: Int) {
        when (state) {
            Player.STATE_READY -> {
                startHandler()
            }

            Player.STATE_BUFFERING -> {

            }
            Player.STATE_ENDED -> {
                cancelHandler()
                mExoPlayer?.seekToDefaultPosition()
                pause()
                mMediaSession?.sendSessionEvent("finish", null)

            }
            Player.STATE_IDLE -> {
                cancelHandler()
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()

    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {

    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }

    private fun updatePlaybackState(state: Int) {
        // You need to change the state because the action taken in the controller depends on the state !!!
        mMediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder().setState(
                state // this state is handled in the media controller
                ,
                mExoPlayer?.currentPosition ?: PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                1.0f // Speed playing
            ).build()
        )
    }

    private fun initializeAttributes() {
        mAttrs = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()
    }

   /* private lateinit var mExtractorFactory: ExtractorMediaSource.Factory

    private fun initializeExtractor() {
        val userAgent = Util.getUserAgent(baseContext, "Application Name")
        mExtractorFactory = ExtractorMediaSource.Factory(DefaultDataSourceFactory(this, userAgent))
            .setExtractorsFactory(DefaultExtractorsFactory())
    }*/

    /* private fun extractMediaSourceFromUri(uri: Uri): MediaSource {

         return mExtractorFactory.createMediaSource(uri)
     }*/

    private fun buildMediaItem(uri: Uri): MediaItem {
        return MediaItem.fromUri(uri)
    }


    /* companion object {
         val intentFilter = IntentFilter().apply {
             addAction(PlayerNotificationManager.ACTION_NEXT)
             addAction(PlayerNotificationManager.ACTION_PREVIOUS)
             addAction(PlayerNotificationManager.ACTION_PAUSE)
             addAction(PlayerNotificationManager.ACTION_STOP)
             addAction(PlayerNotificationManager.ACTION_PLAY)
         }
     }

     inner class NotificationReceiver : BroadcastReceiver() {


         override fun onReceive(context: Context?, intent: Intent?) {
             when (intent?.action) {
                 PlayerNotificationManager.ACTION_NEXT -> {
                 }
                 PlayerNotificationManager.ACTION_PREVIOUS -> {
                 }
                 PlayerNotificationManager.ACTION_PAUSE -> {
                     Log.d(
                         "Test", "value of state: checking receiver" + (mExoPlayer?.playbackState
                                 == Player.STATE_READY && mExoPlayer!!.playWhenReady)
                     )
                     //updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                     mExoPlayer?.playWhenReady = false
                     Log.d(
                         "Test",
                         "value of state: checking receiver after" + (mExoPlayer?.playbackState
                                 == Player.STATE_READY && mExoPlayer!!.playWhenReady)
                     )
                 }
                 PlayerNotificationManager.ACTION_PLAY -> {
                     updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                 }
                 PlayerNotificationManager.ACTION_STOP -> {
                     //do what you want here!!!
                 }
             }
         }
     }*/
}