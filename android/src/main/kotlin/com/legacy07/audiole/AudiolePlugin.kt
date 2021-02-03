package com.legacy07.audiole

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
import org.json.JSONObject
import java.util.concurrent.TimeUnit


/** AudiolePlugin */
class AudiolePlugin: FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {

  private var context: Context? = null
  private var eventChannel: EventChannel?= null
  private lateinit var mediaPlayer: MediaPlayer
  private var chargingStateChangeReceiver: BroadcastReceiver? = null
  lateinit var buttonText:String
  private lateinit var channel : MethodChannel
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
    when (call.method){
      "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
      "play" -> {
        result.success(playAudiole(
                Uri.parse(call.argument<String>("uri")),
                call.argument<String>("playstatus").toString())
        )
      }
  }}

  fun playAudiole(audioUri: Uri, playstatus: String): HashMap<String, Any> {

    buttonText=playstatus
    if(mediaPlayer.isPlaying){
      mediaPlayer.pause()
      buttonText="Resume"
      Toast.makeText(context, "media pause", Toast.LENGTH_SHORT).show()
    }else {
      if (buttonText=="Play"||mediaPlayer.duration==mediaPlayer.currentPosition) {
        mediaPlayer = MediaPlayer.create(context, audioUri)
        mediaPlayer.start()
        buttonText="Pause"
        Toast.makeText(context, "media playing", Toast.LENGTH_SHORT).show()
      }
      else {
        mediaPlayer.seekTo(mediaPlayer.currentPosition)
        mediaPlayer.start()
        buttonText="Pause"
        Toast.makeText(context, "media resume", Toast.LENGTH_SHORT).show()
      }
    }

   val returnMap:HashMap<String,Any> = HashMap<String,Any>()
    returnMap["playstatus"] = buttonText
    returnMap["duration"] = (mediaPlayer.duration/1000).toInt()

    return returnMap
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
                     // Log.w(TAG, "emitting timer event ${mediaPlayer.currentPosition}")
                      events!!.success((mediaPlayer.currentPosition/1000).toInt())
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