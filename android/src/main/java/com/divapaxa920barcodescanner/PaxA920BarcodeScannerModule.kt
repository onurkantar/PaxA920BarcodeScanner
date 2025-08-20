package com.divapaxa920barcodescanner

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = PaxA920BarcodeScannerModule.NAME)
class PaxA920BarcodeScannerModule(reactContext: ReactApplicationContext) :
  NativePaxA920BarcodeScannerSpec(reactContext) {

  override fun getName(): String {
    return NAME
  }

  override fun initScanner() {
    // TODO: Initialize scanner resources
  }

  override fun finalizeScanner() {
    // TODO: Release scanner resources
  }

  companion object {
    const val NAME = "PaxA920BarcodeScanner"
  }
}
