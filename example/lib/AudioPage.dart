import 'dart:async';

import 'package:audiole_example/helperclass.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:audiole/audiole.dart';
import 'package:flutter_xlider/flutter_xlider.dart';
import 'dart:math' as math;

import 'package:flutter/animation.dart';
import 'package:swipedetector/swipedetector.dart';

class AudioPage extends StatefulWidget {
  String audioUri;
  List<FolderInfo> folderinfo;
  int mediaIndex;

  AudioPage(this.audioUri, this.folderinfo, this.mediaIndex);

  @override
  _AudioPageState createState() => _AudioPageState(audioUri, folderinfo, mediaIndex);
}

class _AudioPageState extends State<AudioPage> {
  String audioUri, path;
  List<FolderInfo> folderinfo;
  int mediaIndex;

  _AudioPageState(path, folderinfo, index) {
    if (audioUri != "$path/${folderinfo[index].name}") playbuttonText = "Play";
    this.audioUri = "$path/${folderinfo[index].name}";
    this.folderinfo = folderinfo;
    mediaIndex = index;
    this.path = path;
  }

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
    if (mounted)
      setState(() {
        currentPosition = timer;

        if (currentPosition == duration) {
          setState(() {
            currentPosition = 0;
            playbuttonText = "Play";
            _disableTimer();

            nextTrack();

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
    print(audioUri + "====----");
  }

  checkPermission() async {
    if (!await Audiole.getPermission)
      setState(() {
        playbuttonText = "No Permission";
      });
    else {
      if (audioUri != "") {
        playButtonHandler(audioUri);
      }
    }
  }

  playButtonHandler(String audioUri) async {
    await Audiole.playAudiole(audioUri, playbuttonText).then((value) => {
          setState(() {
            Map<String, dynamic> map = Map.from(value);
            duration = map["duration"];
            if (playbuttonText == "Resume" || playbuttonText == "Play") {
              _enableTimer();
              if (playbuttonText == "Play") {
                artist = map["MEDIA_ARTIST"].toString().replaceAll('\n', ' ');
                ;
                trackname = map["MEDIA_TRACK"].toString().replaceAll('\n', ' ');
                title = map["MEDIA_TITLE"].toString().replaceAll('\n', ' ');
                album = map["MEDIA_ALBUM"].toString().replaceAll('\n', ' ');
                ;
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
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          SwipeDetector(
            child: embededImage != null
                ? Image.memory(
                    embededImage,
                    height: MediaQuery.of(context).size.width,
                  )
                : Center(
                    child: Icon(Icons.music_note_outlined, size: MediaQuery.of(context).size.width - 50),
                  ),
            onSwipeDown: () {
              Navigator.pop(context);
            },
            onSwipeLeft: () => nextTrack(),
            onSwipeRight: ()=>previousTrack(),
          ),
          SizedBox(
            height: 10,
          ),
          Text(
            title != null ? title : "Unknown",
            style: Theme.of(context).textTheme.headline4,
          ),
          SizedBox(
            height: 10,
          ),
          Text(
            artist != null ? artist : "Unknown",
            style: Theme.of(context).textTheme.headline5,
          ),
          SizedBox(
            height: 10,
          ),
          Text(
            album != null ? album : "Unknown",
            style: Theme.of(context).textTheme.bodyText1,
          ),
          SizedBox(
            height: 10,
          ),
          Text(
            trackname != null ? trackname : "Unknown",
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

  nextTrack() {
    if (folderinfo.length > mediaIndex+1 && !folderinfo[mediaIndex + 1].isDir&&
        folderinfo[mediaIndex + 1].name.contains('mp3')) {
      playbuttonText = "Play";
      playButtonHandler("$path/${folderinfo[mediaIndex + 1].name}");
      mediaIndex++;
    }
  }

  previousTrack() {
    if (mediaIndex > 0 && !folderinfo[mediaIndex - 1].isDir&&
        folderinfo[mediaIndex - 1].name.contains('mp3')) {
      playbuttonText = "Play";
      playButtonHandler("$path/${folderinfo[mediaIndex - 1].name}");
      mediaIndex--;
    }
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
            child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
              Expanded(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
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
                        handler: FlutterSliderHandler(
                            child: Icon(
                          Icons.stop_circle,
                          color: Colors.blueAccent,
                        )),
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
                          activeTrackBar: BoxDecoration(
                              borderRadius: BorderRadius.circular(20), color: Colors.blue.withOpacity(0.5)),
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
                    Padding(
                      child: ButtonBar(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          MaterialButton(
                            child: Icon(
                              Icons.arrow_back_ios_rounded,
                              size: 35,
                            ),
                            onPressed: () => previousTrack(),
                            color: Colors.white70,
                            padding: EdgeInsets.all(10),
                            shape: CircleBorder(),
                          ),
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
                            onPressed: () => playButtonHandler("$path/${folderinfo[mediaIndex].name}"),
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
                          MaterialButton(
                            child: Icon(
                              Icons.arrow_forward_ios_rounded,
                              size: 35,
                            ),
                            onPressed: () => nextTrack(),
                            color: Colors.white70,
                            //  textColor: Colors.white,

                            padding: EdgeInsets.all(10),
                            shape: CircleBorder(),
                          ),
                        ],
                      ),
                      padding: EdgeInsets.only(bottom: 0),
                    )
                  ],
                ),
              ),
            ])),
      ),
    );
  }
}
