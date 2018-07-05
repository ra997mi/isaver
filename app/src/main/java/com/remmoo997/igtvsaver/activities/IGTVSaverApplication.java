package com.remmoo997.igtvsaver.activities;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

public class IGTVSaverApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, "ca-app-pub-3836303927954880~9778190433");
    }
}
