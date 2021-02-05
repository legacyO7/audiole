package com.legacy07.audiole.AudioleNotification

import android.content.Intent.getIntent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class AudioleNotificationHelper : AppCompatActivity() {

    private var ctx: AudioleNotificationHelper? = null

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO Auto-generated method stub

        ctx = this

        val action = intent.extras!!["DO"] as String?
        if (action == "radio") {
            //Your code
        } else if (action == "volume") {
            //Your code
        } else if (action == "reboot") {
            //Your code
        } else if (action == "top") {
            //Your code
        } else if (action == "app") {
            //Your code
        }
        if (action != "reboot") finish()
    }

    protected override fun onDestroy() {
        super.onDestroy()
        // TODO Auto-generated method stub

    }
}