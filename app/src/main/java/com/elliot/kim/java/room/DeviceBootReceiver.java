package com.elliot.kim.java.room;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Objects;

public class DeviceBootReceiver extends BroadcastReceiver {
    private final String TAG = "DeviceBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), "android.intent.action.BOOT_COMPLETED")) {
            Intent intentToAlarmReceiver = new Intent(context, AlarmReceiver.class);
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            SharedPreferences preferences = context.getSharedPreferences("alarm_preferences",
                    Context.MODE_PRIVATE);
            Map<String, ?> allEntries = preferences.getAll();

            int nb_entries = allEntries.size();
            int[] intKeySet = Arrays.stream(allEntries.keySet().toArray(new String[0])).
                    mapToInt(Integer::parseInt).toArray();
            Arrays.sort(intKeySet);

            Calendar calendar = new GregorianCalendar();

            int count = 0;
            int number = 0;
            String title = "";
            String content = "";
            for(int i = 0; i < nb_entries; ++i) {
                String key = Integer.toString(intKeySet[i]);
                if(count == 0) {
                    number = preferences.getInt(key, 0);
                } else if (count == 1) {
                    calendar.setTimeInMillis(preferences.getLong(key, 0));
                } else if (count == 2) {
                    title = preferences.getString(key, "");
                } else if (count == 3) {
                    content = preferences.getString(key, "");
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
            Log.d(TAG, "DeviceBootReceiver has been called.");
        }
    }
}
