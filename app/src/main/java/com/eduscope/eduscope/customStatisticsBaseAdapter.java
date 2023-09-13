package com.eduscope.eduscope;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Chronometer;
import android.widget.TextView;

public class customStatisticsBaseAdapter extends BaseAdapter {
    private final String[] leftTV;
    private final String[] middleTV;
    private final String[] rightTV;
    private final long[] rightChronometer;
    private final boolean startStopChronometer;
    private final boolean darkTheme;
    private final int textSize;
    LayoutInflater inflater;

    public customStatisticsBaseAdapter(Context context, String[] leftTV, String[] middleTV, String[] rightTV, long[] rightChronometerBase, boolean startStopChronometer,
                                       boolean darkTheme, int textSize) {
        this.leftTV = leftTV;
        this.middleTV = middleTV;
        this.rightTV = rightTV;
        this.rightChronometer = rightChronometerBase;
        this.startStopChronometer = startStopChronometer;
        this.darkTheme = darkTheme;
        this.textSize = textSize;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return rightTV.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.arraylist_statistics_item, null);
        TextView leftTextView = convertView.findViewById(R.id.leftTVStatistics);
        TextView middleTextView = convertView.findViewById(R.id.middleTVStatistics);
        TextView rightTextView = convertView.findViewById(R.id.rightTVStatistics);
        Chronometer rightChronometerView = convertView.findViewById(R.id.rightChronometerStatistics);

        if (leftTV[position] != null) {
            leftTextView.setText(leftTV[position]);
            leftTextView.setVisibility(View.VISIBLE);
        }
        else
            leftTextView.setVisibility(View.GONE);

        if (middleTV[position] != null) {
            middleTextView.setText(middleTV[position]);
            middleTextView.setVisibility(View.VISIBLE);
        }
        else
            middleTextView.setVisibility(View.GONE);

        if (rightTV[position] != null) {
            rightTextView.setText(rightTV[position]);
            rightTextView.setVisibility(View.VISIBLE);
        }
        else
            rightTextView.setVisibility(View.GONE);

        if (rightChronometer[position] != -1) {
            rightChronometerView.setBase(rightChronometer[position]);
            if (startStopChronometer)
                rightChronometerView.start();
            else
                rightChronometerView.stop();
            rightChronometerView.setVisibility(View.VISIBLE);
        }
        else
            rightChronometerView.setVisibility(View.GONE);

        if (!darkTheme) {
            leftTextView.setTextColor(Color.rgb(37, 37, 37));
            middleTextView.setTextColor(Color.rgb(37, 37, 37));
            rightTextView.setTextColor(Color.rgb(37, 37, 37));
            rightChronometerView.setTextColor(Color.rgb(37, 37, 37));
        }
        else {
            leftTextView.setTextColor(Color.rgb(225, 225, 225));
            middleTextView.setTextColor(Color.rgb(225, 225, 225));
            rightTextView.setTextColor(Color.rgb(225, 225, 225));
            rightChronometerView.setTextColor(Color.rgb(225, 225, 225));
        }

        leftTextView.setTextSize(textSize);
        middleTextView.setTextSize(textSize);
        rightTextView.setTextSize(textSize);
        rightChronometerView.setTextSize(textSize);

        return convertView;
    }
}