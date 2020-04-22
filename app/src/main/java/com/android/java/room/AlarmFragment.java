package com.android.java.room;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import com.android.java.room.databinding.FragmentAlarmBinding;

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
    private FragmentAlarmBinding binding;
    private Note note;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_alarm,
                container, false);

        Calendar calendarNextNotification = new GregorianCalendar();
        Date currentDateTime = calendarNextNotification.getTime();
        SimpleDateFormat hourFormat = new SimpleDateFormat("KK", Locale.getDefault());
        SimpleDateFormat minuteFormat = new SimpleDateFormat("mm", Locale.getDefault());

        int prevHour = Integer.parseInt(hourFormat.format(currentDateTime));
        int prevMinute = Integer.parseInt(minuteFormat.format(currentDateTime));

        binding.timePicker.setIs24HourView(false);
        binding.timePicker.setHour(prevHour);
        binding.timePicker.setMinute(prevMinute);

        binding.buttonDate.setOnClickListener(onClickListener);
        binding.button.setOnClickListener(onClickListener);
        return binding.getRoot();
    }

    public void setNote(Note note) {
        this.note = note;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_date:
                    showDatePicker();
                    break;
                case R.id.button:
                    int year, month, dayOfMonth, hour, minute;
                    String buttonText = binding.buttonDate.getText().toString();
                    year = Integer.parseInt(buttonText.substring(0, 4));
                    month = Integer.parseInt(buttonText.substring(5, 6));
                    dayOfMonth = Integer.parseInt(buttonText.substring(7));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        hour = binding.timePicker.getHour();
                        minute = binding.timePicker.getMinute();
                    } else {
                        hour = binding.timePicker.getCurrentHour();
                        minute = binding.timePicker.getCurrentMinute();
                    }

                    Calendar calendar = new GregorianCalendar(year, month - 1, dayOfMonth,
                            hour, minute, 0);
                    String text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ",
                            Locale.getDefault()).format(calendar.getTime());
                    Toast.makeText(getContext(),text + "으로 알람이 설정되었습니다.", Toast.LENGTH_SHORT).show();

                    setAlarm(calendar);
                    ((MainActivity)MainActivity.mainActivityContext).originalOnBackPressed();
                    break;
            }
        }

        private void setAlarm(Calendar calendar) {
            ComponentName receiver = new ComponentName(Objects.requireNonNull(getActivity()), DeviceBootReceiver.class);
            PackageManager packageManager = getActivity().getPackageManager();
            Intent intent = new Intent(getActivity(), AlarmReceiver.class);
            intent.putExtra(KEY_NUMBER, note.getNumber());
            intent.putExtra(KEY_TITLE, note.getTitle());
            intent.putExtra(KEY_CONTENT, note.getContent());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    getActivity(),
                    note.getNumber(),
                    intent,
                    PendingIntent.FLAG_ONE_SHOT);
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
            packageManager.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.isFragment = true;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(Objects.requireNonNull(getActivity()),
                new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                binding.buttonDate.setText(String.format("%d-%d-%d", year, month + 1, dayOfMonth));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }
}
