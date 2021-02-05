/*
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.legacy07.audiole.AudioleNotification.AudioleNotificationHelper
import com.legacy07.audiole.R

class AudioleNotificationManager @SuppressLint("NewApi")
constructor(ctx: Context): Notification() {
    private val ctx:Context
    private val mNotificationManager: NotificationManager
    init{
        this.ctx = ctx
        val ns = Context.NOTIFICATION_SERVICE
        mNotificationManager = ctx.getSystemService(ns) as NotificationManager
        val tickerText = "Shortcuts"
        val `when` = System.currentTimeMillis()
        val builder = Notification.Builder(ctx)
        val notification = builder.getNotification()
        notification.`when` = `when`
        notification.tickerText = tickerText
        val contentView = RemoteViews(ctx.getPackageName(), R.layout.statusbar_notification)
        //set the button listeners
        setListeners(contentView)
        notification.contentView = contentView
        notification.flags = Notification.FLAG_ONGOING_EVENT
        val contentTitle = "From Shortcuts"
        mNotificationManager.notify(548853, notification)
    }
    fun setListeners(view:RemoteViews) {
        //radio listener
        val radio = Intent(ctx, AudioleNotificationHelper::class.java)
        radio.putExtra("DO", "radio")
        val pRadio = PendingIntent.getActivity(ctx, 0, radio, 0)
        view.setOnClickPendingIntent(R.id.radio, pRadio)
        //volume listener
        val volume = Intent(ctx, AudioleNotificationHelper::class.java)
        volume.putExtra("DO", "volume")
        val pVolume = PendingIntent.getActivity(ctx, 1, volume, 0)
        view.setOnClickPendingIntent(R.id.btn2, pVolume)
        //reboot listener
        val reboot = Intent(ctx, AudioleNotificationHelper::class.java)
        reboot.putExtra("DO", "reboot")
        val pReboot = PendingIntent.getActivity(ctx, 5, reboot, 0)
        view.setOnClickPendingIntent(R.id.btn3, pReboot)
        //top listener
        val top = Intent(ctx, AudioleNotificationHelper::class.java)
        top.putExtra("DO", "top")
        val pTop = PendingIntent.getActivity(ctx, 3, top, 0)
        view.setOnClickPendingIntent(R.id.btn4, pTop)
        //app listener
        val app = Intent(ctx, com.legacy07.audiole.AudioleNotification.AudioleNotificationHelper::class.java)
        app.putExtra("DO", "app")
        val pApp = PendingIntent.getActivity(ctx, 4, app, 0)
        view.setOnClickPendingIntent(R.id.msglbl, pApp)
    }
}*/
