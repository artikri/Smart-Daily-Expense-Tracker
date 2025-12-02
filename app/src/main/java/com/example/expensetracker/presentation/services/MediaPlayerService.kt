package com.example.expensetracker.presentation.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage

class MediaPlayerService : Service() {

    companion object{
        private const val CHANNEL_ID = "musicChannel"
        private const val NOTIFICATION_ID = 101
    }
    val musicUrl = "http://www.hochmuth.de/mp3/Vivaldi_Sonata_4_2_Largo.mp3"
    private lateinit var mediaPlayer: MediaPlayer

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        mediaPlayer.setOnPreparedListener { player ->
            player.start()
        }
        mediaPlayer.setOnErrorListener {_, _, _ ->
            true
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           val name = "Music Playback"
           val importance = NotificationManager.IMPORTANCE_LOW
           val channel = NotificationChannel(CHANNEL_ID, name, importance)
           val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
           notificationManager.createNotificationChannel(channel)
       }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Playing")
            .setContentText("The Song is playing")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        mediaPlayer.setDataSource(musicUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.start()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
    }

}