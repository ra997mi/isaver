package com.remmoo997.igtvsaver.utils;

import android.support.annotation.NonNull;
import android.util.Log;

public final class Utility {

    public static String getVideoName(@NonNull String mUrl) {
        try {
            String[] separated = mUrl.split("/");
            String myVideoName = separated[separated.length - 1];

            if (myVideoName.indexOf(".") > 0)
                myVideoName = myVideoName.substring(0, myVideoName.lastIndexOf("."));

            myVideoName = myVideoName.substring(0, 8);

            if(!myVideoName.contains(".mp4"))
                myVideoName = "IGTVSaver-VID-" +  myVideoName + ".mp4";

            return myVideoName;
        } catch (Exception ex){
            Log.i("Utility", "getVideoName: can't get video name");
            return "IGTVSaver-Video.mp4";
        }
    }
}





