import 'dart:async';

import 'package:flutter/services.dart';

class NannyPrintSunmi {
  static const NAMESPACE = 'plugins.iot.wash.com/nanny_print';
  final MethodChannel _channel = const MethodChannel('$NAMESPACE/methods');
  final StreamController<MethodCall> _methodStreamController = new StreamController.broadcast();

  NannyPrintSunmi._() {
    _channel.setMethodCallHandler((MethodCall call) {
      _methodStreamController.add(call);
    });
  }
  static NannyPrintSunmi _instance = new NannyPrintSunmi._();
  static NannyPrintSunmi get instance => _instance;

  Future<Null> bindPrinter() async {
    await _channel.invokeMethod("bind");
  }

  Future<Null> initPrinter() async {
    await _channel.invokeMethod("initPrinter");
  }

  Future<Null> printSelf() async {
    await _channel.invokeMethod("printSelf");
  }

  Future<Null> printText({ String text }) async {
    Map<String,dynamic> args = <String,dynamic>{};
    args.putIfAbsent("text", () => text);
    await _channel.invokeMethod("printText",args);
  }

  Future<Null> printTextWithFont({ String text, String fontName, int fontSize }) async {
    Map<String,dynamic> args = <String,dynamic>{};
    args.putIfAbsent("text", () => text);
    args.putIfAbsent("fontName", () => fontName);
    args.putIfAbsent("fontSize", () => fontSize);
    await _channel.invokeMethod("printTextWithFont",args);
  }
}
