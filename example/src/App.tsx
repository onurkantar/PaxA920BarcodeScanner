import { Text, View, StyleSheet } from 'react-native';
import { useEffect, useState } from 'react';
import {
  initialize,
  finalize,
  addBarcodeSuccessListener,
  addBarcodeFailListener,
  BARCODE_READ_SUCCESS,
  BARCODE_READ_FAIL,
} from 'diva.pax-a920-barcode-scanner';

export default function App() {
  const [data, setData] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    initialize('com.example.BARCODE_ACTION');
    const s = addBarcodeSuccessListener(({ data: payload }) =>
      setData(payload ?? null)
    );
    const f = addBarcodeFailListener(({ error: message }) =>
      setError(message ?? null)
    );

    return () => {
      s.remove();
      f.remove();
      finalize();
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text>
        Listening for: {BARCODE_READ_SUCCESS} / {BARCODE_READ_FAIL}
      </Text>
      <Text>Last barcode: {data ?? '-'}</Text>
      <Text>Last error: {error ?? '-'}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
