import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:audiole/audiole.dart';
import 'package:flutter_xlider/flutter_xlider.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String playbuttonText;

  int currentPosition = 0,duration=0;
  StreamSubscription _timerSubscription;

  void _enableTimer() {
    if (_timerSubscription == null) {
      _timerSubscription = EventChannel('com.legacy.audiole/stream').receiveBroadcastStream().listen(_updateTimer);
    }
  }

  void _disableTimer() {
    if (_timerSubscription != null) {
      _timerSubscription.cancel();
      _timerSubscription = null;
    }
  }

  void _updateTimer(timer) {
    debugPrint("Timer $timer");
    setState(() => currentPosition = timer);
  }

  @override
  void initState() {
    playbuttonText = "Play";
    super.initState();
  }

  checkPermission() async {
    if (!await Audiole.getPermission)
      setState(() {
        playbuttonText = "No Permission";
      });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Audiole'),
        ),
        body: Center(
            child: Column(children: [
          RaisedButton(
            child: Text(playbuttonText),
            onPressed: () async {
              await Audiole.playAudiole(
                      "/storage/emulated/0/Music/Alan Walker - Sorry.mp3", playbuttonText)
                  .then((value) => {
                        setState(() {
                          Map<String, dynamic> map = Map.from(value);
                          playbuttonText = map["playstatus"];
                          duration=map["duration"];
                          if (playbuttonText == "Pause")
                            _enableTimer();
                          else
                            _disableTimer();
                        })
                      });
            },
          ),
          Center(
            child: Text(
              '$currentPosition',
              style: Theme.of(context).textTheme.display1,
            ),
          ),
          ButtonBar(
            children: <Widget>[
              FlatButton(
                child: const Text('Enable'),
                onPressed: _enableTimer,
              ),
              FlatButton(
                child: const Text('Disable'),
                onPressed: _disableTimer,
              ),
            ],
          ),
              FlutterSlider(
                values: [Duration(seconds: currentPosition).inMinutes.toDouble()],
                max: duration.toDouble(),
                min: 0,
              )
        ])),
      ),
    );
  }
}
