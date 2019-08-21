package com.wash.iot.nanny.nanny_print_sunmi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PrinterReceiver extends BroadcastReceiver {
    public PrinterReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent data) {
        String action = data.getAction();
        String type = "PrinterStatus";
        Log.d("PrinterReceiver", action);

       
    }
}