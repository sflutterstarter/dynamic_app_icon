import 'dynamic_app_icon_platform_interface.dart';

class DynamicAppIcon {
  Future<String?> getPlatformVersion() {
    return DynamicAppIconPlatform.instance.getPlatformVersion();
  }

  Future<void> androidSetIcon(
      {required String icon, required List<String> listAvailableIcon}) {
    return DynamicAppIconPlatform.instance
        .androidSetIcon(icon: icon, listAvailableIcon: listAvailableIcon);
  }

  Future<void> androidSendBroadcast({required String action}) {
    return DynamicAppIconPlatform.instance.androidSendBroadcast(action: action);
  }
}
