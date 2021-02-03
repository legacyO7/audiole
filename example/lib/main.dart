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

  Future<Image> assetThumbToImage(ByteData byteData) async {
    final Image image = Image.memory(byteData.buffer.asUint8List());
    return image;
  }

  @override
  void initState() {
    playbuttonText = "Play";
    playbuttonText = "Unknown";
    trackname = "Unknown";
    title = "Unknown";
    album = "Unknown";
    artist = "Unknown";
    super.initState();
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
            playbuttonText = map["playstatus"];
            duration = map["duration"];
            if (playbuttonText.startsWith('P')) {
              _enableTimer();
              if (playbuttonText == "Play") {
                artist = map["MEDIA_ARTIST"];
                trackname = map["MEDIA_TRACK"];
                title = map["MEDIA_TITLE"];
                album = map["MEDIA_ALBUM"];
                embededImage = map["MEDIA_ART"];
              }
              print(embededImage);
            } else
              _disableTimer();
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

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: Center(
            child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
          infoWidget(),
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
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              RaisedButton(
                  child: Transform(
                    alignment: Alignment.center,
                    transform: Matrix4.rotationY(math.pi),
                    child: Icon(Icons.double_arrow_rounded),
                  ),
                  onPressed: () {
                    Audiole.seekAudiole(currentPosition - 5);
                  }),
              RaisedButton(child: Text(playbuttonText), onPressed: playButtonHandler),
              RaisedButton(
                  child: Icon(Icons.double_arrow_rounded),
                  onPressed: () {
                    Audiole.seekAudiole(currentPosition + 5);
                  }),
            ],
          )
        ])),
      ),
    );
  }
}
