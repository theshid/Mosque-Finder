package com.shid.mosquefinder.app.ui.services

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.main.views.MusicActivity
import com.shid.mosquefinder.app.utils.helper_class.Constants
import com.shid.mosquefinder.app.utils.helper_class.Constants.BUNDLE_KEY_CURRENT_POSITION
import com.shid.mosquefinder.app.utils.helper_class.Constants.BUNDLE_KEY_DURATION
import com.shid.mosquefinder.app.utils.helper_class.Constants.BUNDLE_KEY_PLAYER_STATUS
import com.shid.mosquefinder.app.utils.helper_class.Constants.COMMAND_REPEAT
import com.shid.mosquefinder.app.utils.helper_class.Constants.COMMAND_SURAH
import com.shid.mosquefinder.app.utils.helper_class.Constants.EVENT_MEDIA_INFORMATION
import com.shid.mosquefinder.app.utils.helper_class.Constants.EVENT_PLAYER_FINISH
import com.shid.mosquefinder.app.utils.helper_class.Constants.EVENT_PLAYER_PAUSE
import com.shid.mosquefinder.app.utils.helper_class.Constants.EXTRA_STATE_PLAYER

private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"

class MusicService : MediaBrowserServiceCompat(), Player.Listener {


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
    private var surahName: String = "test"


    private val mMediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            uri?.let {
                val mediaItem = buildMediaItem(uri)
                if (uri != oldUri) {
                    play(mediaItem)
                } else play() // this song was paused so we don't need to reload it
                oldUri = uri
            }
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            super.onCommand(command, extras, cb)

            if (command == Constants.COMMAND_SEEK) {
                extras?.let { mExoPlayer?.seekTo(it.getLong(Constants.COMMAND_SEEK)) }
            } else if (command == COMMAND_REPEAT) {
                extras?.let { isRepeat = it.getBoolean(COMMAND_REPEAT) }
                if (isRepeat) {
                    mExoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
                } else {
                    mExoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
                }
            } else if (command == COMMAND_SURAH) {
                surahName = extras?.getString("surah")!!
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
        initializeAttributes()

        mMediaSession = MediaSessionCompat(baseContext, "tag for debugging").apply {
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
            mMediaSession?.sendSessionEvent(EVENT_MEDIA_INFORMATION, information)
            h?.postDelayed(this, 1000)
        }

    }

    private fun sendPositionAndDuration(): Bundle {
        val b = Bundle()
        b.putLong(BUNDLE_KEY_CURRENT_POSITION, mExoPlayer!!.currentPosition)
        b.putLong(BUNDLE_KEY_DURATION, mExoPlayer!!.duration)
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
            mAttrs?.let { initializeAttributes() }
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
        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            notificationId,
            channelId,
            object : PlayerNotificationManager.MediaDescriptionAdapter {
                @RequiresApi(Build.VERSION_CODES.M)
                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    // return pending intent
                    val intent = Intent(context, MusicActivity::class.java)
                    intent.putExtra(EXTRA_STATE_PLAYER, state_player)
                    val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.getActivity(
                            context, 0, intent,
                            PendingIntent.FLAG_MUTABLE
                        )
                    } else {
                        PendingIntent.getActivity(
                            context, 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    }
                    return pendingIntent
                }

                //pass description here
                override fun getCurrentContentText(player: Player): String? {
                    return Constants.QARI_MISHARY
                }

                //pass title (mostly playing audio name)
                override fun getCurrentContentTitle(player: Player): String {
                    return surahName
                }

                // pass image as bitmap
                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? {
                    return BitmapFactory.decodeResource(resources, R.drawable.logo2)
                }
            }).setNotificationListener(object : PlayerNotificationManager.NotificationListener {

            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                onGoing: Boolean
            ) {
                if (onGoing) {
                    startForeground(notificationId, notification)
                } else {
                    stopForeground(false)
                }


            }

            override fun onNotificationCancelled(
                notificationId: Int,
                dismissedByUser: Boolean
            ) {
                stopSelf()
                stopForeground(true)
            }

        })
            .setChannelDescriptionResourceId(R.string.channel_desc)
            .setChannelNameResourceId(R.string.channel_name)
            .build()

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

    }

    private fun pause() {
        mExoPlayer?.apply {
            playWhenReady = false
            if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
            }
        }
    }

    private fun stop() {
        playerNotificationManager.setPlayer(null)
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
        b.putBoolean(BUNDLE_KEY_PLAYER_STATUS, state_player!!)
        mMediaSession?.sendSessionEvent(EVENT_PLAYER_PAUSE, b)
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
                mMediaSession?.sendSessionEvent(EVENT_PLAYER_FINISH, null)
                stopForeground(true)
                playerNotificationManager.setPlayer(null)

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
        mMediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder().setState(
                state,
                mExoPlayer?.currentPosition ?: PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                1.0f
            ).build()
        )
    }

    private fun initializeAttributes() {
        mAttrs = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()
    }


    private fun buildMediaItem(uri: Uri): MediaItem {
        return MediaItem.fromUri(uri)
    }

}