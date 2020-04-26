package com.elliot.kim.java.room;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.java.room.R;
import com.android.java.room.databinding.FragmentAlarmBinding;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

public class AlarmFragment extends Fragment {
    static final String KEY_NUMBER = "kEY_NUMBER";
    static final String KEY_TITLE = "kEY_TITLE";
    static final String KEY_CONTENT = "kEY_CONTENT";
    private MainActivity activity;
    private FragmentAlarmBinding binding;
    private Note note;
    private Boolean isAlarmSet;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_alarm,
                container, false);
        setTimePickerTextColor(binding.timePicker, R.color.colorLightBlue50);

        Calendar calendar = new GregorianCalendar();
        Date currentTime = calendar.getTime();

        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());
        SimpleDateFormat dayOfMonthFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat hourFormat = new SimpleDateFormat("kk", Locale.getDefault());
        SimpleDateFormat minuteFormat = new SimpleDateFormat("mm", Locale.getDefault());

        int prevYear = Integer.parseInt(yearFormat.format(currentTime));
        int prevMonth = Integer.parseInt(monthFormat.format(currentTime));
        int prevDayOfMonth = Integer.parseInt(dayOfMonthFormat.format(currentTime));
        int prevHour = Integer.parseInt(hourFormat.format(currentTime));
        int prevMinute = Integer.parseInt(minuteFormat.format(currentTime));

        binding.buttonSetDate.setText(String.format("%d년 %d월 %d일",
                prevYear, prevMonth, prevDayOfMonth));

        binding.timePicker.setIs24HourView(false);
        binding.timePicker.setHour(prevHour);
        binding.timePicker.setMinute(prevMinute);

        binding.buttonSetDate.setOnClickListener(onClickListener);
        binding.buttonSetAlarm.setOnClickListener(onClickListener);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.isAlarmFragment = true;
        isAlarmSet = note.getAlarmSet();

        if(isAlarmSet) {
            binding.textViewCurrentTimeSet.setVisibility(View.VISIBLE);
            binding.textViewCurrentTime.setVisibility(View.VISIBLE);

            SharedPreferences preferences = getActivity().getSharedPreferences(
                    "alarm_preferences",
                    Context.MODE_PRIVATE);
            Long what = preferences.getLong(note.getNumber()+"1", 0);
            String pattern = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            String date = formatter.format(new Timestamp(what));
            binding.textViewCurrentTime.setText(date);
        } else {
            binding.textViewCurrentTimeSet.setVisibility(View.INVISIBLE);
            binding.textViewCurrentTime.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.isAlarmFragment = false;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_set_date:
                    showDatePicker();
                    break;
                case R.id.button_set_alarm:
                    int year, month, dayOfMonth, hour, minute;
                    String buttonText = binding.buttonSetDate.getText().toString();
                    buttonText = buttonText.replaceAll("[^0-9]","");

                    year = Integer.parseInt(buttonText.substring(0, 4));
                    month = Integer.parseInt(buttonText.substring(4, 5));
                    dayOfMonth = Integer.parseInt(buttonText.substring(5));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        hour = binding.timePicker.getHour();
                        minute = binding.timePicker.getMinute();
                    } else {
                        hour = binding.timePicker.getCurrentHour();
                        minute = binding.timePicker.getCurrentMinute();
                    }

                    Calendar calendar = new GregorianCalendar(year, month - 1, dayOfMonth,
                            hour, minute, 0);
                    setAlarm(calendar);

                    activity.originalOnBackPressed();
                    break;
            }
        }

        private void setAlarm(Calendar calendar) {
            ComponentName receiver = new ComponentName(Objects.requireNonNull(getActivity()), DeviceBootReceiver.class);
            PackageManager manager = getActivity().getPackageManager();
            Intent intent = new Intent(getActivity(), AlarmReceiver.class);

            int number = note.getNumber();
            String title = note.getTitle();
            String content = note.getContent();
            assert content != null;
            if (content.length() > 16)
                content = content.substring(0, 16);

            intent.putExtra(KEY_NUMBER, number);
            intent.putExtra(KEY_TITLE, title);
            intent.putExtra(KEY_CONTENT, content);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    activity,
                    number,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
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

            /* Since set does not allow data duplication,
             data loss occurs when the title and content are the same.
            Set<String> stringSet = new HashSet<String>();
            stringSet.add(title);
            stringSet.add(content);
             */

            saveAlarmPreferences(number, calendar.getTimeInMillis(), title, content);

            note.setAlarmSet(true);
            activity.applyEditNote(note);

            manager.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            String text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ",
                    Locale.getDefault()).format(calendar.getTime());
            Toast.makeText(getContext(),text + "으로 알림이 설정되었습니다.", Toast.LENGTH_SHORT).show();
        }
    };

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(Objects.requireNonNull(getActivity()),
                (view, year, month, dayOfMonth) -> binding.buttonSetDate.setText(String.format("%d년 %d월 %d일",
                        year, month + 1, dayOfMonth)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveAlarmPreferences(int number, long alarmTime, String title, String content) {
        SharedPreferences sharedPreferences = activity.
                getSharedPreferences("alarm_preferences",
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(number + "0", number);
        editor.putLong(number + "1", alarmTime);
        editor.putString(number + "2", title);
        editor.putString(number + "3", content);
        editor.apply();
    }

    private void setTimePickerTextColor(TimePicker timePicker, final int id) {
        final Resources system = Resources.getSystem();
        final int color = ContextCompat.getColor(Objects.requireNonNull(getContext()), id);

        int hourNumberPickerId = system.getIdentifier("hour", "id", "android");
        int minuteNumberPickerId = system.getIdentifier("minute", "id", "android");
        int amPmNumberPickerId = system.getIdentifier("amPm", "id", "android");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ((NumberPicker) timePicker.findViewById(hourNumberPickerId)).setTextColor(color);
            ((NumberPicker) timePicker.findViewById(minuteNumberPickerId)).setTextColor(color);
            ((NumberPicker) timePicker.findViewById(amPmNumberPickerId)).setTextColor(color);
        } else {
            NumberPicker hourNumberPicker = timePicker.findViewById(hourNumberPickerId);
            NumberPicker minuteNumberPicker = timePicker.findViewById(minuteNumberPickerId);
            NumberPicker amPmNumberPicker = timePicker.findViewById(amPmNumberPickerId);

            setNumberPickerTextColor(hourNumberPicker, color);
            setNumberPickerTextColor(minuteNumberPicker, color);
            setNumberPickerTextColor(amPmNumberPicker, color);
        }
    }

    private void setNumberPickerTextColor(NumberPicker numberPicker, final int color){
        final int count = numberPicker.getChildCount();

        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);

            try{
                Field wheelPaintField = numberPicker.getClass().getDeclaredField("mSelectorWheelPaint");
                wheelPaintField.setAccessible(true);

                ((Paint)wheelPaintField.get(numberPicker)).setColor(color);
                ((EditText)child).setTextColor(color);
                numberPicker.invalidate();
            }
            catch(NoSuchFieldException | IllegalAccessException | IllegalArgumentException e){
                e.printStackTrace();
            }
        }
    }
}
