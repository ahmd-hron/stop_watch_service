import 'package:flutter/material.dart';

class NotificationPage extends StatelessWidget {
  final String? id;
  const NotificationPage({super.key, this.id});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(id ?? ''),
      ),
    );
  }
}
