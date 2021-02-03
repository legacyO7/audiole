import 'dart:convert';
import 'dart:io';

import 'package:flutter/animation.dart';
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

  int currentPosition = 0, duration = 0;
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
    if (currentPosition == duration) {
      if (_timerSubscription != null) _disableTimer();

      setState(() {
        currentPosition = 0;
        playbuttonText = "Play";
      });
      print("I Stopped it " + playbuttonText + "  $currentPosition  $duration");

    } else if (currentPosition == duration) {}
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
            child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
          RaisedButton(
            child: Text(playbuttonText),
            onPressed: () async {
              await Audiole.playAudiole("/storage/emulated/0/Music/Alan Walker - Sorry.mp3", playbuttonText)
                  .then((value) => {
                        setState(() {
                          Map<String, dynamic> map = Map.from(value);
                          playbuttonText = map["playstatus"];
                          duration = map["duration"];
                          if (playbuttonText.startsWith('P'))
                            _enableTimer();
                          else
                            _disableTimer();
                        })
                      });
            },
          ),
          Center(
            child: Text(
              '${(currentPosition / 60).truncate()} : ${currentPosition % 60}',
              style: Theme.of(context).textTheme.display1,
            ),
          ),
          FlutterSlider(
            values: [currentPosition.toDouble()],
            max: duration.toDouble(),
            min: 0,
            onDragCompleted: (handlerIndex, lowerValue, upperValue) async {
              print(lowerValue);
              if ((lowerValue).toInt() != null) await Audiole.seekAudiole((lowerValue).toInt());
            },
            handlerAnimation: FlutterSliderHandlerAnimation(
                curve: Curves.elasticOut,
                reverseCurve: Curves.bounceIn,
                duration: Duration(milliseconds: 500),
                scale: 1.5),
            trackBar: FlutterSliderTrackBar(
              inactiveTrackBar: BoxDecoration(
                borderRadius: BorderRadius.circular(20),
                color: Colors.black12,
                border: Border.all(width: 3, color: Colors.blue),
              ),
              activeTrackBar:
                  BoxDecoration(borderRadius: BorderRadius.circular(4), color: Colors.blue.withOpacity(0.5)),
            ),
            tooltip: FlutterSliderTooltip(
                textStyle: Theme.of(context).textTheme.display1,
                format: (String value) {
                  String position = value.substring(0, value.length - 2);
                  return "${(int.parse(position) / 60).truncate()} :  ${int.parse(position) % 60} ";
                }),
          )
        ])),
      ),
    );
  }
}
