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
      val barcode: String? = intent.getStringExtra(BARCODE_EXTRA_KEY)
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

  private fun emitSuccess(params: WritableMap) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(BARCODE_READ_SUCCESS, params)
  }

  private fun emitFailure(errorMessage: String) {
    val params: WritableMap = Arguments.createMap()
    params.putString("error", errorMessage)
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(BARCODE_READ_FAIL, params)
  }

  companion object {
    private const val TAG = "PaxA920BarcodeRx"
    const val BARCODE_READ_SUCCESS = "barcodeReadSuccess"
    const val BARCODE_READ_FAIL = "barcodeReadFail"
    const val BARCODE_EXTRA_KEY = "BARCODE"
  }
}


