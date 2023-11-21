package com.example.test_native_notfications
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationService (private val context: Context) : Service() {

    val channelId = "com.example.app"
    val channelName = "Example Channel"
    var started=false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun startForegroundService(notification: Notification, id :Any){
//        if(!started){
//            startForeground(1, notification)
//            started=true
//        }
    }

    fun createPendingIntent(receiver: Class<*>, requestCode: Int): PendingIntent {
        val intent = Intent(context, receiver)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    fun createActivityPendingIntent(receiver: Class<*>, requestCode: Int): PendingIntent {
        val intent = Intent(context, receiver)
        MainActivity.eventSink?.success("adding this log id ${MainActivity.logId}")
        intent.putExtra("notification_tapped", MainActivity.logId)
        intent.putExtra("logId", MainActivity.logId)
        intent.putExtra("opened_from_notification", true)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }




    fun createNotificationBuilder( mainActivityPendingIntent: PendingIntent, cancelPendingIntent: PendingIntent, stopPendingIntent: PendingIntent, bodyText: String): NotificationCompat.Builder {

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Stopwatch Notification").setOngoing(true)
            .setContentText("Stopwatch is running.").setContentIntent(mainActivityPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW).setOnlyAlertOnce(true)
            .addAction(R.drawable.ic_notification, "Cancel", cancelPendingIntent)
            .addAction(R.drawable.ic_notification, "Stop", stopPendingIntent)
            .setWhen(MainActivity.startTime)
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    fun showNotification(bodyText:String) {
        val stopPendingIntent = createPendingIntent(StopActionReceiver::class.java, 0)
        val cancelPendingIntent = createPendingIntent(CancelActionReceiver::class.java, 1)
        val mainActivityPendingIntent = createActivityPendingIntent(MainActivity::class.java, 2)

        var builder = createNotificationBuilder( mainActivityPendingIntent, cancelPendingIntent, stopPendingIntent,bodyText)
        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }

        MainActivity.wakeLock?.acquire()
        startStopwatch(channelId, mainActivityPendingIntent, cancelPendingIntent, stopPendingIntent,bodyText)
    }

    private fun startStopwatch(channelId: String, mainActivityPendingIntent: PendingIntent, cancelPendingIntent: PendingIntent, stopPendingIntent: PendingIntent, bodyText: String) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if(!MainActivity.isRunning)return;

                val seconds = calculateSeconds()
                MainActivity.seconds=seconds

                val time = String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
                val notification = createNotificationBuilder( mainActivityPendingIntent, cancelPendingIntent, stopPendingIntent,bodyText)
                    .setContentText(bodyText+time)
                    .build()
                startForegroundService(notification,1)
                val notificationManagerCompat = NotificationManagerCompat.from(context)
                notificationManagerCompat.notify(1, notification)

                if (MainActivity.startTime == 0L) {
                    MainActivity.startTime = System.currentTimeMillis()
                }

                if (MainActivity.isRunning) {
                    handler.postDelayed(this, 1000)
                }
            }
        }

        handler.post(runnable)
    }


    fun calculateSeconds(): Int {
        return ((System.currentTimeMillis() - MainActivity.startTime) / 1000).toInt()
    }
}