import 'dart:math' as math;

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
  String playbuttonText, trackname, title, album, artist;
  var embededImage = null;
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
    //debugPrint("Timer $timer");

    setState(() {
      currentPosition = timer;

      if (currentPosition == duration) {
        setState(() {
          currentPosition = 0;
          playbuttonText = "Play";
          _disableTimer();
          print("I Stopped it " + playbuttonText + "  $currentPosition  $duration");
        });
      }
    });
  }

  @override
  void initState() {
    playbuttonText = "Play";
    trackname = "Unknown";
    title = "Unknown";
    album = "Unknown";
    artist = "Unknown";
    super.initState();
    checkPermission();
  }

  checkPermission() async {
    if (!await Audiole.getPermission)
      setState(() {
        playbuttonText = "No Permission";
      });
  }

  playButtonHandler() async {
    await Audiole.playAudiole("/storage/emulated/0/Music/Alan Walker - Sorry.mp3", playbuttonText).then((value) => {
          setState(() {
            Map<String, dynamic> map = Map.from(value);
            duration = map["duration"];
            if (playbuttonText == "Resume" || playbuttonText == "Play") {
              _enableTimer();
              if (playbuttonText == "Play") {
                artist = map["MEDIA_ARTIST"];
                trackname = map["MEDIA_TRACK"];
                title = map["MEDIA_TITLE"];
                album = map["MEDIA_ALBUM"];
                embededImage = map["MEDIA_ART"];
              }
            } else
              _disableTimer();
            playbuttonText = map["playstatus"];
          })
        });
  }

  Widget infoWidget() {
    if (playbuttonText != "Play")
      return Column(
        children: [
          if (embededImage != null)
            Image.memory(
              embededImage,
            ),
          SizedBox(
            height: 10,
          ),
          Text(
            title,
            style: Theme.of(context).textTheme.headline4,
          ),
          SizedBox(
            height: 10,
          ),
          Text(
            artist,
            style: Theme.of(context).textTheme.headline5,
          ),
          SizedBox(
            height: 10,
          ),
          Text(
            album,
            style: Theme.of(context).textTheme.bodyText1,
          ),
          SizedBox(
            height: 10,
          ),
          Text(
            trackname,
            style: Theme.of(context).textTheme.subtitle1,
          ),
          SizedBox(
            height: 10,
          ),
        ],
      );
    else
      return Container();
  }

  String musicTicker(int currentPosition) {
    String min, sec, minPrefex, secPrefix;
    if (currentPosition / 60 < 10)
      minPrefex = "0";
    else
      minPrefex = "";

    if (currentPosition % 60 < 10)
      secPrefix = "0";
    else
      secPrefix = "";

    return "$minPrefex${(currentPosition / 60).truncate()} : $secPrefix${currentPosition % 60}";
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: Center(
            child: Column(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
          infoWidget(),
          Center(
            child: Text(
              musicTicker(currentPosition),
              style: Theme.of(context).textTheme.display1,
            ),
          ),
          Container(
            child: FlutterSlider(
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
                inactiveTrackBarHeight: 25,
                activeTrackBarHeight: 20,
                inactiveTrackBar: BoxDecoration(
                  borderRadius: BorderRadius.circular(20),
                  color: Colors.white70,
                  border: Border.all(width: 3, color: Colors.blue),
                ),
                activeTrackBar:
                    BoxDecoration(borderRadius: BorderRadius.circular(20), color: Colors.blue.withOpacity(0.5)),
              ),
              tooltip: FlutterSliderTooltip(
                  textStyle: Theme.of(context).textTheme.display1,
                  format: (String value) {
                    return musicTicker(int.parse(value.substring(0, value.length - 2)));
                  }),
            ),
            height: 100,
            width: 400,
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              RaisedButton(
                  child: Transform(
                    alignment: Alignment.center,
                    transform: Matrix4.rotationY(math.pi),
                    child: Icon(Icons.double_arrow_rounded),
                  ),
                  onPressed: () {
                    if (currentPosition - 5 >= 0) Audiole.seekAudiole(currentPosition - 5);
                  },
                  shape: RoundedRectangleBorder(borderRadius: new BorderRadius.circular(30.0))),
              MaterialButton(
                child: playbuttonText == "Pause" ? Icon(Icons.pause) : Icon(Icons.play_arrow_outlined),
                onPressed: playButtonHandler,
                color: Colors.blue,
                textColor: Colors.white,

                padding: EdgeInsets.all(16),
                shape: CircleBorder(),
              ),
              RaisedButton(
                  shape: RoundedRectangleBorder(borderRadius: new BorderRadius.circular(30.0)),
                  child: Icon(Icons.double_arrow_rounded),
                  onPressed: () {
                    if (currentPosition + 5 <= duration) Audiole.seekAudiole(currentPosition + 5);
                  }),
            ],
          ),
          SizedBox(
            height: 50,
          )
        ])),
      ),
    );
  }
}
