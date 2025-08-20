import PaxA920BarcodeScanner from './NativePaxA920BarcodeScanner';

export function initialize(): void {
  PaxA920BarcodeScanner.initScanner();
}

// Alias for convenience based on requested naming
export function init(): void {
  initialize();
}

export function finalize(): void {
  PaxA920BarcodeScanner.finalizeScanner();
}
