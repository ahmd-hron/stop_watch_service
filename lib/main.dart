import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart' as handler;
import 'package:test_native_notfications/notification_page.dart';
import 'package:uni_links/uni_links.dart';

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
    initUniLinks();
    eventChannel.receiveBroadcastStream().listen(_onEvent, onError: _onError);
    super.initState();
  }

  void initUniLinks() async {
    String? initialLink;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      initialLink = await getInitialLink();
      if (initialLink != null) {
        // Parse the link and warn the user, if it is not correct,
        // but keep in mind it could be `null`.
        print('Initial link: $initialLink');
      }
    } on PlatformException {
      initialLink = 'error occured';
    }
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        duration: Duration(seconds: 20),
        content: Container(
          child: Row(
            children: [
              Container(
                width: MediaQuery.of(context).size.width * 0.8,
                child: Text(initialLink ?? 'initial link is null', maxLines: 4),
              )
            ],
          ),
        )));
  }

  void _onEvent(dynamic e) {
    print('loooooooooool recived something $e');
    print(e.runtimeType);
    if (e is Map && e['action'] != null) {
      switch (e['action']) {
        case 'Notification tapped':
          pushNotificationPage(e['logId']);
          break;
        case ('CancelActionReceiver'):
          print('recived a cancel action $e');
          break;
        case ('StopActionReceiver'):
          print('recived a stop action ');
        default:
          print('recived an action of type ${e['action']}');
      }
    }

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

  void pushNotificationPage(String id) {
    Navigator.of(context).push(
        MaterialPageRoute(builder: (context) => NotificationPage(id: id)));
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
