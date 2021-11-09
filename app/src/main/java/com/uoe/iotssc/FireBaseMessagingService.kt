package com.uoe.iotssc

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.function.ToIntBiFunction


class MyFireBaseMessagingService : FirebaseMessagingService() {
    val tag = "Service"

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Log.d(tag, "msg received")
        Log.d(tag, "From: " + p0!!.from + " body: " + p0.notification?.body!!)
        val toastTxt = "${p0.notification!!.title} - ${p0.notification!!.body}"
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, toastTxt, Toast.LENGTH_LONG).show()
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d(tag, "Refreshed token: $p0")
        //sendRegistrationToServer(p0)
    }
}