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
        Intent(context,NotificationService::class.java).also {
            it.action=NotificationService.Actions.STOP.toString()
            context.startService(it)
        }
        MainActivity.eventSink?.success(map)
        MainActivity.startTime=0L
        MainActivity.wakeLock?.release()
    }
}
