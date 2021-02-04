package com.legacy07.audiole

import android.R.attr.path
import android.content.*
import android.content.ContentValues.TAG
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.*
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import wseemann.media.FFmpegMediaMetadataRetriever
import java.net.URLConnection
import java.util.concurrent.TimeUnit


/** AudiolePlugin */
class AudiolePlugin : FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {

    private var context: Context? = null
    private var eventChannel: EventChannel? = null
    private lateinit var mediaPlayer: MediaPlayer
    private var mmr: FFmpegMediaMetadataRetriever = FFmpegMediaMetadataRetriever()
    lateinit var buttonText: String
    private lateinit var channel: MethodChannel
    private var timerSubscription: Disposable? = null

    /**
     * Connect to engine
     */
    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        onAttachedToEngine(flutterPluginBinding.applicationContext, flutterPluginBinding.binaryMessenger)
    }

    private fun onAttachedToEngine(applicationContext: Context, messenger: BinaryMessenger) {
        mediaPlayer = MediaPlayer();
        this.context = applicationContext

        channel = MethodChannel(messenger, "audiole")
        channel.setMethodCallHandler(this)

        eventChannel = EventChannel(messenger, "com.legacy.audiole/stream")
        eventChannel?.setStreamHandler(this)
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
                result.success(playAudiole(
                        Uri.parse(call.argument<String>("uri")),
                        call.argument<String>("playstatus").toString())
                )
            }
            "seek" -> {
                result.success(seekTo(call.argument<Int>("seekTo")!!)
                )
            }
            "folderDetails" -> {
                result.success(FileManager().getFileManager(call.argument<String>("folderUri").toString()))

            }
        }
    }


    fun tos(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun playAudiole(audioUri: Uri, playstatus: String): HashMap<String, Any> {

        val returnMap: HashMap<String, Any> = HashMap<String, Any>()

        if (mediaPlayer.isPlaying && buttonText == playstatus) {
            mediaPlayer.pause()
            buttonText = "Resume"
            Toast.makeText(context, "media pause", Toast.LENGTH_SHORT).show()
        } else {
            buttonText = playstatus
            if (buttonText == "Play" || mediaPlayer.duration == mediaPlayer.currentPosition) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    mediaPlayer.release()
                }
                mediaPlayer = MediaPlayer.create(context, audioUri)
                mediaPlayer.start()
                buttonText = "Pause"
                Toast.makeText(context, "media playing", Toast.LENGTH_SHORT).show()
            } else {
                mediaPlayer.seekTo(mediaPlayer.currentPosition)
                mediaPlayer.start()
                buttonText = "Pause"
                Toast.makeText(context, "media resume", Toast.LENGTH_SHORT).show()
            }
            try {
                mmr.setDataSource(audioUri.toString())
                returnMap["MEDIA_ARTIST"] = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST)
                returnMap["MEDIA_ALBUM"] = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM)
                returnMap["MEDIA_TITLE"] = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE)
                returnMap["MEDIA_TRACK"] = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TRACK)
                returnMap["MEDIA_ART"] = mmr.embeddedPicture
            }catch (e:Exception){
                Log.d("error",e.toString());
            }
        }
        returnMap["playstatus"] = buttonText
        returnMap["duration"] = (mediaPlayer.duration / 1000).toInt()
        return returnMap
    }

    fun seekTo(position: Int) {
        mediaPlayer.seekTo(position * 1000)
    }

    /**
     * Break away from the engine
     */
    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        context = null
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

                            events!!.success((mediaPlayer.currentPosition / 1000).toInt())
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
}