import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'dynamic_app_icon_platform_interface.dart';

/// An implementation of [DynamicAppIconPlatform] that uses method channels.
class MethodChannelDynamicAppIcon extends DynamicAppIconPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('dynamic_app_icon');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<void> androidSetIcon(
      {required String icon, required List<String> listAvailableIcon}) async {
    Map<String, dynamic> data = {
      'icon': icon,
      'listAvailableIcon': listAvailableIcon
    };

    await methodChannel.invokeMethod<bool>('setIcon', data);
  }

  @override
  Future<void> androidSendBroadcast({required String action}) async {
    Map<String, dynamic> data = {
      'action': action,
    };

    await methodChannel.invokeMethod<bool>('sendBroadcast', data);
  }
}
