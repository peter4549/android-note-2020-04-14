package com.elliot.kim.java.dimcatnote;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.android.java.room.R;

public class AlarmReceiver extends BroadcastReceiver {

    /* Considered unnecessary function
    public interface OnAlarmReceiverListener {
        public void onAlarmReceiver(int number);
    }
     */

    @Override
    public void onReceive(Context context, Intent intent) {
        int number = intent.getIntExtra(AlarmFragment.KEY_NUMBER, 0);
        String title = intent.getStringExtra(AlarmFragment.KEY_TITLE);
        String content = intent.getStringExtra(AlarmFragment.KEY_CONTENT);

        NotificationManager manager = (NotificationManager)context.
                getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intentNotification = new Intent(context, MainActivity.class);
        intentNotification.setAction("ALARM_ACTION");
        intentNotification.putExtra("NUMBER", number);
        intentNotification.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                number,
                intentNotification,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setSmallIcon(R.drawable.time_8c9eff_240);

            String channelName = "dim_cat_note_channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            String description = "Notification channel for the Cat Note";

            NotificationChannel channel = new NotificationChannel("default", channelName, importance);
            channel.setDescription(description);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        } else
            builder.setSmallIcon(R.mipmap.time_8c9eff_240);

        builder.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(content)
                .setContentInfo("Content of note set as notification")
                .setContentIntent(pendingIntent);

        if(manager != null)
            manager.notify(number, builder.build());

        Intent serviceIntent = new Intent(context, AlarmIntentService.class);
        serviceIntent.putExtra("NUMBER", number);
        context.startService(serviceIntent);
    }
}