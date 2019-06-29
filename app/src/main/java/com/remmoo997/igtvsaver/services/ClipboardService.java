package com.remmoo997.igtvsaver.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.remmoo997.igtvsaver.R;
import com.remmoo997.igtvsaver.activities.MainActivity;

public class ClipboardService extends Service implements OnPrimaryClipChangedListener {

    private ClipboardManager mClipboardManager;

    @Override
    public void onCreate() {
        try {
            mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (mClipboardManager != null) {
                mClipboardManager.addPrimaryClipChangedListener(this);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Intent getNotificationIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    private void showNotification() {
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "mIGTVSaver_ID1")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.link_download)))
                .setContentIntent(PendingIntent.getActivity(this, 0, getNotificationIntent(), PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.drawable.ic_notify)
                .setTicker(getString(R.string.link_download))
                .setContentTitle(getString(R.string.link_founded))
                .setContentText(getString(R.string.link_download))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setLargeIcon(bm)
                .setOngoing(false)
                .setOnlyAlertOnce(true)
                .setVibrate(new long[]{500, 500});

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            builder.setPriority(Notification.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.notify(100, builder.build());
    }

    private void clipboardCheck() {
        try {
            if (mClipboardManager != null) {
                ClipData link = mClipboardManager.getPrimaryClip();
                if (link != null) {
                    ClipData.Item item = link.getItemAt(0);
                    String mClipboardUrl = item.getText().toString();
                    if (mClipboardUrl.contains("instagram.com/")) {
                        showNotification();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onPrimaryClipChanged() {
        try {
            clipboardCheck();
        } catch (Exception ignore) {
        }
    }
}