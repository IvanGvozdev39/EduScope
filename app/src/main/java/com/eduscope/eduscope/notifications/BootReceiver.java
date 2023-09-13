package com.eduscope.eduscope.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.eduscope.eduscope.sqlite.SQLiteHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Toast.makeText(context, "boot receiver's called", Toast.LENGTH_LONG).show();
            SQLiteHelper sqLiteHelper = new SQLiteHelper(context);
            List<String> titleList = sqLiteHelper.selectRemindersTitles();
            List<String> contentList = sqLiteHelper.selectRemindersContents();
            List<Long> alarmTimeList = sqLiteHelper.selectRemindersAlarmTimes();
            String[] title = titleList.toArray(new String[0]);
            String[] content = contentList.toArray(new String[0]);
            long[] alarmTimes = new long[alarmTimeList.size()];
            for (int i = 0; i < alarmTimeList.size(); i++) {
                alarmTimes[i] = alarmTimeList.get(i);
            }

            // Schedule alarms for each notification
            for (int i = 0; i < title.length; i++) {
                Intent notificationIntent = new Intent(context, ReminderBroadcastReceiver.class);
                notificationIntent.setAction("com.eduscope.eduscope.NOTIFICATION");
                notificationIntent.addCategory("android.intent.category.DEFAULT");
                notificationIntent.putExtra("title", title[i]);
                notificationIntent.putExtra("content", content[i]);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, i, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                // Parse the alarm time from the array and schedule the alarm
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date alarmDate = null;
                try {
                    alarmDate = dateFormat.parse(String.valueOf(alarmTimes[i]));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Calendar calendar = Calendar.getInstance();
                assert alarmDate != null;
                calendar.setTime(alarmDate);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
        }
    }
}
