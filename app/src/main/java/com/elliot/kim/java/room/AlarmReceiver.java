package com.elliot.kim.java.room;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.room.Room;

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

            String channelName = "cat_note_channel";
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

        // 안통하네...
        // 데이터 베이스 자체를 불러서 적용하는게 맞는듯.
        // 서비스 실행하던지. 이건 오반데
        // 아니면 켯을때, sharedpref 보고 싹 갱신하기, 개오반데
        // 그냥 함수하나 호출로 끝날수있는거라. 최대한 경량화 해야돠ㅣ는데....
        // 만약 클래스에 저장한다면 어떻게 평가.
        // 현재시각이랑 비교하는 방법. 근데 걔도 getDate매번 호출해야됨. 매번은 아니고
        // 여기서 배열로 어떤걔 알람 끝낫는지 저장하기.
        // 알람 끝난거랑 비교해서, 클릭시, 비교하고 만약 number가 contain이면,
        // 해당 노트 AlarmSet -> fasle로 변경하기. 시작하자마자 수행하는 방법. 싹 업데이트
        // 클릭시마다 확인하기.
        // 싹 하는게 맞는듯.
        // adapter의 리스트에 적용시킬것.
        // 그러면 그 정보는 어디에. shared에 넣어놓고 원소삭제하는 방식으로..
        /*
        Note note = ((MainActivity) context).getNote(number);
        note.setAlarmSet(false);
        ((MainActivity) context).applyEditNote(note);

        AppDatabase database = Room.databaseBuilder(get, AppDatabase.class,
                "note_database")
                .fallbackToDestructiveMigration()
                .build();

         */
    }
}