package com.remmoo997.igtvsaver.utils;

import android.content.Intent;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.remmoo997.igtvsaver.R;
import com.remmoo997.igtvsaver.activities.GridActivity;
import com.remmoo997.igtvsaver.activities.MainActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class InstaStories extends AsyncTask<Void, Void, ArrayList<String>> {

    private final WeakReference<MainActivity> mActivityRef;
    private final String Profile_Url;
    private String username;

    public InstaStories(@NonNull MainActivity activity, String url) {
        mActivityRef = new WeakReference<>(activity);
        Profile_Url = url;
    }

    @Override
    protected ArrayList<String> doInBackground(Void... voids) {
        ArrayList<String> data = new ArrayList<>();
        try {
            username = extractUserName(Profile_Url);
            String API = "https://www.instadp.com/stories/#Replace_Me";
            String jet = API.replace("#Replace_Me", username);
            Elements elements = Jsoup.connect(jet).userAgent("Mozilla").get().select("ul[class=stories-list] > li[class=story] > div[class=story-post]");
            for (Element element : elements) {
                String img = element.select("img").attr("src");
                if (!img.equals(""))
                    data.add(img);
                else {
                    String vid = element.select("video").select("source").attr("src");
                    if (!vid.equals(""))
                        data.add(vid);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return data;
    }

    @Override
    protected void onPostExecute(ArrayList<String> data) {
        super.onPostExecute(data);

        MainActivity activity = mActivityRef.get();
        if (activity == null || activity.isFinishing()) return;

        activity.mFabProgressCircle.hide();
        try {
            if (data != null && data.size() > 0) {
                Intent i = new Intent(activity, GridActivity.class);
                i.putExtra("multi", data);
                i.putExtra("URL", Profile_Url);
                i.putExtra("title", "(@" + username + ") Stories");
                activity.startActivity(i);
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
            else{
                activity.showAlert(R.string.no_stories_title, R.string.no_stories, R.color.warningColor);
            }
        } catch (Error ignore) {
        } catch (Exception ignore) {
        }
    }

    private String extractUserName(String url) {
        if (url.contains("https://www.instagram.com/")) {
            url = url.replace("https://www.instagram.com/", "");
        } else if (url.contains("https://instagram.com/")) {
            url = url.replace("https://instagram.com/", "");
        }

        if (url.contains("?")) {
            url = url.replace(url.substring(url.indexOf("?")), "");
        }

        return url;
    }
}
