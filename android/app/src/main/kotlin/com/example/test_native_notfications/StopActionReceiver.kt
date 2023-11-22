package com.example.test_native_notfications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class StopActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        var seconds=MainActivity.seconds
        MainActivity.isRunning = false
        NotificationManagerCompat.from(context).cancel(1)
        val map = HashMap<String, Any>()
        map["action"] = "StopActionReceiver"
        if(MainActivity.logId!=null)  map["logId"]=MainActivity.logId!!
        map["timeSpent"]= String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
        // Add more key-value pairs to the map as needed
        Intent(context,NotificationService::class.java).also {
            it.action=NotificationService.Actions.STOP.toString()
            context.startService(it)
        }
        MainActivity.eventSink?.success(map)
        MainActivity.startTime=0L
        MainActivity.wakeLock?.release()
    }
}