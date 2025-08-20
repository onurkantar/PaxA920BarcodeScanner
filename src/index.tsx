import PaxA920BarcodeScanner from './NativePaxA920BarcodeScanner';
import { DeviceEventEmitter } from 'react-native';
import type { EmitterSubscription } from 'react-native';

export function initialize(action?: string): void {
  PaxA920BarcodeScanner.initScanner(action ?? 'com.barcode.sendBroadcast');
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

export function addBarcodeSuccessListener(
  handler: (event: { data?: string | null }) => void
): EmitterSubscription {
  return DeviceEventEmitter.addListener(BARCODE_READ_SUCCESS, handler);
}

export function addBarcodeFailListener(
  handler: (event: { error?: string | null }) => void
): EmitterSubscription {
  return DeviceEventEmitter.addListener(BARCODE_READ_FAIL, handler);
}
