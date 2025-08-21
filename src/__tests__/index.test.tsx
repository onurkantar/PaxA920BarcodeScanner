/*
  Jest tests for the public JS API. We mock react-native to simulate
  NativeModules and NativeEventEmitter behavior so consumers of this
  module can subscribe to events and call initialize/finalize.
*/

jest.mock('react-native', () => {
  const handlers: Record<string, Array<(payload: any) => void>> = {};

  const PaxA920BarcodeScanner = {
    initScanner: jest.fn(),
    finalizeScanner: jest.fn(),
    addListener: jest.fn(),
    removeListeners: jest.fn(),
  };

  class NativeEventEmitter {
    nativeModule: any;
    constructor(nativeModule: any) {
      this.nativeModule = nativeModule;
    }
    addListener(eventName: string, handler: (payload: any) => void) {
      PaxA920BarcodeScanner.addListener(eventName);
      handlers[eventName] = handlers[eventName] || [];
      handlers[eventName].push(handler);
      return { remove: () => void 0 } as any;
    }
    static __emit(eventName: string, payload: any) {
      (handlers[eventName] || []).forEach((h) => h(payload));
    }
  }

  const TurboModuleRegistry = {
    getEnforcing: <T,>(name: string): T => {
      if (name !== 'PaxA920BarcodeScanner') {
        throw new Error('Unknown module: ' + name);
      }
      return PaxA920BarcodeScanner as unknown as T;
    },
  };

  return {
    NativeModules: { PaxA920BarcodeScanner },
    NativeEventEmitter,
    TurboModuleRegistry,
  };
});

describe('PaxA920BarcodeScanner JS API', () => {
  const RN = require('react-native');
  const {
    initialize,
    init,
    finalize,
    addBarcodeSuccessListener,
    addBarcodeFailListener,
  } = require('..');

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('initialize uses default action when none provided', () => {
    initialize();
    expect(
      RN.NativeModules.PaxA920BarcodeScanner.initScanner
    ).toHaveBeenCalledWith('com.barcode.sendBroadcast');
  });

  test('initialize forwards provided action', () => {
    initialize('custom.ACTION');
    expect(
      RN.NativeModules.PaxA920BarcodeScanner.initScanner
    ).toHaveBeenCalledWith('custom.ACTION');
  });

  test('init alias calls initialize', () => {
    init('alias.ACTION');
    expect(
      RN.NativeModules.PaxA920BarcodeScanner.initScanner
    ).toHaveBeenCalledWith('alias.ACTION');
  });

  test('finalize calls native finalizeScanner', () => {
    finalize();
    expect(
      RN.NativeModules.PaxA920BarcodeScanner.finalizeScanner
    ).toHaveBeenCalled();
  });

  test('success listener receives emitted payload', () => {
    const received: Array<{ data?: string | null }> = [];
    addBarcodeSuccessListener((event: { data?: string | null }) =>
      received.push(event)
    );
    RN.NativeEventEmitter.__emit('barcodeReadSuccess', { data: '12345' });
    expect(received).toEqual([{ data: '12345' }]);
    expect(
      RN.NativeModules.PaxA920BarcodeScanner.addListener
    ).toHaveBeenCalledWith('barcodeReadSuccess');
  });

  test('fail listener receives emitted error', () => {
    const received: Array<{ error?: string | null }> = [];
    addBarcodeFailListener((event: { error?: string | null }) =>
      received.push(event)
    );
    RN.NativeEventEmitter.__emit('barcodeReadFail', { error: 'Oops' });
    expect(received).toEqual([{ error: 'Oops' }]);
    expect(
      RN.NativeModules.PaxA920BarcodeScanner.addListener
    ).toHaveBeenCalledWith('barcodeReadFail');
  });
});
