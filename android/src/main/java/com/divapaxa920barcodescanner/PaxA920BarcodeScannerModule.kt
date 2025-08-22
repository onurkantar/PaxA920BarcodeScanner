package com.divapaxa920barcodescanner

import android.content.IntentFilter
import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.modules.core.RCTNativeAppEventEmitter
import com.facebook.react.module.annotations.ReactModule
import com.pax.dal.IDAL
import com.pax.neptunelite.api.NeptuneLiteUser

@ReactModule(name = PaxA920BarcodeScannerModule.NAME)
class PaxA920BarcodeScannerModule(reactContext: ReactApplicationContext) :
  NativePaxA920BarcodeScannerSpec(reactContext) {
  init {
    // Ensure static context is available as soon as the module is constructed
    setReactContext(reactContext)
    // Also set context for the receiver
    PaxA920BarcodeScannerReceiver.setReactContext(reactContext)
  }

  private var barcodeReceiver: PaxA920BarcodeScannerReceiver? = null
  private var registeredAction: String? = null
  private var idal: IDAL? = null

  override fun getName(): String {
    return NAME
  }

  override fun initScanner(action: String?) {
    val actionToUse: String = (action?.takeIf { it.isNotBlank() }) ?: DEFAULT_ACTION
    Log.d(TAG, "=== initScanner called ===")
    Log.d(TAG, "Input action: '$action' -> using '$actionToUse'")
    Log.d(TAG, "Current registered action: $registeredAction")
    Log.d(TAG, "Current receiver: $barcodeReceiver")

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
      Log.d(TAG, "IDAL scan result mode set to broadcast (1)")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to initialize IDAL: ${e.message}")
    }

    // Ensure context is set before any broadcast arrives
    setReactContext(reactApplicationContext)
    PaxA920BarcodeScannerReceiver.setReactContext(reactApplicationContext)

    val receiver = PaxA920BarcodeScannerReceiver()
    // Register for both user's action and default action as a fallback
    val filter = IntentFilter().apply {
      addAction(actionToUse)
      if (actionToUse != DEFAULT_ACTION) addAction(DEFAULT_ACTION)
    }
    Log.d(TAG, "Registering BroadcastReceiver with filter: actions=${listOfNotNull(actionToUse, if (actionToUse != DEFAULT_ACTION) DEFAULT_ACTION else null)}")
    try {
      reactApplicationContext.registerReceiver(receiver, filter)
      barcodeReceiver = receiver
      registeredAction = actionToUse
      val actions = if (actionToUse == DEFAULT_ACTION) listOf(DEFAULT_ACTION) else listOf(actionToUse, DEFAULT_ACTION)
      Log.i(TAG, "SUCCESS: Receiver registered for actions: ${actions.joinToString(", ")}")
    } catch (e: Exception) {
      Log.e(TAG, "FAILED: Could not register receiver: ${e.message}", e)
      throw e
    }
  }

  override fun finalizeScanner() {
    Log.d(TAG, "finalizeScanner called")
    // Switch off broadcast scan result mode
    try {
      idal?.sys?.setScanResultMode(0)
      Log.d(TAG, "IDAL scan result mode set to input (0)")
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

  // Allows NativeEventEmitter to notify native side when JS subscribes/unsubscribes
  override fun addListener(eventName: String) {
    Log.d(TAG, "=== addListener called ===")
    Log.d(TAG, "Event: $eventName")
    Log.d(TAG, "React context active: ${reactApplicationContext.hasActiveCatalystInstance()}")
    // Flush any pending events when JS starts listening
    PaxA920BarcodeScannerReceiver.flush()
  }

  override fun removeListeners(count: Double) {
    Log.d(TAG, "removeListeners called, count=$count")
  }

  companion object {
    const val NAME = "PaxA920BarcodeScanner"
    private const val TAG = "PaxA920Scanner"
    private const val DEFAULT_ACTION = "com.barcode.sendBroadcast"

    @Volatile
    private var reactContextRef: ReactApplicationContext? = null

    fun setReactContext(context: ReactApplicationContext) {
      reactContextRef = context
      // Also update receiver context
      PaxA920BarcodeScannerReceiver.setReactContext(context)
    }

    fun sendEvent(eventName: String, params: WritableMap) {
      Log.d(TAG, "=== sendEvent called ===")
      Log.d(TAG, "Event: $eventName, params: $params")
      
      val ctx = reactContextRef
      if (ctx == null) {
        Log.e(TAG, "sendEvent: No react context")
        return
      }
      
      Log.d(TAG, "Context type: ${ctx.javaClass.simpleName}")
      Log.d(TAG, "Has active catalyst: ${ctx.hasActiveCatalystInstance()}")
      
      // Try DeviceEventManagerModule first
      try {
        ctx.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
          .emit(eventName, params)
        Log.i(TAG, "sendEvent: DeviceEventManagerModule emit SUCCESS")
      } catch (t: Throwable) {
        Log.w(TAG, "sendEvent: DeviceEventManagerModule failed: ${t.message}")
      }
      
      // Try RCTNativeAppEventEmitter for NativeEventEmitter compatibility
      try {
        ctx.getJSModule(RCTNativeAppEventEmitter::class.java)
          .emit(eventName, params)
        Log.i(TAG, "sendEvent: RCTNativeAppEventEmitter emit SUCCESS")
      } catch (t: Throwable) {
        Log.w(TAG, "sendEvent: RCTNativeAppEventEmitter failed: ${t.message}")
      }
    }
  }
}
