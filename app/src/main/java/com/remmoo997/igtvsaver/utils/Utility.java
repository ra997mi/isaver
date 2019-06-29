package com.remmoo997.igtvsaver.utils;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.Calendar;

public final class Utility {

    public static String getVideoName(@NonNull String mUrl) {
        try {
            String img = "IGTVSaver-VID-";
            String[] separated = mUrl.split("/");
            String mVideoName = separated[separated.length - 1];

            if (mVideoName.contains(".mp4"))
                mVideoName = img + mVideoName.substring(0, 8) + ".mp4";

            else if (mVideoName.contains(".m3u8"))
                mVideoName = img + mVideoName.substring(0, 8) + ".m3u8";

            else
                mVideoName = img + mVideoName.substring(0, 8) + ".mp4";

            return mVideoName;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "IGTVSaver-Video.mp4";
        }
    }

    public static String getPictureName(@NonNull String mUrl) {
        try {
            String img = "IGTVSaver-IMG-";
            String[] separated = mUrl.split("/");
            String mPictureName = separated[separated.length - 1];

            if (mPictureName.contains(".jpg"))
                mPictureName = img + mPictureName.substring(0, 8) + ".jpg";

            else if (mPictureName.contains(".gif"))
                mPictureName = img + mPictureName.substring(0, 8) + ".gif";

            else if (mPictureName.contains(".png"))
                mPictureName = img + mPictureName.substring(0, 8) + ".png";

            else
                mPictureName = img + mPictureName.substring(0, 8) + ".jpg";

            return mPictureName;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "IGTVSaver-Picture.jpg";
        }
    }

    public static String getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "x";
        }
    }

    public static int getYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }
}





