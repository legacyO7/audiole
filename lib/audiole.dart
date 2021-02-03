
import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

class Audiole {
  static const MethodChannel _channel =
      const MethodChannel('audiole');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<dynamic> playAudiole(audioUri,playstatus) async {
    print("Sent Call : { uri: $audioUri,playstatus: $playstatus}");
    return (await _channel.invokeMethod('play',{"uri":audioUri,"playstatus":playstatus}));
  }

 static Future<bool> get getPermission async {
    var status = await Permission.storage.status;
    if (status.isUndetermined||status.isDenied) {
      await Permission.storage.request();
    }
    return Permission.storage.isGranted;
  }

  static Future<String> get stopAudiole async {
    final String version = await _channel.invokeMethod('stop');
    return version;
  }
}
