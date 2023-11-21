import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart' as handler;

void main(List<String> args) {
  runApp(MaterialApp(home: Scaffold(body: MyApp())));
}

class MyApp extends StatefulWidget {
  MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final String methodChannel = 'com.example.app/notification';
  static const eventChannel = EventChannel('com.example.app/receiver');

  @override
  void initState() {
    print('rebuilt app');
    eventChannel.receiveBroadcastStream().listen(_onEvent, onError: _onError);
    super.initState();
  }

  void _onEvent(dynamic e) {
    print('loooooooooool recived something $e');
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        duration: Duration(seconds: 20),
        content: Container(
          child: Row(
            children: [
              Container(
                width: MediaQuery.of(context).size.width * 0.8,
                child: Text('notification event message $e', maxLines: 4),
              )
            ],
          ),
        )));
  }

  void _onError(dynamic e) {
    print('loooooooooool recived error -____- $e');
  }

  final platform = MethodChannel('com.example.app/notification');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Notification Example'),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _cancelNotification,
        child: Icon(Icons.stop),
      ),
      body: Center(
        child: ElevatedButton(
          onPressed: _showNotification,
          child: Text('Show Notification'),
        ),
      ),
    );
  }

  Future<void> _showNotification() async {
    String bodyTextBeforeStopWatchText = 'you have been studing for  \n';
    try {
      final notificationPermission =
          await handler.Permission.notification.request();
      if (notificationPermission.isDenied) {
        await handler.openAppSettings();
      }
      print(notificationPermission);
      final results = await platform.invokeMethod('showNotification',
          {'bodyText': bodyTextBeforeStopWatchText, 'logId': '125sdsa'});
      print(results);
    } catch (e, trace) {
      print('something went wrong $e $trace');
    }
  }

  Future<void> _cancelNotification() async {
    try {
      final results = await platform.invokeMethod('cancelNotification');
    } catch (e, trace) {
      print('something went wrong $e $trace');
    }
  }
}
