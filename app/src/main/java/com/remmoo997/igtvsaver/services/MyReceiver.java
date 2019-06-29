package com.remmoo997.igtvsaver.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            if (action != null && action.equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("ServiceOn", false)) {
                    context.startService(new Intent(context, ClipboardService.class));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
