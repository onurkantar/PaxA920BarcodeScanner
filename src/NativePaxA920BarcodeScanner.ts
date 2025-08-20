import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  initScanner(action?: string): void;
  finalizeScanner(): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('PaxA920BarcodeScanner');
