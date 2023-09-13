package com.eduscope.eduscope.notes;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eduscope.eduscope.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarAdapter extends BaseAdapter {

    private final Context mContext;
    private final Calendar mCalendar;
    private final Date mToday;
    private final int mFirstDayOfWeek;
    private OnDateSelectedListener mListener;

    public interface OnDateSelectedListener {
        void onDateSelected(Date date);
    }

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        mListener = listener;
    }

    // ...

    public CalendarAdapter(Context context, Calendar calendar) {
        mContext = context;
        mCalendar = (Calendar) calendar.clone();
        mToday = new Date();
        mFirstDayOfWeek = Calendar.MONDAY;
    }


    public int getCurrentYear() {
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTime(mToday);
        return todayCalendar.get(Calendar.YEAR);
    }


    public void setYear(int year) {
        mCalendar.set(Calendar.YEAR, year);
        notifyDataSetChanged();
    }

    public void previousMonth() {
        mCalendar.add(Calendar.MONTH, -1);
        notifyDataSetChanged();
    }

    public void nextMonth() {
        mCalendar.add(Calendar.MONTH, 1);
        notifyDataSetChanged();
    }

    public Calendar getCalendar() {
        return mCalendar;
    }

    @Override
    public int getCount() {
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int daysInMonth = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int offset = mCalendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
        if (offset < 0) {
            offset += 7;
        }
        int totalDays = daysInMonth + offset;
        int rows = totalDays / 7;
        if (totalDays % 7 > 0) {
            rows++;
        }
        return rows * 7;
    }

    @Override
    public Calendar getItem(int position) {
        int row = position / 7;
        int column = position % 7;

        mCalendar.set(Calendar.DAY_OF_MONTH, 1); // Set the day of the month to 1
        int offset = mCalendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
        if (offset < 0) {
            offset += 7;
        }
        int day = 1 - offset + row * 7 + column;

        if (day <= 0 || day > mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            return null;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), day);
            return calendar;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.day_layout, parent, false);
        } else {
            view = convertView;
        }

        RelativeLayout rlDay = view.findViewById(R.id.rl_day);
        TextView tvDay = view.findViewById(R.id.tv_day);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar = getItem(position);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        if (calendar != null) {
            // Compare only the weekday, month and date of the month
            Calendar todayCalendar = Calendar.getInstance();
            todayCalendar.setTime(mToday);
            boolean isToday = calendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)
                    && calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR);
            Log.d("timedate", "calendar day of year: " + calendar.get(Calendar.DAY_OF_YEAR) + "\ntoday day of year: " + todayCalendar.get(Calendar.DAY_OF_YEAR) +
                    "\ncalendar year: " + calendar.get(Calendar.YEAR) + "\ntoday year: " + todayCalendar.get(Calendar.YEAR));

            int dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek + 7) % 7;
            SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
            tvDay.setText(sdf.format(calendar.getTime()));

            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                /*Typeface typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);
                tvDay.setTypeface(typeface);*/
                tvDay.setTextColor(mContext.getResources().getColor(R.color.incorrect_answer_red));
            }

            if (isToday) {
                rlDay.setBackgroundResource(R.drawable.round_back_blue_50_opacity);
            } else {
                rlDay.setBackgroundColor(Color.TRANSPARENT);
            }
        } else {
            tvDay.setText("");
        }
        rlDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the Date object for the selected day
                try {
                    Date date = getItem(position).getTime();

                    // Call the onDateSelected() method of the listener, if it's not null
                    if (mListener != null) {
                        mListener.onDateSelected(date);
                    }
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
            }
        });

        return view;
    }
}
