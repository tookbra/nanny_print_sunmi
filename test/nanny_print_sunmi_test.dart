import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:nanny_print_sunmi/nanny_print_sunmi.dart';

void main() {
  const MethodChannel channel = MethodChannel('nanny_print_sunmi');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
//    expect(await NannyPrintSunmi.platformVersion, '42');
  });
}
