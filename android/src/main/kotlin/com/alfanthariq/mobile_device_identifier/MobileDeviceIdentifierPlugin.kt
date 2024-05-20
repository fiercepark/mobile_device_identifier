package com.alfanthariq.mobile_device_identifier

import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

import android.media.MediaDrm
import java.util.UUID

class MobileDeviceIdentifierPlugin: FlutterPlugin, MethodCallHandler {
  private lateinit var channel : MethodChannel

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "mobile_device_identifier")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getDeviceId") {
      val devId = getDeviceId()
      result.success(devId)
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  private fun getDeviceId(): String? {
      val wideVineUuid = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
      return try {
          val wvDrm = MediaDrm(wideVineUuid)
          val wideVineId = wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
          wideVineId.joinToString(":") { String.format("%02X", it) }

          val macAddress = getMacAddress()
          "$wideVineIdStr::$macAddress"
      } catch (e: java.lang.Exception) {
          null
      }
  }

  private fun getMacAddress(): String {
    try {
        val all: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
        for (nif in all) {
            if (!nif.name.equals("wlan0", ignoreCase = true)) continue
            val macBytes = nif.hardwareAddress ?: return ""
            val res1 = StringBuilder()
            for (b in macBytes) {
                res1.append(String.format("%02X:", b))
            }
            if (res1.isNotEmpty()) {
                res1.deleteCharAt(res1.length - 1)
            }
            return res1.toString()
        }
    } catch (ex: Exception) {
        // Handle exceptions
    }
    return "02:00:00:00:00:00" // Default MAC address if none found
  }
}
