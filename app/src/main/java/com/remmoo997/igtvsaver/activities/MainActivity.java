package com.remmoo997.igtvsaver.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
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

    @Override
    public void onClick(View view) {
        try {
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
                    new getTV(this, mClipboardUrl).execute();
                }
                else {
                    showAlert(R.string.correct_url_title, R.string.correct_url, R.color.Warning);
                }
            }
            else {
                showAlert(R.string.clipboard_error_title, R.string.clipboard_error, R.color.ERROR);
            }

        } catch (Exception ex){
            showAlert(R.string.clipboard_error_title, R.string.clipboard_error, R.color.ERROR);
            Log.e(TAG, "onClick: " + mClipboardService, ex);
            ex.printStackTrace();
        }

    }

    private void showAlert(int title, int message, int color){
        Alerter.create(this)
                .setTitle(getString(title))
                .setText(getString(message))
                .setIcon(R.drawable.ic_error)
                .setBackgroundColorRes(color)
                .show();
    }

    private static class getTV extends AsyncTask<Void, Void, String>{

        private String mImageTitle = null;
        private final WeakReference<MainActivity> mActivityRef;
        private final String mUrl;

        getTV(MainActivity activity, String url){
            mActivityRef = new WeakReference<>(activity);
            mUrl = url;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                Elements element;
                if (mUrl != null) {
                    String data;
                    Document document = Jsoup.connect(mUrl).get();
                    element = document.select("meta[property=og:video]");
                    data = element.attr("content");

                    if (data != null && !data.isEmpty()){
                        return data;
                    } else {
                        Elements title = document.select("meta[property=og:description]");
                        mImageTitle = title.attr("content");
                        mImageTitle = mImageTitle.substring(0, mImageTitle.indexOf("on Instagram"));
                        element = document.select("meta[property=og:image]");
                        data = element.attr("content");
                        if (data != null && !data.isEmpty())
                            return data;
                    }

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
                    i.putExtra("VideoLink", mUrl);
                    activity.startActivity(i);
                    activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
                else if (data != null && data.contains(".jpg") && !data.equals("ERROR")){
                    Intent i = new Intent(activity, PhotoActivity.class);
                    i.putExtra("PictureUrl", data);
                    i.putExtra("PictureName", Utility.getPictureName(data));
                    i.putExtra("PictureLink", mUrl);
                    if (mImageTitle != null && !mImageTitle.isEmpty())
                        i.putExtra("PictureTitle", mImageTitle.trim());
                    else
                        i.putExtra("PictureTitle", "IGTVSaver");
                    activity.startActivity(i);
                    activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
                else {
                    activity.showAlert(R.string.no_video_title, R.string.no_video, R.color.ERROR);
                }
            } catch (Error ex){
                Log.e(TAG, "onPostExecute: mClipboardUrl:" + mUrl + " mDownloadingUrl: " + data,ex);

            } catch (Exception ex){
                Log.e(TAG, "onPostExecute: mClipboardUrl:" + mUrl + " mDownloadingUrl: " + data,ex);
            }
        }
    }
}
