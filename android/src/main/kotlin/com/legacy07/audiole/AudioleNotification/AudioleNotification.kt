import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.legacy07.audiole.AudioleMediaService
import com.legacy07.audiole.AudioleNotificationHelper
import com.legacy07.audiole.AudiolePlugin
import com.legacy07.audiole.R


class AudioleNotificationManager(audioleMediaService: AudioleMediaService) {
    private val ctx: Context=audioleMediaService.application

    fun createNotification(): Notification {
        val notificationChannelId = "AUDIOLE SERVICE CHANNEL"
        val contentView = RemoteViews(ctx.packageName,
                R.layout.statusbar_notification)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

        val pendingIntent: PendingIntent = Intent(ctx.applicationContext, AudiolePlugin::class.java).let { notificationIntent ->
            PendingIntent.getActivity(ctx.applicationContext, 0, notificationIntent, 0)
        }

        // Get the layouts to use in the custom notification
        val notificationLayout = RemoteViews(ctx.packageName, R.layout.statusbar_notification_small)
        val notificationLayoutExpanded = RemoteViews(ctx.packageName, R.layout.statusbar_notification)

        setListeners(notificationLayoutExpanded)
        return NotificationCompat.Builder(ctx.applicationContext, notificationChannelId)
                .setSmallIcon(R.drawable.ic_nitification)
                .setContent(notificationLayoutExpanded)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setContentTitle("Audiole Player")
                .setCustomBigContentView(notificationLayoutExpanded)
                .setContentIntent(pendingIntent)
                .build()
    }

    fun setListeners(view: RemoteViews) {
        //radio listener

        Log.d("weeeee","eeerrrHEREEEE")
        val radio = Intent(ctx, AudioleNotificationHelper::class.java)
        radio.putExtra("DO", "radio")

        val pRadio = PendingIntent.getActivity(ctx, 0, radio, 0)
        view.setOnClickPendingIntent(R.id.albumart, pRadio)
        //volume listener
        val volume = Intent(ctx, AudioleNotificationHelper::class.java)
        volume.putExtra("DO", "volume")
        val pVolume = PendingIntent.getActivity(ctx, 1, volume, 0)
        view.setOnClickPendingIntent(R.id.backbutton_exp, pVolume)
        //reboot listener
        val reboot = Intent(ctx, AudioleNotificationHelper::class.java)
        reboot.putExtra("DO", "reboot")
        val pReboot = PendingIntent.getActivity(ctx, 5, reboot, 0)
        view.setOnClickPendingIntent(R.id.mainbutton_exp, pReboot)
        //top listener
        val top = Intent(ctx, AudioleNotificationHelper::class.java)
        top.putExtra("DO", "top")
        val pTop = PendingIntent.getActivity(ctx, 3, top, 0)
        view.setOnClickPendingIntent(R.id.forwardbutton_exp, pTop)

    }

}