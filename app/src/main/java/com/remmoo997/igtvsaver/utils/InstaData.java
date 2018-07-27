package com.remmoo997.igtvsaver.utils;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.remmoo997.igtvsaver.R;
import com.remmoo997.igtvsaver.activities.MainActivity;
import com.remmoo997.igtvsaver.activities.PhotoActivity;
import com.remmoo997.igtvsaver.activities.VideoActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;

public class InstaData extends AsyncTask<Void, Void, String> {

    private static final String TAG = "InstaData";
    private String mImageTitle = null;
    private final WeakReference<MainActivity> mActivityRef;
    private final String mUrl;

    public InstaData(MainActivity activity, String url){
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
