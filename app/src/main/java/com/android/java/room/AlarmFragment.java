package com.android.java.room;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.java.room.databinding.FragmentAlarmBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class AlarmFragment extends Fragment {
    private MainActivity activity;
    private FragmentAlarmBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_alarm,
                container, false);
        //binding.timePicker.setIs24HourView(true);

        Calendar calendarNextNotification = new GregorianCalendar();
       // calendarNextNotification.setTimeInMillis(millis);
        Date nextDateTime = calendarNextNotification.getTime();
        String dateText = new SimpleDateFormat("MM월 dd일 EE요일 a hh시 mm분", Locale.getDefault()).format(nextDateTime);
        Toast.makeText(getContext(), dateText + "으로 알람이 설정되었습니다.", Toast.LENGTH_LONG).show();

        Date currentDateTime = calendarNextNotification.getTime();
        SimpleDateFormat hourFormat = new SimpleDateFormat("KK", Locale.getDefault());
        SimpleDateFormat minuteFormat = new SimpleDateFormat("mm", Locale.getDefault());

        int prevHour = Integer.parseInt(hourFormat.format(currentDateTime));
        int prevMinute = Integer.parseInt(minuteFormat.format(currentDateTime));

        binding.timePicker.setHour(prevHour);
        binding.timePicker.setMinute(prevMinute);

        binding.buttonYear.setOnClickListener(onClickListener);
        binding.button.setOnClickListener(onClickListener);
        return binding.getRoot();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_year:
                    showDatePicker();
                    break;
                case R.id.button:
                    int hour, minute;
                    String amOrPm;
                    hour = binding.timePicker.getHour();
                    minute = binding.timePicker.getMinute();
                    //amOrPm = binding.timePicker.get();
                    //Log.d("TTRRTRTR", amOrPm);


                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    int yy = calendar.get(Calendar.YEAR);

                    Date currentDateTime = calendar.getTime();
                    String dateText = new SimpleDateFormat("하하하 MM월 dd일 EE요일 a hh시 mm분", Locale.getDefault()).format(currentDateTime);
                    Toast.makeText(getContext(), dateText + "으로 알람이 설정되었습니다."+yy+"A", Toast.LENGTH_LONG).show();
                    break;
            }
        }

        private void notify(Calendar calendar) {
            // 여기에 sharedpreference 사용해서, 재부팅시에 값 사용할 수 있도록 하면 될듯. 던져줄 값.
            // 노트 아이디랑 설정시간, 만약 노트가 삭제되었으면, 음 ㅅㅂ 어떡하지 ㅋㅋ
            // 가 아니라, 일치하는 id가 없으면, 그냥 안띄우는 걸로.. 혹은 그냥 띄워버리거나.
            // 그니까 일딴 쉐어에는 아이디만 던져주고, 알람시간되면 해당 id로부터 어댑터를 스캔하고 있으면 알람, 즉 소리내고 지랄하기전,
            // 체크하고, 없으면 자동 버스트, 가 아니고 알람 해제하는거 잇으면 그걸로...
            PackageManager packageManager = activity.getPackageManager();
            ComponentName receiver = new ComponentName(activity, DeviceBootReceiver.class);
            Intent intent = new Intent(activity, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);
            AlarmManager alarmManager = (AlarmManager)activity.getSystemService(Context.ALARM_SERVICE);

            if(alarmManager != null) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        pendingIntent);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(), pendingIntent);
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
        showDatePicker();


    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.isFragment = false;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                binding.buttonYear.setText(String.format("%d-%d-%d", year, month, dayOfMonth));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }
}
