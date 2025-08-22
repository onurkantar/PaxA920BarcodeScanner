import PaxA920BarcodeScanner from './NativePaxA920BarcodeScanner';
import { NativeEventEmitter, DeviceEventEmitter, Platform } from 'react-native';
import type { EmitterSubscription } from 'react-native';

export function initialize(action?: string): void {
  const usedAction = action ?? 'com.barcode.sendBroadcast';
  if (__DEV__) {
    // Ensure Metro logs this call
    // eslint-disable-next-line no-console
    console.log('[Library] initialize called with action:', usedAction);
  }
  PaxA920BarcodeScanner.initScanner(usedAction);
}

// Alias for convenience based on requested naming
export function init(action?: string): void {
  initialize(action ?? 'com.barcode.sendBroadcast');
}

export function finalize(): void {
  PaxA920BarcodeScanner.finalizeScanner();
}

export const BARCODE_READ_SUCCESS = 'barcodeReadSuccess';
export const BARCODE_READ_FAIL = 'barcodeReadFail';

// On iOS, NativeEventEmitter must be passed the native module.
// On Android, DeviceEventEmitter receives events from RCTDeviceEventEmitter.
const iosEmitter =
  Platform.OS === 'ios'
    ? new NativeEventEmitter(PaxA920BarcodeScanner as any)
    : null;

// For Android, prefer DeviceEventEmitter but also create a module-bound emitter
// to support bridgeless/new-arch where NativeEventEmitter(module) may be required.
const androidModuleEmitter =
  Platform.OS === 'android'
    ? new NativeEventEmitter(PaxA920BarcodeScanner as any)
    : null;

// Simple dedup within a short window in case both emitters deliver the same event
const recentEventByName: Record<string, { signature: string; ts: number }> = {};
const shouldDeliver = (eventName: string, normalizedPayload: any): boolean => {
  try {
    const signature = JSON.stringify(normalizedPayload);
    const now = Date.now();
    const prev = recentEventByName[eventName];
    if (prev && prev.signature === signature && now - prev.ts < 100) {
      return false;
    }
    recentEventByName[eventName] = { signature, ts: now };
    return true;
  } catch {
    return true;
  }
};

export function addBarcodeSuccessListener(
  handler: (event: { data?: string | null }) => void
): EmitterSubscription {
  if (__DEV__) {
    // eslint-disable-next-line no-console
    console.log('[Library] adding success listener');
  }
  // Inform native module about subscription lifecycle
  // Notify native (best-effort; avoid throwing on platforms without implementation)
  if (typeof (PaxA920BarcodeScanner as any)?.addListener === 'function') {
    (PaxA920BarcodeScanner as any).addListener(BARCODE_READ_SUCCESS);
  }
  const normalize = (event: any): { data?: string | null } => {
    if (event == null) return { data: null };
    if (typeof event === 'string') return { data: event };
    if (typeof event === 'object') {
      if ('data' in event) return event as any;
      if ('value' in event) return { data: (event as any).value };
      if ('barcode' in event) return { data: (event as any).barcode };
      if ('barCode' in event) return { data: (event as any).barCode };
    }
    try {
      return { data: String(event) };
    } catch {
      return { data: null };
    }
  };

  if (Platform.OS === 'android') {
    console.log('[Library] android - setting up listeners');
    const deliver = (e: any) => {
      const payload = normalize(e);
      if (__DEV__) {
        // eslint-disable-next-line no-console
        console.log('[Library] 🎯 barcodeReadSuccess event received:', payload);
      }
      if (shouldDeliver(BARCODE_READ_SUCCESS, payload)) {
        console.log('[Library] 📤 Delivering to handler:', payload);
        handler(payload);
      } else {
        console.log('[Library] 🚫 Skipped duplicate event:', payload);
      }
    };

    console.log('[Library] Adding DeviceEventEmitter listener');
    const subA = DeviceEventEmitter.addListener(BARCODE_READ_SUCCESS, deliver);

    console.log('[Library] Adding NativeEventEmitter listener');
    const subB = androidModuleEmitter!.addListener(
      BARCODE_READ_SUCCESS,
      deliver
    );

    const combined: EmitterSubscription = {
      remove: () => {
        console.log('[Library] Removing Android listeners');
        subA.remove();
        subB.remove();
      },
    } as any;

    // Notify native
    if (typeof (PaxA920BarcodeScanner as any)?.addListener === 'function') {
      console.log('[Library] Notifying native addListener');
      (PaxA920BarcodeScanner as any).addListener(BARCODE_READ_SUCCESS);
    }

    console.log('[Library] Android listeners setup complete');
    return combined;
  }

  const sub = iosEmitter!.addListener(BARCODE_READ_SUCCESS, (e) => {
    const payload = normalize(e);
    if (__DEV__) {
      // eslint-disable-next-line no-console
      console.log('[Library] barcodeReadSuccess event (iOS):', payload);
    }
    handler(payload);
  });
  const originalRemove = sub.remove.bind(sub);
  (sub as any).remove = () => {
    if (typeof (PaxA920BarcodeScanner as any)?.removeListeners === 'function') {
      (PaxA920BarcodeScanner as any).removeListeners(1);
    }
    originalRemove();
  };
  return sub;
}

export function addBarcodeFailListener(
  handler: (event: { error?: string | null }) => void
): EmitterSubscription {
  if (__DEV__) {
    // eslint-disable-next-line no-console
    console.log('[Library] adding fail listener');
  }
  if (typeof (PaxA920BarcodeScanner as any)?.addListener === 'function') {
    (PaxA920BarcodeScanner as any).addListener(BARCODE_READ_FAIL);
  }
  const normalize = (event: any): { error?: string | null } => {
    if (event == null) return { error: null };
    if (typeof event === 'string') return { error: event };
    if (typeof event === 'object') {
      if ('error' in event) return event as any;
      if ('message' in event) return { error: (event as any).message };
    }
    try {
      return { error: String(event) };
    } catch {
      return { error: null };
    }
  };

  if (Platform.OS === 'android') {
    const deliver = (e: any) => {
      const payload = normalize(e);
      if (__DEV__) {
        // eslint-disable-next-line no-console
        console.log('[Library] barcodeReadFail event:', payload);
      }
      if (shouldDeliver(BARCODE_READ_FAIL, payload)) {
        handler(payload);
      }
    };
    const subA = DeviceEventEmitter.addListener(BARCODE_READ_FAIL, deliver);
    const subB = androidModuleEmitter!.addListener(BARCODE_READ_FAIL, deliver);
    const combined: EmitterSubscription = {
      remove: () => {
        subA.remove();
        subB.remove();
      },
    } as any;
    if (typeof (PaxA920BarcodeScanner as any)?.addListener === 'function') {
      (PaxA920BarcodeScanner as any).addListener(BARCODE_READ_FAIL);
    }
    return combined;
  }

  const sub = iosEmitter!.addListener(BARCODE_READ_FAIL, (e) => {
    const payload = normalize(e);
    if (__DEV__) {
      // eslint-disable-next-line no-console
      console.log('[Library] barcodeReadFail event (iOS):', payload);
    }
    handler(payload);
  });
  const originalRemove = sub.remove.bind(sub);
  (sub as any).remove = () => {
    if (typeof (PaxA920BarcodeScanner as any)?.removeListeners === 'function') {
      (PaxA920BarcodeScanner as any).removeListeners(1);
    }
    originalRemove();
  };
  return sub;
}
