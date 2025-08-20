package com.divapaxa920barcodescanner

import android.content.IntentFilter
import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule
import com.pax.dal.IDAL
import com.pax.neptunelite.api.NeptuneLiteUser

@ReactModule(name = PaxA920BarcodeScannerModule.NAME)
class PaxA920BarcodeScannerModule(reactContext: ReactApplicationContext) :
  NativePaxA920BarcodeScannerSpec(reactContext) {

  private var barcodeReceiver: PaxA920BarcodeScannerReceiver? = null
  private var registeredAction: String? = null
  private var idal: IDAL? = null

  override fun getName(): String {
    return NAME
  }

  override fun initScanner(action: String?) {
    val actionToUse: String = (action?.takeIf { it.isNotBlank() }) ?: DEFAULT_ACTION

    // If already registered with the same action, do nothing
    if (registeredAction == actionToUse && barcodeReceiver != null) {
      return
    }

    // Unregister any previous receiver
    try {
      barcodeReceiver?.let { reactApplicationContext.unregisterReceiver(it) }
    } catch (_: IllegalArgumentException) {
      // Ignore if not registered
    }

    // Initialize PAX IDAL and set scan result to broadcast mode
    try {
      if (idal == null) {
        idal = NeptuneLiteUser.getInstance().getDal(reactApplicationContext)
      }
      idal?.sys?.setScanResultMode(1)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to initialize IDAL: ${e.message}")
    }

    val receiver = PaxA920BarcodeScannerReceiver(reactApplicationContext)
    val filter = IntentFilter(actionToUse)
    reactApplicationContext.registerReceiver(receiver, filter)
    barcodeReceiver = receiver
    registeredAction = actionToUse
  }

  override fun finalizeScanner() {
    // Switch off broadcast scan result mode
    try {
      idal?.sys?.setScanResultMode(0)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to finalize IDAL: ${e.message}")
    }

    try {
      barcodeReceiver?.let { reactApplicationContext.unregisterReceiver(it) }
    } catch (_: IllegalArgumentException) {
      // Ignore if not registered
    } finally {
      barcodeReceiver = null
      registeredAction = null
    }
  }

  companion object {
    const val NAME = "PaxA920BarcodeScanner"
    private const val TAG = "PaxA920Scanner"
    private const val DEFAULT_ACTION = "com.barcode.sendBroadcast"
  }
}
