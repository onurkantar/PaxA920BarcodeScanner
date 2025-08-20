import PaxA920BarcodeScanner from './NativePaxA920BarcodeScanner';
import { NativeEventEmitter, NativeModules } from 'react-native';
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

const nativeEmitter = new NativeEventEmitter(
  NativeModules.PaxA920BarcodeScanner
);

export function addBarcodeSuccessListener(
  handler: (event: { data?: string | null }) => void
): EmitterSubscription {
  if (__DEV__) {
    // eslint-disable-next-line no-console
    console.log('[Library] adding success listener');
  }
  return nativeEmitter.addListener(BARCODE_READ_SUCCESS, handler);
}

export function addBarcodeFailListener(
  handler: (event: { error?: string | null }) => void
): EmitterSubscription {
  if (__DEV__) {
    // eslint-disable-next-line no-console
    console.log('[Library] adding fail listener');
  }
  return nativeEmitter.addListener(BARCODE_READ_FAIL, handler);
}
