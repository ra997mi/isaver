package com.remmoo997.igtvsaver.utils;

import android.content.Intent;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.util.Log;

import com.remmoo997.igtvsaver.R;
import com.remmoo997.igtvsaver.activities.GridActivity;
import com.remmoo997.igtvsaver.activities.MainActivity;
import com.remmoo997.igtvsaver.activities.PhotoActivity;
import com.remmoo997.igtvsaver.activities.VideoActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.lang.ref.WeakReference;

public class InstaData extends AsyncTask<Void, Void, Schema> {

    private String mImageTitle = null;
    private boolean isProfile = false;
    private boolean isVideo = false;
    private boolean isPicture = false;
    private boolean isMultiple = false;
    private static final String TAG = "InstaData";
    private final WeakReference<MainActivity> mActivityRef;
    private final String mUrl;

    public InstaData(@NonNull MainActivity activity, @NonNull String url) {
        mActivityRef = new WeakReference<>(activity);
        mUrl = url;
    }

    @Override
    protected Schema doInBackground(Void... voids) {
        JSONObject media = null;
        String jsonData;
        String userId = null;
        try {
            jsonData = Jsoup.connect(mUrl).ignoreContentType(true).execute().body();
            try {
                isProfile = false;
                media = new JSONObject(jsonData).getJSONObject("graphql").getJSONObject("shortcode_media");
            } catch (JSONException jsonException) {
                isProfile = true;
                userId = new JSONObject(jsonData).getJSONObject("graphql").getJSONObject("user").getString("id");
            }

            if (!isProfile && media != null) {
                String Type = media.getString("__typename");
                if (Type.equals("GraphImage")) {
                    String pic_url = media.getString("display_url");
                    isPicture = true;
                    mImageTitle = media.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text").trim();
                    return new Schema("SinglePicture", pic_url);
                } else if (Type.equals("GraphVideo")) {
                    String video_url = media.getString("video_url");
                    isVideo = true;
                    return new Schema("SingleVideo", video_url);
                } else {
                    isMultiple = true;
                    Schema multi = new Schema();
                    JSONArray nodes = media.getJSONObject("edge_sidecar_to_children").getJSONArray("edges");
                    mImageTitle = media.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text").trim();
                    for (int i = 0; i < nodes.length(); i++) {
                        String nodeType = nodes.getJSONObject(i).getJSONObject("node").getString("__typename");
                        if (nodeType.equals("GraphImage"))
                            multi.getMulti_data().add(nodes.getJSONObject(i).getJSONObject("node").getString("display_url"));
                        else {
                            multi.getMulti_data().add(nodes.getJSONObject(i).getJSONObject("node").getString("video_url"));
                        }
                    }
                    return multi;
                }
            } else if (isProfile && userId != null) {
                String api = "https://i.instagram.com/api/v1/users/" + userId + "/info/";
                jsonData = Jsoup.connect(api).ignoreContentType(true).execute().body();
                JSONObject data = new JSONObject(jsonData).getJSONObject("user");
                String Profile_url = data.getJSONObject("hd_profile_pic_url_info").getString("url");
                String username = data.getString("username");
                int followers = data.getInt("follower_count");
                int following = data.getInt("following_count");
                return new Schema(Profile_url, username, followers, following);
            }

        } catch (Error ex) {
            ex.printStackTrace();
            Log.e(TAG, "doInBackground: mClipboardUrl:" + mUrl, ex);
            return null;
        } catch (Exception ex) {
            Log.e(TAG, "doInBackground: mClipboardUrl:" + mUrl, ex);
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Schema data) {
        super.onPostExecute(data);

        // get a reference to the activity if it is still there
        MainActivity activity = mActivityRef.get();
        if (activity == null || activity.isFinishing()) return;

        activity.mFabProgressCircle.hide();
        try {
            if (data != null) {
                if (isProfile) {
                    Intent i = new Intent(activity, PhotoActivity.class);
                    i.putExtra("PictureUrl", data.getUSERNAME_PICTURE_URL());
                    i.putExtra("PictureName", Utility.getPictureName(data.getUSERNAME_PICTURE_URL()));
                    i.putExtra("PictureLink", mUrl);
                    i.putExtra("PictureTitle", "(@" + data.getUSERNAME() + ") Followers: " + data.getFOLLOWERS() + ", Following: " + data.getFOLLOWING());
                    activity.startActivity(i);
                    activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else if (isPicture) {
                    Intent i = new Intent(activity, PhotoActivity.class);
                    i.putExtra("PictureUrl", data.getSINGLE_POST_PICTURE_URL());
                    i.putExtra("PictureName", Utility.getPictureName(data.getUSERNAME_PICTURE_URL()));
                    i.putExtra("PictureLink", mUrl);
                    if (mImageTitle != null && !mImageTitle.isEmpty())
                        i.putExtra("PictureTitle", mImageTitle.trim());
                    else
                        i.putExtra("PictureTitle", "IGTVSaver");
                    activity.startActivity(i);
                    activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else if (isVideo) {
                    Intent i = new Intent(activity, VideoActivity.class);
                    i.putExtra("VideoUrl", data.getSINGLE_POST_VIDEO_URL());
                    i.putExtra("VideoName", Utility.getVideoName(data.getSINGLE_POST_VIDEO_URL()));
                    i.putExtra("VideoLink", mUrl);
                    activity.startActivity(i);
                    activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else if (isMultiple) {
                    Intent i = new Intent(activity, GridActivity.class);
                    i.putExtra("multi", data.getMulti_data());
                    i.putExtra("URL", mUrl);
                    if (mImageTitle != null && !mImageTitle.isEmpty())
                        i.putExtra("title", mImageTitle.trim());
                    else
                        i.putExtra("title", "IGTVSaver");
                    activity.startActivity(i);
                    activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            } else {
                activity.showAlert(R.string.no_video_title, R.string.no_video, R.color.ERROR);
            }
        } catch (Error ex) {
            Log.e(TAG, "onPostExecute: mClipboardUrl:" + mUrl + " mDownloadingUrl: " + data, ex);

        } catch (Exception ex) {
            Log.e(TAG, "onPostExecute: mClipboardUrl:" + mUrl + " mDownloadingUrl: " + data, ex);
        }
    }
}
