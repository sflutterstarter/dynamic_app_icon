package com.zhaojz.sflutterstarter.dynamic_app_icon

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startActivity
import android.util.Log

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.lang.ref.WeakReference

/** DynamicAppIconPlugin */
class DynamicAppIconPlugin: FlutterPlugin, ActivityAware, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel

  private val activity get() = activityReference.get()
  private val applicationContext get() =
    contextReference.get() ?: activity?.applicationContext

  private var activityReference = WeakReference<Activity>(null)
  private var contextReference = WeakReference<Context>(null)

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
        "getPlatformVersion" -> {
          result.success("Android ${android.os.Build.VERSION.RELEASE}")
        }
        "setIcon" -> {
          try {
            val icon = call.argument<String>("icon")
            val listIcon = call.argument<List<String>>("listAvailableIcon")
            if (listIcon != null && icon != null) {
              setIcon(icon, listIcon)
            }
            result.success(true)
          } catch (e: Exception) {
            e.printStackTrace()
            result.error("1", e.message, e)
          }
        }
        else -> {
          result.notImplemented()
        }
    }
  }


  //dynamically change app icon
  private fun setIcon(targetIcon: String, activitiesArray: List<String>) {
    val packageManager: PackageManager = applicationContext!!.packageManager
    val packageName = applicationContext!!.packageName
    val className = StringBuilder()
    className.append(packageName)
    className.append(".")
    className.append(targetIcon)

    Log.i("calendar_app_dbg", "setIcon start")

    var restartActivity = false;
    
    for (value in activitiesArray) {
        val action = if (value == targetIcon) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        val oldAction = packageManager.getComponentEnabledSetting(ComponentName(packageName!!, "$packageName.$value"))
        if (oldAction == action) {
            continue
        }

        Log.i("calendar_app_dbg", "$value:$oldAction => $action")
        packageManager.setComponentEnabledSetting(
            ComponentName(packageName, "$packageName.$value"),
            action, PackageManager.DONT_KILL_APP
        )

        if (value == targetIcon) {
          restartActivity = true;
        }
    }


    //finish current activity & launch new intent to prevent app from killing itself!
    //check if android version is greater than 8
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && restartActivity) {
      Log.i("calendar_app_dbg", "setIcon restart activity")

      val intent = Intent()
      intent.setClassName(packageName!!, className.toString())
      intent.action = Intent.ACTION_MAIN
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
              Intent.FLAG_ACTIVITY_CLEAR_TASK
      this.activity?.finish()
      startActivity(this.applicationContext!!, intent, null)
    }

    Log.i("calendar_app_dbg", "setIcon finish")
  }

  //
  //
  //


  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "dynamic_app_icon")
    channel.setMethodCallHandler(this)

    contextReference = WeakReference(flutterPluginBinding.applicationContext)
  }


  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activityReference = WeakReference(binding.activity)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    activityReference.clear()
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activityReference = WeakReference(binding.activity)
  }

  override fun onDetachedFromActivity() {
    activityReference.clear()
  }
}
