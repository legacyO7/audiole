import 'package:flutter/services.dart';

enum BatteryState {
  /// The battery is completely full of energy.
  full,

  /// The battery is currently storing energy.
  charging,

  /// The battery is currently losing energy.
  discharging
}

class FlutterPluginLearning {
  factory FlutterPluginLearning() {
    if (_instance == null) {

      final EventChannel eventChannel =
      const EventChannel('com.legacy.audiole/stream');
      _instance = FlutterPluginLearning.init(eventChannel);
    }
    return _instance;
  }

  FlutterPluginLearning.init(this._eventChannel);

  static FlutterPluginLearning _instance;

  final EventChannel _eventChannel;
  Stream<BatteryState> _onBatteryStateChanged;

  Stream<BatteryState> get onBatteryStateChanged {
    if (_onBatteryStateChanged == null) {
      _onBatteryStateChanged = _eventChannel
          .receiveBroadcastStream()
          .map((dynamic event) => _parseBatteryState(event));
    }
    return _onBatteryStateChanged;
  }

  BatteryState _parseBatteryState(String state) {
    switch (state) {
      case 'full':
        return BatteryState.full;
      case 'charging':
        return BatteryState.charging;
      case 'discharging':
        return BatteryState.discharging;
      default:
        throw ArgumentError('$state is not a valid BatteryState.');
    }
  }
}