package com.example.test_native_notfications
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.EventChannel

class MainActivity:FlutterActivity(), EventChannel.StreamHandler {
    private val CHANNEL = "com.example.app/notification"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakeLockTag")
        logId = intent.getStringExtra("logId")
    }

    override fun onResume() {
        super.onResume()

        if (logId != null) {
            val map:HashMap<String, Any> =HashMap<String,Any>()
            map["action"] = "Notification tapped"
            map["logId"]= logId!!
            eventSink?.success(map)
        }
    }

    companion object {
        var eventSink: EventChannel.EventSink? = null
        var isRunning = true
        var seconds = 0
        var startTime = 0L
        var logId: String? = null
        var wakeLock: PowerManager.WakeLock? = null
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
                logId = call.argument<String>("logId")!!
                isRunning = true
                Intent(applicationContext,NotificationService::class.java).also {
                    it.action=NotificationService.Actions.START.toString()
                    it.putExtra("bodyText",bodyText)
                    startService(it)
                }
//                notificationService.showNotification(bodyText!!)
                result.success("Notification shown")
                }
                "cancelNotification" -> {
                    result.success("Notification cancelled")
                    stopService()
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun stopService(){
        // calls stop action
        val intent = Intent(this, StopActionReceiver::class.java)
        sendBroadcast(intent)
        // stop service
        Intent(applicationContext,NotificationService::class.java).also {
            it.action=NotificationService.Actions.STOP.toString()
            startService(it)
        }
        wakeLock?.release()
    }

    
    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        eventSink = events
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }

}




