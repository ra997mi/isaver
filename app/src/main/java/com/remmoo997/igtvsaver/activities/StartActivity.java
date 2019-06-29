package com.remmoo997.igtvsaver.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.remmoo997.igtvsaver.R;
import com.remmoo997.igtvsaver.services.Connectivity;
import com.remmoo997.igtvsaver.utils.Utility;


public class StartActivity extends AppCompatActivity {

    private boolean isTheirConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isTheirConnection = Connectivity.isConnected(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            setTranslucentStatus();

        // Make screen Portrait to disable Landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_start);
        setStartElements();
        showScreen();
    }

    private void showScreen() {
        new Handler().postDelayed(() -> {

            if (!isTheirConnection) {
                startIntent(new Intent(this, ConnectionActivity.class));
            } else {
                startIntent(new Intent(this, MainActivity.class));
            }
        }, (long) 800);
    }

    private void setStartElements() {
        TextView version = findViewById(R.id.versionName);
        version.setText(getString(R.string.version, Utility.getAppVersion(StartActivity.this)));
        TextView copyright = findViewById(R.id.copyrightText);
        copyright.setText(getString(R.string.proximadev, Utility.getYear()));
    }

    @TargetApi(19)
    private void setTranslucentStatus() {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        winParams.flags |= bits;
        win.setAttributes(winParams);
    }

    private void startIntent(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
