package com.example.test_native_notfications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class CancelActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        MainActivity.isRunning = false
        NotificationManagerCompat.from(context).cancel(1)
        val map = HashMap<String, Any>()
        map["action"] = "CancelActionReceiver"
        // Add more key-value pairs to the map as needed
        MainActivity.eventSink?.success(map)
    }
}
