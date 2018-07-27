package com.remmoo997.igtvsaver.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.remmoo997.igtvsaver.R;
import com.remmoo997.igtvsaver.services.ClipboardService;
import com.remmoo997.igtvsaver.utils.InstaData;
import com.tapadoo.alerter.Alerter;

import es.dmoral.toasty.Toasty;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.FullscreenPromptBackground;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static String OPENED_FROM_OUTSIDE = null;
    private static final String TAG = "MainActivity";
    private InterstitialAd mInterstitialAd;
    private Intent mClipboardIntent;
    public FABProgressCircle mFabProgressCircle;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make screen Portrait to disable Landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mClipboardIntent = new Intent(this, ClipboardService.class);

        mInterstitialAd = new InterstitialAd(MainActivity.this);
        mInterstitialAd.setAdUnitId(getString(R.string.ad_unit1));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
            }
        });

        Switch mSwitch = findViewById(R.id.fastlink);
        mSwitch.setChecked(mSharedPreferences.getBoolean("ServiceOn",false));
        mSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                startService(mClipboardIntent);
            }
            else {
                stopService(mClipboardIntent);
            }
            Log.d(TAG, "Switch Value: " + b);
            mSharedPreferences.edit().putBoolean("ServiceOn",b).apply();
        });

        FloatingActionButton mFabOpen = findViewById(R.id.getFab);
        mFabOpen.setOnClickListener(this);

        mFabProgressCircle = findViewById(R.id.fabProgressCircle);

        if (mSharedPreferences.getBoolean("isFirstStarted",true)) {
            new MaterialTapTargetPrompt.Builder(MainActivity.this)
                    .setTarget(mFabOpen)
                    .setBackButtonDismissEnabled(false)
                    .setPrimaryText(getString(R.string.guide))
                    .setPromptBackground(new FullscreenPromptBackground())
                    .setSecondaryText(getString(R.string.guide_description))
                    .show();
            mSharedPreferences.edit().putBoolean("isFirstStarted",false).apply();
        }

        if (getIntent() != null && getIntent().getDataString() != null){
            OPENED_FROM_OUTSIDE = getIntent().getDataString();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getIntent() != null && getIntent().getDataString() != null){
            OPENED_FROM_OUTSIDE = getIntent().getDataString();
        }
    }

    @Override
    public void onClick(View view) {
        try {
            if (OPENED_FROM_OUTSIDE != null && (OPENED_FROM_OUTSIDE.contains("www.instagram.com/tv/") || OPENED_FROM_OUTSIDE.contains("www.instagram.com/p/"))){
                mFabProgressCircle.show();
                Toasty.info(MainActivity.this ,getString(R.string.please_wait),0).show();
                new InstaData(this, OPENED_FROM_OUTSIDE).execute();
            }
            else{
                ClipboardManager mClipboardService = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                if (mClipboardService != null) {
                    ClipData link = mClipboardService.getPrimaryClip();
                    ClipData.Item item = link.getItemAt(0);
                    String mClipboardUrl = item.getText().toString();
                    Log.d(TAG, "ClipboardData: " + mClipboardUrl);

                    if (mClipboardUrl.isEmpty()){
                        showAlert(R.string.no_copy_title, R.string.no_copy, R.color.Warning);
                    }
                    else if (mClipboardUrl.contains("www.instagram.com/tv/") || mClipboardUrl.contains("www.instagram.com/p/")){
                        mFabProgressCircle.show();
                        Toasty.info(MainActivity.this ,getString(R.string.please_wait),0).show();
                        new InstaData(this, mClipboardUrl).execute();
                    }
                    else {
                        showAlert(R.string.correct_url_title, R.string.correct_url, R.color.Warning);
                    }
                }
                else {
                    showAlert(R.string.clipboard_error_title, R.string.clipboard_error, R.color.ERROR);
                }
            }

        } catch (Exception ex){
            showAlert(R.string.clipboard_error_title, R.string.clipboard_error, R.color.ERROR);
            ex.printStackTrace();
        }
    }

    public void showAlert(int title, int message, int color){
        Alerter.create(this)
                .setTitle(getString(title))
                .setText(getString(message))
                .setIcon(R.drawable.ic_error)
                .setBackgroundColorRes(color)
                .show();
    }
}
