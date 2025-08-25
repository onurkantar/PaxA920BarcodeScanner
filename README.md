# PAX A920 Barcode Scanner

[![npm version](https://badge.fury.io/js/diva.pax-a920-barcode-scanner.svg)](https://badge.fury.io/js/diva.pax-a920-barcode-scanner)
[![TypeScript](https://img.shields.io/badge/%3C%2F%3E-TypeScript-%230074c1.svg)](http://www.typescriptlang.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A professional React Native library for barcode scanning on PAX A920 devices. Provides seamless integration with the PAX Neptune Lite API for broadcast-based barcode capture with full support for both old and new React Native architectures.

## 🚀 Features

- ✅ **PAX A920 Device Support**: Native integration with PAX Neptune Lite API
- ✅ **Broadcast Mode**: Automatic barcode capture without manual triggers
- ✅ **TurboModule Architecture**: Built as a modern TurboModule for optimal performance
- ✅ **Dual Event System**: Support for both DeviceEventEmitter and NativeEventEmitter
- ✅ **TypeScript Support**: Complete type definitions included
- ✅ **Cross-Platform**: iOS stub implementation for development flexibility
- ✅ **Event-Driven**: Real-time barcode events with success/failure handling
- ✅ **Production Ready**: Comprehensive error handling and debug logging
- ✅ **Zero Dependencies**: Lightweight with no external dependencies

## 📋 Requirements

- React Native 0.70+
- Android API Level 21+
- PAX A920 devices with Neptune Lite API
- TypeScript 4.0+ (optional but recommended)

## 📦 Installation

```bash
npm install diva.pax-a920-barcode-scanner
```

Or using Yarn:

```bash
yarn add diva.pax-a920-barcode-scanner
```

### Android Setup

No additional setup required! The library uses autolinking and includes the necessary PAX Neptune Lite API JAR file.

### iOS Setup

The library includes iOS stubs for development purposes. For production iOS apps, you'll need to implement your own barcode scanning solution or use this library only on Android devices.

## 🔧 Quick Start

### Basic Usage

```typescript
import React, { useEffect, useState } from 'react';
import { View, Text, Button } from 'react-native';
import {
  initialize,
  finalize,
  addBarcodeSuccessListener,
  addBarcodeFailListener,
} from 'diva.pax-a920-barcode-scanner';

export default function BarcodeScanner() {
  const [barcode, setBarcode] = useState<string>('');
  const [status, setStatus] = useState<string>('Ready');

  useEffect(() => {
    // Set up event listeners
    const successListener = addBarcodeSuccessListener(({ data }) => {
      setBarcode(data || '');
      setStatus('Barcode scanned successfully');
    });

    const failListener = addBarcodeFailListener(({ error }) => {
      setStatus(`Error: ${error || 'Unknown error'}`);
    });

    // Cleanup on unmount
    return () => {
      successListener.remove();
      failListener.remove();
      finalize();
    };
  }, []);

  const startScanning = () => {
    initialize();
    setStatus('Scanner initialized - ready to scan');
  };

  const stopScanning = () => {
    finalize();
    setStatus('Scanner stopped');
  };

  return (
    <View>
      <Button title="Start Scanner" onPress={startScanning} />
      <Button title="Stop Scanner" onPress={stopScanning} />
      <Text>Status: {status}</Text>
      <Text>Last Barcode: {barcode || 'None'}</Text>
    </View>
  );
}
```

### Auto-Start Scanner

```typescript
import React, { useEffect, useState } from 'react';
import {
  initialize,
  finalize,
  addBarcodeSuccessListener,
} from 'diva.pax-a920-barcode-scanner';

export default function AutoScanner() {
  const [barcode, setBarcode] = useState<string>('');

  useEffect(() => {
    // Auto-start scanner and set up listener
    const successListener = addBarcodeSuccessListener(({ data }) => {
      setBarcode(data || '');
    });

    initialize(); // Start scanning immediately

    return () => {
      successListener.remove();
      finalize();
    };
  }, []);

  return (
    <View>
      <Text>Last Barcode: {barcode || 'Waiting for scan...'}</Text>
    </View>
  );
}
```

## 📚 API Reference

### Functions

#### `initialize(action?: string): void`

Initializes the barcode scanner and starts listening for broadcast intents.

**Parameters:**

- `action` (optional): Custom broadcast action to listen for. Defaults to `'com.barcode.sendBroadcast'`

**Example:**

```typescript
// Use default action
initialize();

// Use custom action
initialize('com.myapp.barcode');
```

#### `finalize(): void`

Stops the barcode scanner and unregisters broadcast receivers.

**Example:**

```typescript
finalize();
```

#### `addBarcodeSuccessListener(handler: (event: BarcodeSuccessEvent) => void): EmitterSubscription`

Adds a listener for successful barcode scans.

**Parameters:**

- `handler`: Function called when a barcode is successfully scanned

**Returns:**

- `EmitterSubscription`: Subscription object with a `remove()` method

**Example:**

```typescript
const subscription = addBarcodeSuccessListener(({ data }) => {
  console.log('Scanned barcode:', data);
});

// Don't forget to remove the listener
subscription.remove();
```

#### `addBarcodeFailListener(handler: (event: BarcodeFailEvent) => void): EmitterSubscription`

Adds a listener for barcode scanning failures.

**Parameters:**

- `handler`: Function called when barcode scanning fails

**Returns:**

- `EmitterSubscription`: Subscription object with a `remove()` method

**Example:**

```typescript
const subscription = addBarcodeFailListener(({ error }) => {
  console.error('Barcode scan failed:', error);
});

// Don't forget to remove the listener
subscription.remove();
```

### Types

#### `BarcodeSuccessEvent`

```typescript
interface BarcodeSuccessEvent {
  data?: string | null;
}
```

#### `BarcodeFailEvent`

```typescript
interface BarcodeFailEvent {
  error?: string | null;
}
```

## 🔍 Advanced Usage

### Custom Broadcast Actions

You can listen for custom broadcast actions by passing an action parameter to `initialize()`:

```typescript
// Listen for custom barcode broadcasts
initialize('com.mycompany.custom.barcode');
```

### Multiple Event Listeners

You can register multiple listeners for the same event:

```typescript
const listener1 = addBarcodeSuccessListener(({ data }) => {
  // Handle in component A
  updateComponentA(data);
});

const listener2 = addBarcodeSuccessListener(({ data }) => {
  // Handle in component B
  updateComponentB(data);
});

// Remember to remove both listeners
listener1.remove();
listener2.remove();
```

### Error Handling

Always implement error handling for robust applications:

```typescript
useEffect(() => {
  const successListener = addBarcodeSuccessListener(({ data }) => {
    if (data) {
      processBarcode(data);
    } else {
      console.warn('Received empty barcode data');
    }
  });

  const failListener = addBarcodeFailListener(({ error }) => {
    // Log error for debugging
    console.error('Barcode scan error:', error);

    // Show user-friendly message
    showErrorMessage('Failed to scan barcode. Please try again.');
  });

  return () => {
    successListener.remove();
    failListener.remove();
    finalize();
  };
}, []);
```

### App State Management

Handle app backgrounding and foregrounding:

```typescript
import { AppState } from 'react-native';

useEffect(() => {
  const handleAppStateChange = (nextAppState: string) => {
    if (nextAppState === 'active') {
      // Reinitialize scanner when app becomes active
      initialize();
    } else if (nextAppState === 'background') {
      // Optionally finalize to save resources
      finalize();
    }
  };

  const subscription = AppState.addEventListener(
    'change',
    handleAppStateChange
  );
  return () => subscription?.remove();
}, []);
```

## 🛠️ How It Works

### Architecture Overview

The library is built as a modern React Native TurboModule with the following components:

1. **JavaScript Layer** (`src/index.tsx`): Provides the public API and event handling
2. **Native Specification** (`src/NativePaxA920BarcodeScanner.ts`): Defines the TurboModule interface
3. **Android Implementation**:
   - `PaxA920BarcodeScannerModule.kt`: Main native module
   - `PaxA920BarcodeScannerReceiver.kt`: Broadcast receiver for barcode events
4. **iOS Stub Implementation**: Provides cross-platform compatibility

### Broadcast Flow

1. **Initialization**: `initialize()` calls the native module to set PAX device to broadcast mode
2. **Registration**: Native module registers a `BroadcastReceiver` for barcode intents
3. **Scanning**: When a barcode is scanned, PAX device sends a broadcast intent
4. **Reception**: `PaxA920BarcodeScannerReceiver` receives the broadcast
5. **Extraction**: Receiver extracts barcode data from intent extras
6. **Emission**: Event is emitted to JavaScript via both DeviceEventEmitter and NativeEventEmitter
7. **Handling**: JavaScript listeners receive the event and update UI

### Event Deduplication

The library includes intelligent event deduplication to prevent duplicate events within a 100ms window, ensuring clean event handling even with multiple emission paths.

## 🐛 Troubleshooting

### Common Issues

#### "No barcode events received"

1. **Check device compatibility**: Ensure you're running on a PAX A920 device
2. **Verify initialization**: Make sure `initialize()` is called before scanning
3. **Check broadcast action**: Verify the PAX device is sending the correct broadcast action
4. **Review logs**: Check Android logcat for detailed error messages

```bash
# Filter logcat for scanner-related logs
adb logcat | grep -E "(PaxA920Scanner|PaxA920BarcodeRx)"
```

#### "Scanner not working after app backgrounding"

The scanner automatically handles app lifecycle, but you may need to reinitialize after extended background periods. See the App State Management section above.

#### "TypeScript errors"

Ensure you have the latest version installed and that your TypeScript configuration includes the library's types:

```bash
npm install --save-dev @types/react-native
```

### Debug Mode

The library includes comprehensive debug logging in development mode. Check the Metro console for detailed logs:

```
[Library] initialize called with action: com.barcode.sendBroadcast
[Library] adding success listener
[Library] 🎯 barcodeReadSuccess event received: { data: "1234567890" }
```

### Performance Optimization

For high-frequency barcode scanning:

1. **Debounce rapid scans**: Implement debouncing to prevent duplicate processing
2. **Remove unused listeners**: Always remove event listeners when components unmount
3. **Use React.memo**: Optimize renders for components that display barcode data

```typescript
import { useMemo } from 'react';

const BarcodeDisplay = React.memo(({ barcode }: { barcode: string }) => {
  const processedBarcode = useMemo(() => {
    return processBarcode(barcode);
  }, [barcode]);

  return <Text>{processedBarcode}</Text>;
});
```

## 🧪 Testing

### Running Tests

```bash
yarn test
```

### Running the Example App

```bash
# Install dependencies
yarn

# Run on Android
yarn example android

# Run on iOS (stub implementation)
yarn example ios
```

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Development Setup

1. Clone the repository
2. Install dependencies: `yarn`
3. Run the example app: `yarn example android`

### Code Quality

```bash
# Linting
yarn lint

# Type checking
yarn typecheck

# Clean build
yarn clean
```

## 📄 License

MIT © [Onur Kantar](https://github.com/onurkantar)

## 🔗 Links

- 📖 [Documentation](https://github.com/onurkantar/diva.pax-a920-barcode-scanner#readme)
- 🐛 [Issue Tracker](https://github.com/onurkantar/diva.pax-a920-barcode-scanner/issues)
- 💬 [Discussions](https://github.com/onurkantar/diva.pax-a920-barcode-scanner/discussions)
- 📦 [npm Package](https://www.npmjs.com/package/diva.pax-a920-barcode-scanner)

## 🏷️ Keywords

- React Native
- PAX A920
- Barcode Scanner
- POS Terminal
- Neptune Lite API
- TurboModule
- Android
- Payment Terminal
- Point of Sale

---

Made with ❤️ for the React Native community
