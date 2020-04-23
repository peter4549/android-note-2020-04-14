package com.elliot.kim.java.room;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DeviceBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), "android.intent.action.BOOT_COMPLETED")) {
            Intent intentToAlarmReceiver = new Intent(context, AlarmReceiver.class);
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            SharedPreferences sharedPreferences = context.getSharedPreferences("alarm_information",
                    Context.MODE_PRIVATE);
            Map<String, ?> allEntries = sharedPreferences.getAll();
            int nb_entries = allEntries.size();
            int[] intKeySet = Arrays.stream(allEntries.keySet().toArray(new String[0])).mapToInt(Integer::parseInt).toArray();
            Arrays.sort(intKeySet);

            Calendar calendar = new GregorianCalendar();

            int count = 0;
            int number = 0;
            String title = "";
            String content = "";
            for(int i = 0; i < nb_entries; ++i) {
                String key = Integer.toString(intKeySet[i]);
                if(count == 0) {
                    number = sharedPreferences.getInt(key, 0);
                } else if (count == 1) {
                    calendar.setTimeInMillis(sharedPreferences.getLong(key, 0));
                } else if (count == 2) {
                    title = sharedPreferences.getString(key, "");
                } else if (count == 3) {
                    content = sharedPreferences.getString(key, "");
                }
                ++count;

                if (count == 4) {
                    count = 0;
                    intentToAlarmReceiver.putExtra(AlarmFragment.KEY_NUMBER, number);
                    intentToAlarmReceiver.putExtra(AlarmFragment.KEY_TITLE, title);
                    intentToAlarmReceiver.putExtra(AlarmFragment.KEY_CONTENT, content);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            context,
                            number,
                            intentToAlarmReceiver,
                            PendingIntent.FLAG_ONE_SHOT);

                    if(alarmManager != null) {
                        alarmManager.set(
                                AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                pendingIntent);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    calendar.getTimeInMillis(),
                                    pendingIntent);
                        }
                    }
                }
            }
            Date currentDateTime = calendar.getTime();
            String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
            Toast.makeText(context.getApplicationContext(),"[재부팅후] 다음 알람은 " + date_text + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();
        }
    }
}
