package com.eduscope.eduscope.notes;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

import com.eduscope.eduscope.R;
import com.eduscope.eduscope.notifications.ReminderBroadcastReceiver;
import com.eduscope.eduscope.sqlite.SQLiteHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class CalendarDialog extends DialogFragment implements CalendarAdapter.OnDateSelectedListener {
    private CalendarAdapter mAdapter;
    private TextView mMonthTextView;
    private TextView mYearTextView;
    private View globalView;
    private LinearLayout yearPickerViewMode;
    private LinearLayout monthSelectorLayout;
    private GridView daysGridView;
    private String noteTitle, noteContent;

    public CalendarDialog(String noteTitle, String noteContent) {
        this.noteTitle = noteTitle;
        this.noteContent = noteContent;
    }

    public static CalendarDialog newInstance() {
        return new CalendarDialog(newInstance().noteTitle, newInstance().noteContent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.RoundedCornerDialogStyle);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dialog_calendar, container, false);
        globalView = view;

        // Find views
        mMonthTextView = view.findViewById(R.id.monthTextView);
        monthSelectorLayout = view.findViewById(R.id.monthSelectorLayout);
        yearPickerViewMode = view.findViewById(R.id.year_picker_view_mode);
        daysGridView = view.findViewById(R.id.daysGridView);
        mYearTextView = view.findViewById(R.id.yearTextView);
        mYearTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show year picker dialog
                // Create a dialog with a custom layout that contains a NumberPicker for selecting years
                daysGridView.setVisibility(View.GONE);
                monthSelectorLayout.setVisibility(View.GONE);
                yearPickerViewMode.setVisibility(View.VISIBLE);
                setCancelable(false);
                final NumberPicker yearPicker = view.findViewById(R.id.numberPicker);

                // Set the min and max values for the NumberPicker
                yearPicker.setMinValue(mAdapter.getCurrentYear());
                yearPicker.setMaxValue(mAdapter.getCurrentYear() + 10);
                //yearPicker.setMinValue(MIN_YEAR);
                //yearPicker.setMaxValue(MAX_YEAR);
                yearPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                yearPicker.setWrapSelectorWheel(false);
                yearPicker.setValue(Integer.parseInt(mYearTextView.getText().toString())); //с расчетом на то, что год всегда будет отображаться правильно

                Button setBtn = view.findViewById(R.id.btnSet);
                setBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Update the yearTextView with the selected year
                        yearPickerViewMode.setVisibility(View.GONE);
                        daysGridView.setVisibility(View.VISIBLE);
                        monthSelectorLayout.setVisibility(View.VISIBLE);
                        setCancelable(true);
                        mAdapter.setYear(yearPicker.getValue());
                        updateMonthYear();
                    }
                });

                // Set up the Cancel button to dismiss the dialog without updating the yearTextView
                Button cancelButton = view.findViewById(R.id.btnCancel);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        yearPickerViewMode.setVisibility(View.GONE);
                        daysGridView.setVisibility(View.VISIBLE);
                        monthSelectorLayout.setVisibility(View.VISIBLE);
                        setCancelable(true);
                    }
                });
            }
        });
        GridView daysGridView = view.findViewById(R.id.daysGridView);
        ImageView previousMonthButton = view.findViewById(R.id.previousMonthButton);
        ImageView nextMonthButton = view.findViewById(R.id.nextMonthButton);

        // Initialize calendar adapter
        mAdapter = new CalendarAdapter(requireContext(), Calendar.getInstance());
        daysGridView.setAdapter(mAdapter);

        // Set month and year
        updateMonthYear();

        // Set listeners for month navigation buttons
        previousMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.previousMonth();
                updateMonthYear();
            }
        });
        nextMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.nextMonth();
                updateMonthYear();
            }
        });

        // Set the background of the root LinearLayout to the rounded corner drawable
        view.findViewById(R.id.monthSelectorLayout).setBackgroundResource(R.drawable.round_back_white_10);

        mAdapter.setOnDateSelectedListener(this);

        return view;
    }

    private void updateMonthYear() {
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        int month = mAdapter.getCalendar().get(Calendar.MONTH);
        String[] months = getResources().getStringArray(R.array.months_array);
        mMonthTextView.setText(months[month]);
        mYearTextView.setText(yearFormat.format(mAdapter.getCalendar().getTime()));
    }

    @Override
    public void onDateSelected(Date date) {
        //Handling the selected date
        //учесть момент, когда нажата пустая область
        Log.d("CalendarDialog", "Selected date: " + date.toString());
        LinearLayout hourMinutePickerViewMode = globalView.findViewById(R.id.hour_minute_picker);
        //yearPickerViewMode.setVisibility(View.GONE);
        monthSelectorLayout.setVisibility(View.GONE);
        daysGridView.setVisibility(View.GONE);
        hourMinutePickerViewMode.setVisibility(View.VISIBLE);
        setCancelable(false);
        AppCompatButton setBtn = globalView.findViewById(R.id.btnSet_hoursMinutes);
        AppCompatButton cancelBtn = globalView.findViewById(R.id.btnCancel_hoursMinutes);
        NumberPicker hourPicker = globalView.findViewById(R.id.numberPicker_hoursMinutes);
        NumberPicker minutePicker = globalView.findViewById(R.id.numberPicker_hoursMinutes2);
        TextView title = globalView.findViewById(R.id.hour_minute_picker_title);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1; // Note: Month is zero-based in Calendar, so add 1
        int year = calendar.get(Calendar.YEAR);

        Resources resources = getResources();
        String[] monthArr = resources.getStringArray(R.array.months_array_genitive_case);

        title.setText(day + " " + monthArr[month - 1] + " " + year);

        hourPicker.setMinValue(0); //эта херня должна быть зависима от часового пояса. У кого-то 12-часовой формат
        minutePicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        minutePicker.setMaxValue(59);
        hourPicker.setValue(hourPicker.getMinValue());
        minutePicker.setValue(minutePicker.getMinValue());
        hourPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        minutePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        hourPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format("%02d", value);
            }
        });
        minutePicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format("%02d", value);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                monthSelectorLayout.setVisibility(View.VISIBLE);
                daysGridView.setVisibility(View.VISIBLE);
                hourMinutePickerViewMode.setVisibility(View.GONE);
                hourPicker.setValue(hourPicker.getMinValue());
                minutePicker.setValue(minutePicker.getMinValue());
                setCancelable(true);
            }
        });

        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour = hourPicker.getValue();
                int minute = minutePicker.getValue();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Create the NotificationChannel with all the parameters
                    NotificationChannel channel = new NotificationChannel("default", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setDescription("This is my notification channel.");

                    // Register the channel with the system
                    NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.createNotificationChannel(channel);
                }

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month - 1); // Calendar.MONTH is zero-based
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                Intent intent = new Intent(getContext(), ReminderBroadcastReceiver.class);
                intent.setAction("com.eduscope.eduscope.NOTIFICATION");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.putExtra("title", noteTitle);
                intent.putExtra("content", noteContent);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }

                SQLiteHelper sqLiteHelper = new SQLiteHelper(getContext());
                sqLiteHelper.insertReminders(noteTitle, noteContent, calendar.getTimeInMillis());

                Toast.makeText(getContext(), R.string.reminder_set, Toast.LENGTH_SHORT).show();
                monthSelectorLayout.setVisibility(View.VISIBLE);
                daysGridView.setVisibility(View.VISIBLE);
                hourMinutePickerViewMode.setVisibility(View.GONE);
                dismiss();
            }
        });

    }
}