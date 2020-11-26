package com.remmoo997.igtvsaver.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.remmoo997.igtvsaver.R;


import es.dmoral.toasty.Toasty;

public class PhotoActivity extends AppCompatActivity {

    private PhotoView mImageView;
    private String mPhotoUrl, mPhotoName, mPhotoLink;
    private SpinKitView mSpinKitView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs.getBoolean("RotationLock", true))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_photo);

        mInterstitialAd = new InterstitialAd(PhotoActivity.this);
        mInterstitialAd.setAdUnitId(getString(R.string.ad_unit3));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mSpinKitView = findViewById(R.id.spin_kit_photo);

        mImageView = findViewById(R.id.container);
        Toolbar mToolbar = findViewById(R.id.toolbar_ph);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        TextView mPhotoTitle = findViewById(R.id.photo_title);
        mPhotoUrl = getIntent().getStringExtra("PictureUrl");
        mPhotoName = getIntent().getStringExtra("PictureName");
        mPhotoLink = getIntent().getStringExtra("PictureLink");
        mPhotoTitle.setText(getIntent().getStringExtra("PictureTitle"));

        LoadPhoto();
    }

    private void LoadPhoto() {
        try {
            Glide.with(PhotoActivity.this)
                    .load(mPhotoUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            mSpinKitView.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(mImageView);
        } catch (Exception ex) {
            ex.printStackTrace();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.download_image:
                if (isPermissionsGranted()) {
                    if (mInterstitialAd != null && mInterstitialAd.isLoaded())
                        mInterstitialAd.show();
                    getPhoto();
                }
                break;

            case R.id.share_image:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                shareIntent.putExtra(Intent.EXTRA_TEXT, mPhotoLink);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_link)));
                break;

            case R.id.copy_url_image:
                try {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newUri(getContentResolver(), getString(R.string.app_name), Uri.parse(mPhotoLink));
                    if (clipboard != null && clip != null) {
                        clipboard.setPrimaryClip(clip);
                        Toasty.success(this, getString(R.string.copied_to_clipboard), 0).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toasty.error(PhotoActivity.this, getString(R.string.cant_copy), 0).show();
                }
                break;
            case android.R.id.home:
                super.onBackPressed();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isPermissionsGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                RequestStoragePermission();
                return false;
            }
        } else {
            // Permission is automatically granted on sdk < 23 upon installation
            return true;
        }
    }

    private void RequestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    private void getPhoto() {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mPhotoUrl));
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, mPhotoName);
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm != null) {
                dm.enqueue(request);
            }
            Toasty.success(this, getString(R.string.downloading), 0).show();
        } catch (Exception ex) {
            ex.printStackTrace();
            Toasty.error(this, getString(R.string.error), 0).show();
        }
    }
}