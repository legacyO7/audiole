package com.legacy07.audiole

import android.app.*
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import wseemann.media.FFmpegMediaMetadataRetriever
import java.time.Duration
import java.util.*
import javax.net.ssl.SSLContext.getInstance


class AudioleMediaService : Service() {
    var mediaPlayer: MediaPlayer = MediaPlayer()
    var audioUri: String? = null
    private var mmr: FFmpegMediaMetadataRetriever = FFmpegMediaMetadataRetriever()
    lateinit var buttonText: String
    lateinit var timer: CountDownTimer
    val intent = Intent("com.legacy07.audiole")
    val bundle = Bundle()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val notification = createNotification()
        startForeground(1, notification)

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        audioUri = intent.getStringExtra("audioUri")
        buttonText = intent.getStringExtra("playstatus")

        if (audioUri != null) {
            playAudiole(Uri.parse(audioUri), buttonText)
//            mediaPlayer!!.isLooping = true // Set looping
//            mediaPlayer!!.setVolume(100f, 100f)
        }
        return START_STICKY
    }

    override fun onStart(intent: Intent, startId: Int) {}
    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    override fun onLowMemory() {}

    fun sendReturn(duration:Int,playstatus: String){

        bundle.putString("playstatus", playstatus)
        bundle.putInt("duration", duration)
        intent.putExtras(bundle)
        sendBroadcast(intent)
    }

    fun playAudiole(audioUri: Uri, playstatus: String) {

        if (mediaPlayer.isPlaying && buttonText == playstatus) {
            mediaPlayer.pause()
            buttonText = "Resume"
            timer.cancel()
            Toast.makeText(this, "media pause", Toast.LENGTH_SHORT).show()
        } else {
            buttonText = playstatus
            if (buttonText == "Play" || mediaPlayer.duration == mediaPlayer.currentPosition) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    mediaPlayer.release()
                }
                mediaPlayer = MediaPlayer.create(this, audioUri)
                mediaPlayer.start()
                Toast.makeText(this, "media playing", Toast.LENGTH_SHORT).show()
            } else {
                mediaPlayer.seekTo(mediaPlayer.currentPosition)
                mediaPlayer.start()
                Toast.makeText(this, "media resume", Toast.LENGTH_SHORT).show()
            }
            buttonText = "Pause"
            mediaTicker(mediaPlayer.duration-mediaPlayer.currentPosition);

        }

        sendReturn((mediaPlayer.duration / 1000).toInt(),buttonText)
    }

    fun mediaTicker(duration: Int){
            timer = object : CountDownTimer((duration).toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    Log.d("timer", ((millisUntilFinished/1000)).toString())
                    bundle.putInt("currentPosition", (millisUntilFinished/1000).toInt())
                    intent.putExtras(bundle)
                    sendBroadcast(intent)
                }

                override fun onFinish() {
                    Log.d("timer", "finished")
                }
            }
            timer.start()
    }


    private fun createNotification(): Notification {
        val notificationChannelId = "RED SERVICE CHANNEL"
        val contentView = RemoteViews(packageName,
                R.layout.statusbar_notification)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                    notificationChannelId,
                    "Audiole Service notification",
                    NotificationManager.IMPORTANCE_LOW
            ).let {
                it.description = "Audiole Media"
                it.audioAttributes
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = Intent(this, AudiolePlugin::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        // Get the layouts to use in the custom notification
        val notificationLayout = RemoteViews(packageName, R.layout.statusbar_notification)
        val notificationLayoutExpanded = RemoteViews(packageName, R.layout.statusbar_notification)


        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                notificationChannelId
        ) else Notification.Builder(this)

        return  NotificationCompat.Builder(this, notificationChannelId)
                .setSmallIcon(R.drawable.ic_nitification)
                .setVibrate(null)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setContentIntent(pendingIntent)
                .build()
    }


}