package com.wash.iot.nanny.nanny_print_sunmi;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import io.flutter.app.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
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
    channel.setMethodCallHandler(this);
    this.context = r.context();
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    switch (call.method) {
      case "bind":
        bindingService();
        break;
      case "printSelf":
        printSelf(result);
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
}
