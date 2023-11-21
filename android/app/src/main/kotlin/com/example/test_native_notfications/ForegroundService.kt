//import android.app.Service
//import android.content.Intent
//import android.os.IBinder
//import com.example.test_native_notfications.CancelActionReceiver
//import com.example.test_native_notfications.MainActivity
//import com.example.test_native_notfications.StopActionReceiver
//
//class YourService : Service() {
//    private val notificationService = AppNotificationManager(this)
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {[^1^][1]
//        val channelId = "com.example.app"
//        val channelName = "Example Channel"
//        notificationManager.createNotificationChannel(channelId, channelName)
//
//        val stopPendingIntent = notificationManager.createPendingIntent(StopActionReceiver::class.java, 0)
//        val cancelPendingIntent = notificationManager.createPendingIntent(CancelActionReceiver::class.java, 1)
//        val mainActivityPendingIntent = notificationManager.createPendingIntent(MainActivity::class.java, 0)
//
//        val bodyText = "Your body text here"
//        notificationManager.showNotification(bodyText)
//
//        return START_STICKY
//    }
//
//    override fun onBind(intent: Intent): IBinder? {
//        return null
//    }
//}
