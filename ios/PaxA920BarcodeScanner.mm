#import "PaxA920BarcodeScanner.h"

@implementation PaxA920BarcodeScanner
RCT_EXPORT_MODULE()

- (void)initScanner:(NSString * _Nullable)action {
}

- (void)finalizeScanner {
}

// Required for NativeEventEmitter
- (void)addListener:(NSString *)eventType {
}

- (void)removeListeners:(double)count {
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativePaxA920BarcodeScannerSpecJSI>(params);
}

@end
