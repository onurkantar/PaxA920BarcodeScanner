import {
  Text,
  View,
  StyleSheet,
  Button,
  TextInput,
  DeviceEventEmitter,
  NativeEventEmitter,
  NativeModules,
} from 'react-native';
import { useEffect, useState } from 'react';
import {
  initialize,
  finalize,
  addBarcodeSuccessListener,
  addBarcodeFailListener,
} from 'diva.pax-a920-barcode-scanner';

export default function App() {
  const [lastBarcode, setLastBarcode] = useState<string>('');
  const [status, setStatus] = useState<string>('Idle');
  const [manualEntry, setManualEntry] = useState<string>('');

  useEffect(() => {
    console.log('[Example] === Setting up listeners ===');

    // Direct DeviceEventEmitter test
    console.log('[Example] Setting up direct DeviceEventEmitter listener');
    const directSub = DeviceEventEmitter.addListener(
      'barcodeReadSuccess',
      (event) => {
        console.log('[Example] 🔥 DIRECT DeviceEventEmitter success:', event);
        setLastBarcode(event?.data ?? JSON.stringify(event));
        setStatus('Direct event received!');
      }
    );

    // Direct NativeEventEmitter test
    console.log('[Example] Setting up direct NativeEventEmitter listener');
    const nativeEmitter = new NativeEventEmitter(
      NativeModules.PaxA920BarcodeScanner
    );
    const nativeSub = nativeEmitter.addListener(
      'barcodeReadSuccess',
      (event) => {
        console.log('[Example] 🚀 DIRECT NativeEventEmitter success:', event);
        setLastBarcode(event?.data ?? JSON.stringify(event));
        setStatus('Native event received!');
      }
    );

    // Library listeners
    const s = addBarcodeSuccessListener(({ data: payload }) => {
      console.log('[Example] ✅ LIBRARY barcodeReadSuccess payload:', payload);
      setLastBarcode(payload ?? '');
      setStatus('Library event received!');
    });
    const f = addBarcodeFailListener(({ error: message }) => {
      console.log('[Example] ❌ barcodeReadFail error:', message);
      setStatus(`Error: ${message ?? 'unknown'}`);
    });

    return () => {
      console.log('[Example] Cleaning up listeners and finalizing');
      directSub.remove();
      nativeSub.remove();
      s.remove();
      f.remove();
      finalize();
    };
  }, []);

  const onInitPress = () => {
    console.log('[Example] === Initialize pressed ===');
    try {
      console.log('[Example] Calling initialize()...');
      initialize();
      console.log('[Example] ✅ initialize() returned successfully');
      setStatus('Scanner initialized (listening)');
    } catch (e) {
      console.log('[Example] ❌ initialize() threw error:', e);
      setStatus('Failed to initialize scanner');
    }
  };

  const onManualFocus = () => {
    finalize();
    setStatus('Scanner finalized (input mode)');
  };

  return (
    <View style={styles.container}>
      <Button title="Initialize Scanner" onPress={onInitPress} />
      <View style={{ height: 8 }} />
      <Button
        title="Finalize Scanner"
        onPress={() => {
          finalize();
          setStatus('Scanner finalized (input mode)');
        }}
      />
      <Text style={styles.status}>Status: {status}</Text>
      <Text style={styles.lastBarcode}>Last barcode: {lastBarcode || '-'}</Text>

      <TextInput
        style={styles.input}
        value={manualEntry}
        onChangeText={setManualEntry}
        onFocus={onManualFocus}
        placeholder="Manual input (focus to finalize scanner)"
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  status: {
    marginTop: 12,
  },
  readonlyField: {
    marginTop: 16,
    width: '80%',
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 6,
    paddingHorizontal: 12,
    paddingVertical: 10,
    backgroundColor: '#f5f5f5',
  },
  lastBarcode: {
    marginTop: 16,
  },
  input: {
    marginTop: 16,
    width: '80%',
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 6,
    paddingHorizontal: 12,
    paddingVertical: 10,
  },
});
