package com.lyralabs.imfc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class RefreshService extends Service {
    private NotificationManager mNM;
    private int NOTIFICATION = 2;

    public class RefreshBinder extends Binder {
    	RefreshService getService() {
            return RefreshService.this;
        }
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mNM.cancel(NOTIFICATION);

        Toast.makeText(this, "Mobile Mfc schlieﬂt sich...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new RefreshBinder();

    private void showNotification() {
        CharSequence text = "keine neuen Nachrichten.";
        Notification notification = new Notification(R.drawable.android_icon_mfc, text, System.currentTimeMillis());
        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_FOREGROUND_SERVICE;
        
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Chats.class), 0);
        
        notification.setLatestEventInfo(this, "Mobile Mfc", text, contentIntent);

        mNM.notify(NOTIFICATION, notification);
    }
    
    public void bla() {
    	Toast.makeText(this, "bla!", Toast.LENGTH_SHORT).show();
    }
}