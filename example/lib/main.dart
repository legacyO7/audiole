
import 'package:audiole_example/AudioPage.dart';
import 'package:audiole_example/FileManager.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';


void main() {
  WidgetsFlutterBinding.ensureInitialized();
  SystemChrome.setPreferredOrientations([
    DeviceOrientation.portraitUp,
    DeviceOrientation.portraitDown
  ]).then((_) async {
    //runApp(AudioPage());
    runApp(Audiole());
  });

}

class Audiole extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: FileManager(),
    );
  }}

