package com.legacy07.audiole

import android.app.Activity
import android.content.Intent.getIntent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class AudioleNotificationHelper : Activity() {

    private var ctx: AudioleNotificationHelper? = null

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ctx = this
        Log.d("weeeee","HEREEEE")
        val action = intent.extras!!["DO"] as String?
        if (action == "radio") {
           Log.d("weeeee","woooooo")
        } else if (action == "volume") {
            Log.d("weeeee","woooooo")
        } else if (action == "reboot") {
            Log.d("weeeee","woooooo")
        } else if (action == "top") {
            Log.d("weeeee","woooooo")
        } else if (action == "app") {
            Log.d("weeeee","woooooo")
        }
        if (action != "reboot") finish()
    }

     override fun onDestroy() {
        super.onDestroy()
        // TODO Auto-generated method stub

    }
}