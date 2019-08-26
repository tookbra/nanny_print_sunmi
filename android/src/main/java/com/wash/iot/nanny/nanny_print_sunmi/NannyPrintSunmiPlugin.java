package com.wash.iot.nanny.nanny_print_sunmi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;

import io.flutter.app.FlutterActivity;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.IWoyouService;

/** NannyPrintSunmiPlugin */
public class NannyPrintSunmiPlugin implements MethodCallHandler {
  /** Plugin registration. */
  private static final String TAG = "FlutterPrintPlugin";
  private static final String NAMESPACE = "plugins.iot.wash.com/nanny_print";
  private final Registrar registrar;
  private final MethodChannel channel;
  private IWoyouService woyouService;
  private ICallback callback = null;
  private Context context;
  private final EventChannel stateChannel;

  private PrinterReceiver receiver = new PrinterReceiver();

  // 缺纸异常
  public final static String OUT_OF_PAPER_ACTION = "woyou.aidlservice.jiuv5.OUT_OF_PAPER_ACTION";
  // 打印错误
  public final static String ERROR_ACTION = "woyou.aidlservice.jiuv5.ERROR_ACTION";
  // 可以打印
  public final static String NORMAL_ACTION = "woyou.aidlservice.jiuv5.NORMAL_ACTION";
  // 开盖子
  public final static String COVER_OPEN_ACTION = "woyou.aidlservice.jiuv5.COVER_OPEN_ACTION";
  // 关盖子异常
  public final static String COVER_ERROR_ACTION = "woyou.aidlservice.jiuv5.COVER_ERROR_ACTION";
  // 切刀异常1－卡切刀
  public final static String KNIFE_ERROR_1_ACTION = "woyou.aidlservice.jiuv5.KNIFE_ERROR_ACTION_1";
  // 切刀异常2－切刀修复
  public final static String KNIFE_ERROR_2_ACTION = "woyou.aidlservice.jiuv5.KNIFE_ERROR_ACTION_2";
  // 打印头过热异常
  public final static String OVER_HEATING_ACITON = "woyou.aidlservice.jiuv5.OVER_HEATING_ACITON";
  // 打印机固件开始升级
  public final static String FIRMWARE_UPDATING_ACITON = "woyou.aidlservice.jiuv5.FIRMWARE_UPDATING_ACITON";

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final NannyPrintSunmiPlugin instance = new NannyPrintSunmiPlugin(registrar);
  }

  private ServiceConnection connService = new ServiceConnection() {

    @Override
    public void onServiceDisconnected(ComponentName name) {

      woyouService = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      woyouService = IWoyouService.Stub.asInterface(service);
    }
  };

  NannyPrintSunmiPlugin(Registrar r) {
    this.registrar = r;
    this.channel = new MethodChannel(registrar.messenger(), NAMESPACE+"/methods");
    this.stateChannel = new EventChannel(registrar.messenger(), NAMESPACE+"/state");
    channel.setMethodCallHandler(this);
    this.context = r.context();
    stateChannel.setStreamHandler(stateHandler);
  }

  private final StreamHandler stateHandler = new StreamHandler() {
    private EventSink sink;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
      }
    };
    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
      sink = eventSink;
      IntentFilter filter = new IntentFilter();
      filter.addAction(OUT_OF_PAPER_ACTION);
      filter.addAction(ERROR_ACTION);
      filter.addAction(NORMAL_ACTION);
      filter.addAction(COVER_OPEN_ACTION);
      filter.addAction(COVER_ERROR_ACTION);
      filter.addAction(KNIFE_ERROR_1_ACTION);
      filter.addAction(KNIFE_ERROR_2_ACTION);
      filter.addAction(OVER_HEATING_ACITON);
      filter.addAction(FIRMWARE_UPDATING_ACITON);
      registrar.activeContext().registerReceiver(receiver, filter);
    }

    @Override
    public void onCancel(Object o) {
      sink = null;
      registrar.activeContext().unregisterReceiver(mReceiver);
    }
  };


  @Override
  public void onMethodCall(MethodCall call, Result result) {
    switch (call.method) {
      case "bind":
        bindingService();
        result.success(true);
        break;
      case "printSelf":
        printSelf(result);
        result.success(true);
        break;
      case "printText":
        String text = call.argument("text");
        printText(text);
        result.success(true);
        break;  
      case "printTextWithFont":
        String fontText = call.argument("text");
        String fontName = call.argument("fontName");
        int textSize = call.argument("fontSize");
        printTextWithFont(fontText, fontName, textSize);
        result.success(true);
        break;
      case "setAlignment":
        int alignment = call.argument("alignment");
        this.setAlignment(alignment);
        result.success(true);
        break;
      case "setLineWrap":
        int line = call.argument("line");
        this.setLineWrap(line);
        result.success(true);
        break;
      case "setFontSize":
        int fontSize = call.argument("fontSize");
        this.setFontSize(fontSize);
        result.success(true);
        break;
      case "printOrder":
        String title = call.argument("title");
        Integer total = call.argument("total");
        List<String> body = call.argument("body");
        String remark = call.argument("remark");
        List<String> subTitles = call.argument("subTitles");
        String orderNo = call.argument("orderNo");
        this.printOrder(title, total, body, remark, subTitles, orderNo);
        result.success(true);
        break;
      case "getStringWidth":
        String text1 = call.argument("text");
        int size = this.getStringWidth(text1);
        result.success(size);
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  /**
   * bind
   */
  public void bindingService(){
    callback = new ICallback.Stub() {

      @Override
      public void onRunResult(final boolean success) throws RemoteException {
      }

      @Override
      public void onReturnString(final String value) throws RemoteException {
        Log.i(TAG,"printlength:" + value + "\n");
      }

      @Override
      public void onRaiseException(int code, final String msg) throws RemoteException {
        Log.i(TAG,"onRaiseException: " + msg);
      }
    };
    Intent intent=new Intent();
    intent.setPackage("woyou.aidlservice.jiuiv5");
    intent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");
    context.startService(intent);
    context.bindService(intent, connService, Context.BIND_AUTO_CREATE);
  }

  /**
   *
   * @param result
   */
  private void printSelf(Result result) {
    ThreadPoolManager.getInstance().executeTask(new Runnable(){
      @Override
      public void run() {
        try {
          woyouService.setAlignment(1, callback);
          woyouService.printerSelfChecking(callback);
        } catch (RemoteException e) {
          e.printStackTrace();
        }
      }}
    );
  }

  public void initPrinter(){
    ThreadPoolManager.getInstance().executeTask(new Runnable(){
      @Override
      public void run() {
        try {
          woyouService.printerInit(callback);
        } catch (RemoteException e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * 打印文字
   * @param text
   */
  private void printText(final String text){
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          woyouService.printText(text,callback);
        } catch (RemoteException e){
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * 打印文字
   * @param text
   * @param fontName
   * @param fontSize
   */
  private void printTextWithFont(final String text, final String fontName, final float fontSize){
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          woyouService.printTextWithFont(text,fontName,fontSize,callback);
        } catch (RemoteException e){
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * 设置文字大小
   * @param fontSize
   */
  public void setFontSize(final float fontSize){
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          woyouService.setFontSize(fontSize,callback);
        } catch (final RemoteException e){
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * 设置对齐模式，对之后打印有影响，除非初始化
   * 对齐方式 0--居左 , 1--居中, 2--居右
   * @param alignment
   */
  private void setAlignment(final int alignment){
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          woyouService.setAlignment(alignment, callback);
        } catch (RemoteException e){
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * 打印机走纸(强制换行，结束之前的打印内容后走纸n行)
   * @param line 走纸行数
   */
  private void setLineWrap(final int line){
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          woyouService.lineWrap(line,callback);
        } catch (RemoteException e){
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * 打印二维码
   * @param content
   */
  private void printQCode(String content){
    ThreadPoolManager.getInstance().executeTask(new Runnable(){
      @Override
      public void run() {
        try {
          woyouService.setAlignment(1, callback);
          woyouService.printQRCode(content, 10, 2, callback);
          byte[] bytes = BytesUtil.getZXingQRCode(content, 240);
          System.out.println(BytesUtil.getHexStringFromBytes(bytes));
          woyouService.sendRAWData(bytes, callback);
          woyouService.lineWrap(3, callback);
        } catch (RemoteException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }});
  }

  private int getStringWidth(String str) {
    int width = 0;
    for (char c : str.toCharArray()) {
      width += isChinese(c) ? 2 : 1;
    }
    return width;
  }

  /**
   * 判断是否中文
   * GENERAL_PUNCTUATION 判断中文的“号
   * CJK_SYMBOLS_AND_PUNCTUATION 判断中文的。号
   * HALFWIDTH_AND_FULLWIDTH_FORMS 判断中文的，号
   *
   * @param c 字符
   * @return 是否中文
   */
  private static boolean isChinese(char c) {
    Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
    return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
            || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
            || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
            || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
  }

  private void printOrder(final String title, final Integer total, 
    final List<String> body, final String remark, final List<String> subTitles, final String orderNo){
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          woyouService.setAlignment(1, callback);
          woyouService.setFontSize(42, callback);
          woyouService.printText(title, callback);
          woyouService.setFontSize(24, callback);
          woyouService.setAlignment(0, callback);
          for(String text : subTitles) {
            woyouService.printText(text, callback);
          }
          woyouService.setAlignment(1, callback);
          woyouService.printText(PrinterUtils.printLine(), callback);
          woyouService.printText(PrinterUtils.printInOneLine("产品", "数量") + "\n", callback);
          for(String text : body) {
            woyouService.printOriginalText(text, callback);
          }
          woyouService.printText(PrinterUtils.printLine(), callback);
          woyouService.printText(PrinterUtils.printInOneLine("合计", "x" + total), callback);
          if(remark != null && remark.trim() != "") {
            woyouService.lineWrap(3, callback);
            woyouService.printText(remark, callback);
          }
          woyouService.lineWrap(3, callback);
          woyouService.printQRCode(orderNo, 10, 2, callback);
          woyouService.lineWrap(5, callback);
        } catch (RemoteException e){
          e.printStackTrace();
        }
      }
    });
  }
}
