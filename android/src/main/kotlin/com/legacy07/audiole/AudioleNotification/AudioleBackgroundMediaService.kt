package com.legacy07.audiole

import android.app.*
import android.content.Context
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
import wseemann.media.FFmpegMediaMetadataRetriever


class AudioleMediaService : Service() {
    var mediaPlayer: MediaPlayer = MediaPlayer()
    var audioUri: String? = null
    private var mmr: FFmpegMediaMetadataRetriever = FFmpegMediaMetadataRetriever()
    lateinit var playstatus: String
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


        when (intent.getStringExtra("action")){
            "play"->{
                audioUri = intent.getStringExtra("audioUri")
                playstatus = intent.getStringExtra("playstatus")

                if (audioUri != null) {
                    playAudiole(Uri.parse(audioUri), playstatus)
//            mediaPlayer!!.isLooping = true // Set looping
//            mediaPlayer!!.setVolume(100f, 100f)
                }
            }

            "seek"->{
                mediaPlayer.seekTo(intent.getIntExtra("seekTo",mediaPlayer.currentPosition))
                timer.cancel()
                mediaTicker(mediaPlayer.duration,mediaPlayer.currentPosition)
            }
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

        Log.d("Received playstatus",playstatus)

        if (mediaPlayer.isPlaying && playstatus!="Play") {
            mediaPlayer.pause()
            this.playstatus = "Resume"
            timer.cancel()
            Toast.makeText(this, "media pause", Toast.LENGTH_SHORT).show()
        } else {
            this.playstatus = playstatus
            if (this.playstatus == "Play" || mediaPlayer.duration == mediaPlayer.currentPosition) {
                if (mediaPlayer.isPlaying) {
                    timer.cancel()
                    mediaPlayer.stop()
                    mediaPlayer.release()
                }
                mediaPlayer = MediaPlayer.create(this, audioUri)
                mediaPlayer.start()
                this.playstatus = "Pause"
                Toast.makeText(this, "media playing", Toast.LENGTH_SHORT).show()
            } else {
                mediaPlayer.seekTo(mediaPlayer.currentPosition)
                mediaPlayer.start()
                this.playstatus = "Pause"
                Toast.makeText(this, "media resume", Toast.LENGTH_SHORT).show()
            }

           mediaTicker(mediaPlayer.duration,mediaPlayer.currentPosition);

        }
    }

    fun mediaTicker(mediaDuration: Int,mediaCurrentPosition:Int){
            timer = object : CountDownTimer((mediaDuration-mediaCurrentPosition).toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                   // Log.d("timer", ((mediaduration-millisUntilFinished/1000)).toString())
                    bundle.putInt("currentPosition", ((mediaDuration-millisUntilFinished)/1000).toInt())
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