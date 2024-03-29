package com.legacy07.audiole

import android.app.Activity
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.broadcastreceiver.BroadcastReceiverAware
import io.flutter.embedding.engine.plugins.broadcastreceiver.BroadcastReceiverPluginBinding
import io.flutter.plugin.common.*
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import wseemann.media.FFmpegMediaMetadataRetriever
import java.util.concurrent.TimeUnit
import javax.xml.datatype.DatatypeConstants.SECONDS


/** AudiolePlugin */
class AudiolePlugin : FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler, ActivityAware, BroadcastReceiverAware {

    private lateinit var mContext: Context
    private var mActivity: Activity? = null
    private var eventChannel: EventChannel? = null
    private lateinit var mediaPlayer: MediaPlayer
    private var mmr: FFmpegMediaMetadataRetriever = FFmpegMediaMetadataRetriever()
    lateinit var buttonText: String
    private lateinit var channel: MethodChannel
    private var timerSubscription: Disposable? = null
    private val broadcastReceiver: AudioleReceiver = AudioleReceiver()


    /**
     * Connect to engine
     */
    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        onAttachedToEngine(flutterPluginBinding.applicationContext, flutterPluginBinding.binaryMessenger)
    }

    private fun onAttachedToEngine(applicationContext: Context, messenger: BinaryMessenger) {
        mediaPlayer = MediaPlayer();
        this.mContext = applicationContext

        channel = MethodChannel(messenger, "audiole")
        channel.setMethodCallHandler(this)

        eventChannel = EventChannel(messenger, "com.legacy.audiole/stream")
        eventChannel?.setStreamHandler(this)

        val intentFilter = IntentFilter("com.legacy07.audiole")
        mContext.registerReceiver(broadcastReceiver, intentFilter)
    }

    companion object {
        @JvmStatic
        fun registerWith(registrar: PluginRegistry.Registrar) {
            val instance = AudiolePlugin()
            instance.onAttachedToEngine(registrar.context(), registrar.messenger())
        }
    }

    /**
     * Callback method
     */
    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "play" -> {
                result.success(playinBackground(
                        call.argument<String>("uri").toString(),
                        call.argument<String>("playstatus").toString())
                )
            }
            "seek" -> {
                result.success(seekTo(call.argument<Int>("seekTo")!!)
                )
            }
            "trackInfo" -> {
                result.success(trackInfo(Uri.parse(call.argument<String>("uri"))))
            }
            "folderDetails" -> {
                result.success(FileManager().getFileManager(call.argument<String>("folderUri").toString()))

            }
        }
    }

    fun trackInfo(audioUri: Uri): HashMap<String, Any> {
        Log.d("dura", "called me")
        val returnMap: HashMap<String, Any> = HashMap<String, Any>()
        mediaPlayer = MediaPlayer.create(mContext, audioUri)
        try {
            mmr.setDataSource(audioUri.toString())
            returnMap["MEDIA_ARTIST"] = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST)
            returnMap["MEDIA_ALBUM"] = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM)
            returnMap["MEDIA_TITLE"] = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE)
            returnMap["MEDIA_TRACK"] = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TRACK)
            returnMap["MEDIA_ART"] = mmr.embeddedPicture
            mmr.release()
        } catch (e: Exception) {
            Log.d("error", e.toString());
        }
        returnMap["duration"] = (mediaPlayer.duration / 1000).toInt()
        mediaPlayer.release()
        Log.d("dura", returnMap["duration"].toString())
        return returnMap

    }

    fun playinBackground(audioUri: String, playstatus: String): String {
        val intent = Intent(mActivity, AudioleMediaService::class.java)
        intent.putExtra("action", "play")
        intent.putExtra("audioUri", audioUri)
        intent.putExtra("playstatus", playstatus)
        mActivity?.startService(intent)
        return "done";
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = mActivity?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


    fun tos(message: String) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
    }

    fun seekTo(position: Int) {
        tos("seeking")
        val intent = Intent(mActivity, AudioleMediaService::class.java)
        intent.putExtra("action", "seek")
        intent.putExtra("seekTo", position * 1000)
        mActivity?.startService(intent)
    }

    /**
     * Break away from the engine
     */
    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        //  mContext = null
        eventChannel?.setStreamHandler(null)
        eventChannel = null
    }

    /**
     * monitor
     */
    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        timerSubscription = Observable
                .interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {

                            //   Log.d("timer", broadcastReceiver.currentPosition.toString())

                            events!!.success(broadcastReceiver.currentPosition)
                        },
                        { error: Throwable ->
                            Log.e(TAG, "error in emitting timer", error)
                            events!!.error("STREAM", "Error in processing observable", error.message)
                        },
                        { Log.w(TAG, "closing the timer observable") }
                )
    }

    /**
     * Cancel listening
     */
    override fun onCancel(arguments: Any?) {
        if (timerSubscription != null) {
            timerSubscription!!.dispose();
            timerSubscription = null;
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.mActivity = binding.activity;
    }

    override fun onDetachedFromActivityForConfigChanges() {
        TODO("Not yet implemented")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        TODO("Not yet implemented")
    }

    override fun onDetachedFromActivity() {
        mActivity!!.unregisterReceiver(broadcastReceiver!!)
        // broadcastReceiver = null
    }


    override fun onAttachedToBroadcastReceiver(binding: BroadcastReceiverPluginBinding) {
        Toast.makeText(mContext, "Broadcast Intent Detected.",
                Toast.LENGTH_LONG).show()
    }

    override fun onDetachedFromBroadcastReceiver() {
        Toast.makeText(mContext, "Broadcast Intent Detected.",
                Toast.LENGTH_LONG).show()
    }

}


class AudioleReceiver : BroadcastReceiver() {

    var currentPosition: Int

    init {
        currentPosition = 1
    }

    override fun onReceive(context: Context, intent: Intent) {
        val b = intent.extras
        currentPosition = b!!.getInt("currentPosition", 0)
        //  Log.d("cp broadcast", currentPosition.toString())

    }
}