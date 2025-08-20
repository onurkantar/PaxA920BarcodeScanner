package com.divapaxa920barcodescanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule

class PaxA920BarcodeScannerReceiver(
  private val reactContext: ReactApplicationContext
) : BroadcastReceiver() {

  override fun onReceive(context: Context?, intent: Intent?) {
    if (intent == null || context == null) {
      Log.e(TAG, "Intent or context is null")
      emitFailure("Intent or context is null")
      return
    }

    try {
      Log.d(TAG, "onReceive invoked with contextHasCatalyst=${reactContext.hasActiveCatalystInstance()} and intent=${intent}")
      val barcode: String? = extractBarcode(intent)
      Log.d(TAG, "Received action: ${intent.action}")
      Log.d(TAG, "Barcode: $barcode")

      val params: WritableMap = Arguments.createMap()
      params.putString("data", barcode)
      emitSuccess(params)
    } catch (e: Exception) {
      Log.e(TAG, "Error in onReceive: ${e.message}")
      emitFailure(e.message ?: "Unknown error")
    }
  }

  private fun extractBarcode(intent: Intent): String? {
    val candidateKeys = listOf(
      BARCODE_EXTRA_KEY,
      "SCAN_BARCODE1",
      "SCAN_BARCODE",
      "barcode",
      "barCode",
      "data",
      "value",
      "com.symbol.datawedge.data_string",
      Intent.EXTRA_TEXT
    )
    for (key in candidateKeys) {
      try {
        val value = intent.getStringExtra(key)
        if (!value.isNullOrEmpty()) return value
      } catch (_: Exception) {
      }
    }
    try {
      val extras = intent.extras
      if (extras != null) {
        for (key in extras.keySet()) {
          val anyVal = extras.get(key)
          when (anyVal) {
            is String -> if (anyVal.isNotEmpty()) return anyVal
            is ByteArray -> try {
              val s = String(anyVal)
              if (s.isNotEmpty()) return s
            } catch (_: Exception) {}
            else -> {
              val s = anyVal?.toString()
              if (!s.isNullOrEmpty()) return s
            }
          }
        }
      }
    } catch (_: Exception) {
    }
    return intent.dataString
  }

  private fun emitSuccess(params: WritableMap) {
    try {
      if (!reactContext.hasActiveCatalystInstance()) {
        Log.w(TAG, "No active React instance; dropping success event: $params")
        return
      }
      Log.d(TAG, "Emitting JS event: $BARCODE_READ_SUCCESS -> $params")
      reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        .emit(BARCODE_READ_SUCCESS, params)
    } catch (t: Throwable) {
      Log.e(TAG, "emitSuccess failed: ${t.message}")
    }
  }

  private fun emitFailure(errorMessage: String) {
    val params: WritableMap = Arguments.createMap()
    params.putString("error", errorMessage)
    try {
      if (!reactContext.hasActiveCatalystInstance()) {
        Log.w(TAG, "No active React instance; dropping failure event: $params")
        return
      }
      Log.d(TAG, "Emitting JS event: $BARCODE_READ_FAIL -> $params")
      reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        .emit(BARCODE_READ_FAIL, params)
    } catch (t: Throwable) {
      Log.e(TAG, "emitFailure failed: ${t.message}")
    }
  }

  companion object {
    private const val TAG = "PaxA920BarcodeRx"
    const val BARCODE_READ_SUCCESS = "barcodeReadSuccess"
    const val BARCODE_READ_FAIL = "barcodeReadFail"
    const val BARCODE_EXTRA_KEY = "BARCODE"
  }
}


