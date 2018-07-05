package com.remmoo997.igtvsaver.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.remmoo997.igtvsaver.R;
import com.remmoo997.igtvsaver.services.Connectivity;

import java.util.Calendar;

public class StartActivity  extends AppCompatActivity {

    private static final String TAG = "StartActivity";
    private boolean isTheirConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isTheirConnection = Connectivity.isConnected(this);

        Log.d(TAG, "onCreate: Started.");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            setTranslucentStatus();

        // Make screen Portrait to disable Landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_start);
        setStartElements();
        showScreen();
    }

    private void showScreen() {
        Log.d(TAG, "showScreen: Function called.");
        new Handler().postDelayed(() -> {

            if (!isTheirConnection){
                startIntent(new Intent(this, ConnectionActivity.class));
            }
            else {
                startIntent(new Intent(this, MainActivity.class));
            }
        }, (long) 800);
    }

    private void setStartElements() {
        Log.d(TAG, "setStartElements: Function called.");
        TextView version = findViewById(R.id.versionName);
        version.setText(getString(R.string.version, getVersion()));
        TextView copyright = findViewById(R.id.copyrightText);
        copyright.setText(getString(R.string.proximadev ,getYear()));
    }

    @TargetApi(19)
    private void setTranslucentStatus() {
        Log.d(TAG, "setTranslucentStatus: Function called.");
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        winParams.flags |= bits;
        win.setAttributes(winParams);
    }

    private String getVersion() {
        Log.d(TAG, "getVersion: Function called.");
        String mVersion = "0.0.0";
        try {
            mVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception ex) {
            Log.e(TAG, "getVersion: Error: " + mVersion, ex);
            ex.printStackTrace();
        }
        return mVersion;
    }

    private static int getYear(){
        Log.d(TAG, "getYear: Function called.");
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    private void startIntent(Intent intent){
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
