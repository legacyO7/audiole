import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.legacy07.audiole.AudioleMediaService
import com.legacy07.audiole.AudiolePlugin
import com.legacy07.audiole.R

fun createNotification(audioleMediaService: AudioleMediaService): Notification {
    val notificationChannelId = "RED SERVICE CHANNEL"
    val contentView = RemoteViews(audioleMediaService.packageName,
            R.layout.statusbar_notification)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = audioleMediaService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

    val pendingIntent: PendingIntent = Intent(audioleMediaService.applicationContext, AudiolePlugin::class.java).let { notificationIntent ->
        PendingIntent.getActivity(audioleMediaService.applicationContext, 0, notificationIntent, 0)
    }

    // Get the layouts to use in the custom notification
    val notificationLayout = RemoteViews(audioleMediaService.packageName, R.layout.statusbar_notification_small)
    val notificationLayoutExpanded = RemoteViews(audioleMediaService.packageName, R.layout.statusbar_notification)


    return  NotificationCompat.Builder(audioleMediaService.applicationContext, notificationChannelId)
            .setSmallIcon(R.drawable.ic_nitification)
            .setVibrate(null)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayoutExpanded)
            .setContentIntent(pendingIntent)
            .build()
}