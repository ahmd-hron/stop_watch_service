package com.example.test_native_notfications
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.flutter.plugin.common.EventChannel

class MainActivity:FlutterActivity(), EventChannel.StreamHandler {
    private val CHANNEL = "com.example.app/notification"
    var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakeLockTag")
    }
    companion object {
        var eventSink: EventChannel.EventSink? = null
        var isRunning = true
        var seconds = 0
        var startTime = 0L
        var logId=""
    }





    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, "com.example.app/receiver").setStreamHandler(this)
        configureMethodChannel(flutterEngine)
    }

    private fun configureMethodChannel(flutterEngine: FlutterEngine) {
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "showNotification" -> {
                val bodyText = call.argument<String>("bodyText")
                 MainActivity.logId = call.argument<String>("logId")!!
                isRunning = true
                showNotification(bodyText!!)
                result.success("Notification shown")
                }
                "cancelNotification" -> {
                    val intent = Intent(this, StopActionReceiver::class.java)
                    sendBroadcast(intent)
                    result.success("Notification cancelled")
                }
                else -> result.notImplemented()
            }
        }
    }


    fun showNotification(bodyText:String) {
        val channelId = "com.example.app"
        val channelName = "Example Channel"
        createNotificationChannel(channelId, channelName)

        val stopPendingIntent = createPendingIntent(StopActionReceiver::class.java, 0)
        val cancelPendingIntent = createPendingIntent(CancelActionReceiver::class.java, 1)
        val mainActivityPendingIntent = createPendingIntent(MainActivity::class.java, 0)

        var builder = createNotificationBuilder(channelId, mainActivityPendingIntent, cancelPendingIntent, stopPendingIntent,bodyText)
        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
        wakeLock?.acquire()
        startStopwatch(channelId, mainActivityPendingIntent, cancelPendingIntent, stopPendingIntent,bodyText)
    }

    private fun createPendingIntent(receiver: Class<*>, requestCode: Int): PendingIntent {
        val intent = Intent(this, receiver)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    private fun createNotificationBuilder(channelId: String, mainActivityPendingIntent: PendingIntent, cancelPendingIntent: PendingIntent, stopPendingIntent: PendingIntent,bodyText: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Stopwatch Notification").setOngoing(true)
            .setContentText("Stopwatch is running.").setContentIntent(mainActivityPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW).setOnlyAlertOnce(true)
            .addAction(R.drawable.ic_notification, "Cancel", cancelPendingIntent)
            .addAction(R.drawable.ic_notification, "Stop", stopPendingIntent)
//            .setUsesChronometer(true)
            .setWhen(MainActivity.startTime)
    }

    private fun startStopwatch(channelId: String, mainActivityPendingIntent: PendingIntent, cancelPendingIntent: PendingIntent, stopPendingIntent: PendingIntent,bodyText: String) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
             var seconds = 0
            override fun run() {
                if(!isRunning)return;

                val seconds = calculateSeconds()
                MainActivity.seconds=seconds
                MainActivity.eventSink?.success("Seconds: $seconds")

                val time = String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
                val notification = createNotificationBuilder(channelId, mainActivityPendingIntent, cancelPendingIntent, stopPendingIntent,bodyText)
                    .setContentText(bodyText+time)
                    .build()

                val notificationManager = NotificationManagerCompat.from(this@MainActivity)
                notificationManager.notify(1, notification)


                if (MainActivity.startTime == 0L) {
                    MainActivity.startTime = System.currentTimeMillis()
                }



                if (isRunning) {
                    handler.postDelayed(this, 1000)
                }
            }

        }

        handler.post(runnable)
    }

    // Method to calculate elapsed time in seconds
    private fun calculateSeconds(): Int {
        return ((System.currentTimeMillis() - MainActivity.startTime) / 1000).toInt()
    }




    fun createNotificationChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    
    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        eventSink = events
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }

}


class CancelActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        MainActivity.isRunning = false
        NotificationManagerCompat.from(context).cancel(1)
        val map = HashMap<String, Any>()
        map["action"] = "CancelActionReceiver triggered"
        // Add more key-value pairs to the map as needed
        MainActivity.eventSink?.success(map)
    }
}

class StopActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        var seconds=MainActivity.seconds
        MainActivity.isRunning = false
        NotificationManagerCompat.from(context).cancel(1)
        val map = HashMap<String, Any>()
        map["action"] = "StopActionReceiver triggered"
        map["logId"]=MainActivity.logId
        map["timeSpent"]= String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
        // Add more key-value pairs to the map as needed
        MainActivity.eventSink?.success(map)
    }
}

