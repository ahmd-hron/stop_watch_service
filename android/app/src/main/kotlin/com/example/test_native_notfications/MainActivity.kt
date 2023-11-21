package com.example.test_native_notfications
import AppNotificationManager
import android.content.Context
import android.content.Intent
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
    }

    companion object {
        var eventSink: EventChannel.EventSink? = null
        var isRunning = true
        var seconds = 0
        var startTime = 0L
        var logId=""
        var wakeLock: PowerManager.WakeLock? = null
    }

    private val notificationManager = AppNotificationManager(this)

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
                notificationManager.showNotification(bodyText!!)
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

    
    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        eventSink = events
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }

}




