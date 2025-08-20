import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  // Required by NativeEventEmitter to track subscriptions
  addListener(eventType: string): void;
  removeListeners(count: number): void;
  initScanner(action?: string): void;
  finalizeScanner(): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('PaxA920BarcodeScanner');
