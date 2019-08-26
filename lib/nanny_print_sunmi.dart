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

  Future<void> bindPrinter() async {
    await _channel.invokeMethod("bind");
  }

  Future<void> initPrinter() async {
    await _channel.invokeMethod("initPrinter");
  }

  Future<bool> printSelf() async {
    return await _channel.invokeMethod("printSelf");
  }

  Future<bool> printText({ String text }) async {
    Map<String,dynamic> args = <String,dynamic>{};
    args.putIfAbsent("text", () => text);
    return await _channel.invokeMethod("printText",args);
  }

  Future<bool> printTextWithFont({ String text, String fontName, int fontSize }) async {
    Map<String,dynamic> args = <String,dynamic>{};
    args.putIfAbsent("text", () => text);
    args.putIfAbsent("fontName", () => fontName);
    args.putIfAbsent("fontSize", () => fontSize);
    return await _channel.invokeMethod("printTextWithFont",args);
  }

  Future<bool> setLineWrap({ int line }) async {
    Map<String,dynamic> args = <String,dynamic>{};
    args.putIfAbsent("line", () => line);
    return await _channel.invokeMethod("setLineWrap",args);
  }

  Future<bool> setAlignment({ TEXTALIGN align }) async {
    Map<String,dynamic> args = <String,dynamic>{};
    args.putIfAbsent("alignment", () => align == TEXTALIGN.LEFT ? 0
        : align == TEXTALIGN.CENTER ? 1
        : align == TEXTALIGN.RIGHT ? 2
        : 0);
    return await _channel.invokeMethod("setAlignment",args);
  }

  Future<bool> setFontSize({ int fontSize }) async {
    Map<String,dynamic> args = <String,dynamic>{};
    args.putIfAbsent("fontSize", () => fontSize);
    return await _channel.invokeMethod("setFontSize",args);
  }

  Future<bool> printOrder({ String title, int total, List<String> body, String remark }) async {
    Map<String,dynamic> args = <String,dynamic>{};
    args.putIfAbsent("title", () => title);
    args.putIfAbsent("total", () => total);
    args.putIfAbsent("body", () => body);
    args.putIfAbsent("remark", () => remark);
    return await _channel.invokeMethod("printOrder",args);
  }

  Future<int> getStringWidth({ String text}) async {
    Map<String,dynamic> args = <String,dynamic>{};
    args.putIfAbsent("text", () => text);
    return await _channel.invokeMethod("getStringWidth",args);
  }
}

enum TEXTALIGN { LEFT, CENTER, RIGHT }
