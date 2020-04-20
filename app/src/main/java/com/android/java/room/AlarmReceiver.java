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
        NotificationManager notificationManager = (NotificationManager)context.
                getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intentNotification = new Intent(context, MainActivity.class);
        // 메인 액티비티를 실행하도록함.. 나같은 경우는.. 그냥 아니다..메인액티비티 실행, 플래그 던져주고,
        // 해당 노트 에딧플래크먼트 실행하도록..

        intentNotification.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intentNotification, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setSmallIcon(R.drawable.check_mark_green_240);


            String channelName = "알람채널!";
            String description = "뭘 넣어줘야하는가!";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel("default", channelName, importance);
            channel.setDescription(description);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        } else
            builder.setSmallIcon(R.mipmap.ic_launcher);

        builder.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setTicker("{FUCK}")
                .setContentTitle("드래그시보이는 타이틀")
                .setContentText("상태바 드래그시 서브 타이틀")
                .setContentInfo("INFO")
                .setContentIntent(pendingIntent);

        if(notificationManager != null) {
            notificationManager.notify(1234, builder.build());

            Date currentDateTime = Calendar.getInstance().getTime();
            String dateText = new SimpleDateFormat("yyyy-MM-EE-a-hh-mm", Locale.getDefault()).format(currentDateTime);
            Toast.makeText(context.getApplicationContext(), dateText, Toast.LENGTH_LONG).show();
        }
    }
}
