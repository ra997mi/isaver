package com.remmoo997.igtvsaver.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import com.remmoo997.igtvsaver.utils.Utility;
import com.tapadoo.alerter.Alerter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;

import es.dmoral.toasty.Toasty;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.FullscreenPromptBackground;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private ClipboardManager mClipboardService;
    private InterstitialAd mInterstitialAd;
    private Intent mClipboardIntent;
    private FABProgressCircle mFabProgressCircle;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make screen Portrait to disable Landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mClipboardIntent  = new Intent(this, ClipboardService.class);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.ad_unit2));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                startGame();
            }
        });
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mClipboardService = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);

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

        startGame();
        new Handler().postDelayed(this::showInterstitial, (long) 3000);

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

    }

    private void startGame() {
        if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
        }
    }

    private void showInterstitial() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            startGame();
        }
    }

    @Override
    public void onClick(View view) {
        try {
            if (mClipboardService != null) {
                ClipData link = mClipboardService.getPrimaryClip();
                ClipData.Item item = link.getItemAt(0);
                String mClipboardUrl = item.getText().toString();
                Log.d(TAG, "ClipboardData: " + mClipboardUrl);

                if (mClipboardUrl.isEmpty()){
                    Alerter.create(this)
                            .setTitle(getString(R.string.app_name))
                            .setText(getString(R.string.no_copy))
                            .setIcon(R.drawable.ic_error)
                            .setBackgroundColorRes(R.color.Warning)
                            .show();
                }
                else if (mClipboardUrl.contains("www.instagram.com/tv/")){
                    mFabProgressCircle.show();
                    Toasty.info(MainActivity.this ,getString(R.string.please_wait),0).show();
                    new getTV(this, mClipboardUrl).execute();
                }
                else {
                    Alerter.create(this)
                            .setTitle(getString(R.string.app_name))
                            .setText(getString(R.string.correct_url))
                            .setIcon(R.drawable.ic_error)
                            .setBackgroundColorRes(R.color.Warning)
                            .show();
                }
            }
            else {
                Alerter.create(this)
                        .setTitle(getString(R.string.app_name))
                        .setText(getString(R.string.clipboard_error))
                        .setIcon(R.drawable.ic_error)
                        .setBackgroundColorRes(R.color.ERROR)
                        .show();
            }

        } catch (Exception ex){
            Log.e(TAG, "onClick: " + mClipboardService, ex);
            ex.printStackTrace();
        }

    }

    private static class getTV extends AsyncTask<Void, Void, String>{

        private final WeakReference<MainActivity> mActivityRef;
        private final String mUrl;

        getTV(MainActivity activity, String url){
            mActivityRef = new WeakReference<>(activity);
            mUrl = url;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                if (mUrl != null) {
                    Document document = Jsoup.connect(mUrl).get();
                    Elements elements = document.select("meta[property=og:video]");
                    return elements.attr("content");
                }
            } catch (Error ex) {
                ex.printStackTrace();
                Log.e(TAG, "doInBackground: mClipboardUrl:" + mUrl,ex);
            }
            catch (Exception ex) {
                Log.e(TAG, "doInBackground: mClipboardUrl:" + mUrl,ex);
            }
            return "ERROR";
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            // get a reference to the activity if it is still there
            MainActivity activity = mActivityRef.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mFabProgressCircle.hide();
            try {
                if (data != null && data.contains(".mp4") && !data.equals("ERROR")){
                    Intent i = new Intent(activity, VideoActivity.class);
                    i.putExtra("VideoUrl", data);
                    i.putExtra("VideoName", Utility.getVideoName(data));
                    activity.startActivity(i);
                    activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            } catch (Error ex){
                Log.e(TAG, "onPostExecute: mClipboardUrl:" + mUrl + " mDownloadingUrl: " + data,ex);

            } catch (Exception ex){
                Log.e(TAG, "onPostExecute: mClipboardUrl:" + mUrl + " mDownloadingUrl: " + data,ex);
            }
        }
    }
}
