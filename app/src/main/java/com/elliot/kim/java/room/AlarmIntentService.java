package com.elliot.kim.java.room;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.room.Room;

import java.util.List;

public class AlarmIntentService extends IntentService {
    private final String packageName = "com.elliot.kim.java.room";

    public AlarmIntentService() {
        super("AlarmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int number = intent.getIntExtra("NUMBER", -1);
        if (number != -1) {
            AppDatabase database = Room.databaseBuilder(getApplication(), AppDatabase.class,
                    "note_database")
                    .fallbackToDestructiveMigration()
                    .build();

            NoteDao dao = database.dao();
            Note note = dao.getNoteByNumber(number);
            note.setAlarmSet(false);
            dao.update(note);
        }
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "AlarmIntentService done", Toast.LENGTH_SHORT).show();
    }

    public static boolean isAppRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> processInfoList = activityManager.getRunningAppProcesses();
        if (processInfoList != null)
        {
            for (final ActivityManager.RunningAppProcessInfo processInfo : processInfoList) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

}
