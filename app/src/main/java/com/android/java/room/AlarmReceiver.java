package com.android.java.room;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        int number = intent.getIntExtra(AlarmFragment.KEY_NUMBER, 0);
        String title = intent.getStringExtra(AlarmFragment.KEY_TITLE);
        String content = intent.getStringExtra(AlarmFragment.KEY_CONTENT);

        NotificationManager notificationManager = (NotificationManager)context.
                getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intentNotification = new Intent(context, MainActivity.class);
        intentNotification.setAction("RUN_FROM_ALARM");
        intentNotification.putExtra("NUMBER", number);
        intentNotification.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intentNotification, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setSmallIcon(R.drawable.time_8c9eff_240);

            String channelName = "Cat paws notes channel";
            String description = "This is an alarm channel for cat paw notes.";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel("default", channelName, importance);
            channel.setDescription(description);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        } else
            builder.setSmallIcon(R.mipmap.time_8c9eff_240);

        assert content != null;
        if (content.length() >= 20)
            content = content.substring(0, 20);
        builder.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setTicker("{TO SERVICE}")
                .setContentTitle(title)
                .setContentText(content)
                .setContentInfo("Contents of alarm set notes")
                .setContentIntent(pendingIntent);

        if(notificationManager != null) {
            notificationManager.notify(number, builder.build());

            Date currentDateTime = Calendar.getInstance().getTime();
            String dateText = new SimpleDateFormat("yyyy-MM-EE-a-hh-mm", Locale.getDefault()).format(currentDateTime);
            Toast.makeText(context.getApplicationContext(), dateText, Toast.LENGTH_LONG).show();
        }
    }
}
