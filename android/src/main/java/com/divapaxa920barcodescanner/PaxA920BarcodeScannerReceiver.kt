package com.divapaxa920barcodescanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.lang.ref.WeakReference
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentLinkedQueue

class PaxA920BarcodeScannerReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context?, intent: Intent?) {
    Log.d(TAG, "=== onReceive called ===")
    Log.d(TAG, "Context: $context")
    Log.d(TAG, "Intent: $intent")
    Log.d(TAG, "Intent action: ${intent?.action}")
    Log.d(TAG, "Intent extras: ${intent?.extras}")
    
    if (context == null || intent == null) {
      Log.e(TAG, "Context or intent is null!")
      emit(BARCODE_READ_FAIL, mapOf("error" to "Null intent/context"))
      return
    }

    val barcode = extractBarcode(intent)
    Log.d(TAG, "Extracted barcode: '$barcode'")
    
    if (barcode.isNullOrBlank()) {
      Log.w(TAG, "No barcode found in intent")
      emit(BARCODE_READ_FAIL, mapOf("error" to "No barcode in intent"))
    } else {
      Log.d(TAG, "Found barcode: '$barcode', emitting success")
      emit(BARCODE_READ_SUCCESS, mapOf("data" to barcode))
    }
  }

  private fun extractBarcode(intent: Intent): String? {
    val keys = listOf(
      BARCODE_EXTRA_KEY, "SCAN_BARCODE1", "SCAN_BARCODE", "barcode", "barCode",
      "data", "value", "com.symbol.datawedge.data_string", Intent.EXTRA_TEXT
    )

    // Try known keys
    for (k in keys) {
      try {
        val v = intent.getStringExtra(k)
        if (!v.isNullOrEmpty()) return v
      } catch (_: Throwable) { }
    }

    // Fallback: scan all extras
    val extras = intent.extras ?: return intent.dataString
    for (k in extras.keySet()) {
      val v = extras.get(k)
      when (v) {
        is String -> if (v.isNotEmpty()) return v
        is ByteArray -> try {
          val s = String(v, StandardCharsets.UTF_8)
          if (s.isNotEmpty()) return s
        } catch (_: Throwable) { }
        else -> v?.toString()?.let { if (it.isNotEmpty()) return it }
      }
    }
    return intent.dataString
  }

  private fun emit(event: String, data: Map<String, String>) {
    Log.d(TAG, "=== emit() called ===")
    Log.d(TAG, "Event: $event")
    Log.d(TAG, "Data: $data")
    
    val params: WritableMap = Arguments.createMap().apply {
      data.forEach { (k, v) -> putString(k, v) }
    }
    Log.d(TAG, "Created params: $params")

    val ctx = reactContextRef.get()
    Log.d(TAG, "React context: $ctx")
    
    val ready = ctx != null && ctx.hasActiveCatalystInstance()
    Log.d(TAG, "React ready: $ready (context=$ctx, hasActiveCatalyst=${ctx?.hasActiveCatalystInstance()})")
    
    if (!ready) {
      pending.add(event to params)
      Log.w(TAG, "Queued $event. React not ready. Queue size: ${pending.size}")
      return
    }

    // Try multiple emission paths for bridgeless compatibility
    var emitted = false
    
    // Path 1: DeviceEventManagerModule (old bridge)
    try {
      Log.d(TAG, "Attempting to emit via DeviceEventManagerModule...")
      ctx!!
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        .emit(event, params)
      Log.d(TAG, "DeviceEventManagerModule emit succeeded")
      emitted = true
    } catch (t: Throwable) {
      Log.w(TAG, "DeviceEventManagerModule emit failed: ${t.message}")
    }
    
    // Path 2: Send via module helper with fresh params (bridgeless compatible)
    try {
      Log.d(TAG, "Attempting to emit via module sendEvent...")
      val freshParams: WritableMap = Arguments.createMap().apply {
        data.forEach { (k, v) -> putString(k, v) }
      }
      PaxA920BarcodeScannerModule.sendEvent(event, freshParams)
      Log.d(TAG, "Module sendEvent succeeded")
      emitted = true
    } catch (t: Throwable) {
      Log.w(TAG, "Module sendEvent failed: ${t.message}")
    }
    
    if (emitted) {
      Log.i(TAG, "SUCCESS: Emitted $event to JS with data: $data")
    } else {
      pending.add(event to params)
      Log.e(TAG, "FAILED: All emit paths failed, queued event")
    }
  }

  companion object {
    private const val TAG = "PaxA920BarcodeRx"
    const val BARCODE_READ_SUCCESS = "barcodeReadSuccess"
    const val BARCODE_READ_FAIL = "barcodeReadFail"
    const val BARCODE_EXTRA_KEY = "BARCODE"

    private var reactContextRef: WeakReference<ReactApplicationContext> = WeakReference(null)
    private val pending = ConcurrentLinkedQueue<Pair<String, WritableMap>>()

    // Call this from your RN module's init
    fun setReactContext(ctx: ReactApplicationContext) {
      Log.d(TAG, "=== setReactContext called ===")
      Log.d(TAG, "New context: $ctx")
      Log.d(TAG, "Context has active catalyst: ${ctx.hasActiveCatalystInstance()}")
      reactContextRef = WeakReference(ctx)
      flush()
    }

    // Call on host resume as well
    fun flush() {
      Log.d(TAG, "=== flush() called ===")
      val ctx = reactContextRef.get()
      Log.d(TAG, "Context: $ctx")
      if (ctx == null) {
        Log.w(TAG, "No context for flush, pending size: ${pending.size}")
        return
      }
      
      if (!ctx.hasActiveCatalystInstance()) {
        Log.w(TAG, "No active catalyst instance for flush, pending size: ${pending.size}")
        return
      }
      
      Log.d(TAG, "Flushing ${pending.size} pending events")
      var flushed = 0
      while (true) {
        val item = pending.poll() ?: break
        try {
          ctx.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(item.first, item.second)
          flushed++
          Log.d(TAG, "Flushed event: ${item.first}")
        } catch (t: Throwable) {
          Log.e(TAG, "Flush emit failed: ${t.message}", t)
          pending.add(item)
          break
        }
      }
      Log.i(TAG, "Flush complete. Flushed: $flushed, remaining: ${pending.size}")
    }
  }
}
