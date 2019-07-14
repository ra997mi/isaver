package com.remmoo997.igtvsaver.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.widget.Switch;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.remmoo997.igtvsaver.R;
import com.remmoo997.igtvsaver.services.ClipboardService;
import com.remmoo997.igtvsaver.utils.InstaData;
import com.remmoo997.igtvsaver.utils.InstaStories;
import com.tapadoo.alerter.Alerter;

import es.dmoral.toasty.Toasty;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.FullscreenPromptBackground;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static String OPENED_FROM_OUTSIDE = null;
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

        Switch mSwitch = findViewById(R.id.fastlink);
        mSwitch.setChecked(mSharedPreferences.getBoolean("ServiceOn", false));
        mSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                startService(mClipboardIntent);
            } else {
                stopService(mClipboardIntent);
            }
            mSharedPreferences.edit().putBoolean("ServiceOn", b).apply();
        });

        FloatingActionButton mFabOpen = findViewById(R.id.getFab);
        mFabOpen.setOnClickListener(this);
        mFabOpen.setOnLongClickListener(this);

        mFabProgressCircle = findViewById(R.id.fabProgressCircle);

        if (mSharedPreferences.getBoolean("isFirstStarted", true)) {
            new MaterialTapTargetPrompt.Builder(MainActivity.this)
                    .setTarget(mFabOpen)
                    .setBackButtonDismissEnabled(false)
                    .setPrimaryText(getString(R.string.guide))
                    .setBackgroundColour(ContextCompat.getColor(this, R.color.semiDark))
                    .setPromptBackground(new FullscreenPromptBackground())
                    .setSecondaryText(getString(R.string.guide_description))
                    .show();
            mSharedPreferences.edit().putBoolean("isFirstStarted", false).apply();
        }

        if (getIntent() != null && getIntent().getDataString() != null) {
            OPENED_FROM_OUTSIDE = getIntent().getDataString();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getIntent() != null && getIntent().getDataString() != null) {
            OPENED_FROM_OUTSIDE = getIntent().getDataString();
        }
    }

    @Override
    public void onClick(View view) {
        try {
            if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                if (OPENED_FROM_OUTSIDE != null && (OPENED_FROM_OUTSIDE.contains("instagram.com/"))) {
                    mFabProgressCircle.show();
                    Toasty.custom(MainActivity.this, getString(R.string.please_wait), ContextCompat.getDrawable(this, R.drawable.wait), Toasty.LENGTH_SHORT, true).show();
                    new InstaData(this, instaJob(OPENED_FROM_OUTSIDE)).execute();
                } else {
                    ClipboardManager mClipboardService = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    if (mClipboardService != null) {
                        ClipData link = mClipboardService.getPrimaryClip();
                        ClipData.Item item = link.getItemAt(0);
                        String mClipboardUrl = item.getText().toString();

                        if (mClipboardUrl.isEmpty()) {
                            showAlert(R.string.no_copy_title, R.string.no_copy, R.color.Warning);
                        } else if (mClipboardUrl.contains("instagram.com/")) {
                            mFabProgressCircle.show();
                            Toasty.custom(MainActivity.this, getString(R.string.please_wait), ContextCompat.getDrawable(this, R.drawable.wait), Toasty.LENGTH_SHORT, true).show();
                            new InstaData(this, instaJob(mClipboardUrl)).execute();
                        } else {
                            showAlert(R.string.correct_url_title, R.string.correct_url, R.color.Warning);
                        }
                    } else {
                        showAlert(R.string.clipboard_error_title, R.string.clipboard_error, R.color.ERROR);
                    }
                }
            }

        } catch (Exception ex) {
            showAlert(R.string.clipboard_error_title, R.string.clipboard_error, R.color.ERROR);
            ex.printStackTrace();
        }
    }

    private static String instaJob(String x) {
        if (!x.contains("https://www.instagram.com/explore") &&
                !x.contains("https://www.instagram.com/accounts/activity") &&
                !x.contains("https://www.instagram.com/direct/inbox") &&
                !x.contains("https://www.instagram.com/direct/new") &&
                !x.contains("https://www.instagram.com/explore/search")) {
            if (x.contains("?")) {
                x = x.substring(0, x.lastIndexOf("?"));
                x = x + "?__a=1";
            } else {
                if (!x.endsWith("/")) {
                    x = x + "/?__a=1";
                } else {
                    x = x + "?__a=1";
                }
            }
        }
        return x;
    }

    public void showAlert(int title, int message, int color) {
        Alerter.create(this)
                .setTitle(getString(title))
                .setText(getString(message))
                .setIcon(R.drawable.ic_error)
                .setBackgroundColorRes(color)
                .show();
    }

    @Override
    public boolean onLongClick(View v) {
        try {
            if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                if (OPENED_FROM_OUTSIDE != null && !OPENED_FROM_OUTSIDE.contains("instagram.com/p/") && !OPENED_FROM_OUTSIDE.contains("instagram.com/tv/")) {
                    mFabProgressCircle.show();
                    Toasty.custom(MainActivity.this, getString(R.string.please_wait), ContextCompat.getDrawable(this, R.drawable.wait), Toasty.LENGTH_SHORT, true).show();
                    new InstaStories(this, instaJob(OPENED_FROM_OUTSIDE)).execute();
                } else {
                    ClipboardManager mClipboardService = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    if (mClipboardService != null) {
                        ClipData link = mClipboardService.getPrimaryClip();
                        ClipData.Item item = link.getItemAt(0);
                        String mClipboardUrl = item.getText().toString();

                        if (mClipboardUrl.isEmpty()) {
                            showAlert(R.string.no_copy_title, R.string.no_copy, R.color.Warning);
                        } else if (!mClipboardUrl.contains("instagram.com/p/") && !mClipboardUrl.contains("instagram.com/tv/")) {
                            mFabProgressCircle.show();
                            Toasty.custom(MainActivity.this, getString(R.string.please_wait), ContextCompat.getDrawable(this, R.drawable.wait), Toasty.LENGTH_SHORT, true).show();
                            new InstaStories(this, mClipboardUrl).execute();
                        } else {
                            showAlert(R.string.profile_link, R.string.profile_link_content, R.color.Warning);
                        }
                    } else {
                        showAlert(R.string.clipboard_error_title, R.string.clipboard_error, R.color.ERROR);
                    }
                }
            }
        } catch (Exception ex) {
            showAlert(R.string.clipboard_error_title, R.string.clipboard_error, R.color.ERROR);
        }
        return true;
    }
}
