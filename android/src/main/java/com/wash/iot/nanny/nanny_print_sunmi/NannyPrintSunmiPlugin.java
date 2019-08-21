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
        break;
      case "printSelf":
        printSelf(result);
        break;
      case "printText":
        String text = call.argument("text");
        printText(text);
        break;  
      case "printTextWithFont":
        String fontText = call.argument("text");
        String fontName = call.argument("fontName");
        int fontSize = call.argument("fontSize");
        printTextWithFont(fontText, fontName, fontSize);
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
}
