package com.divapaxa920barcodescanner

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = PaxA920BarcodeScannerModule.NAME)
class PaxA920BarcodeScannerModule(reactContext: ReactApplicationContext) :
  NativePaxA920BarcodeScannerSpec(reactContext) {

  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  companion object {
    const val NAME = "PaxA920BarcodeScanner"
  }
}
