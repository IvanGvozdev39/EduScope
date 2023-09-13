package com.eduscope.eduscope;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class customViewMistakesBaseAdapter extends BaseAdapter {
    private final ArrayList<Integer> questionNumber;
    private final ArrayList<String> question;
    private final ArrayList<String> selected;
    private final ArrayList<String> correct;
    private final boolean darkTheme;
    private final int textSize;
    LayoutInflater inflater;

    public customViewMistakesBaseAdapter(Context context, ArrayList<Integer> questionNumber, ArrayList<String> question, ArrayList<String> selected, ArrayList<String> correct, boolean darkTheme, int textSize) {
        this.questionNumber = questionNumber;
        this.question = question;
        this.selected = selected;
        this.correct = correct;
        this.darkTheme = darkTheme;
        this.textSize = textSize;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return question.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint({"ViewHolder", "InflateParams", "SetTextI18n"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.arraylist_view_mistakes_item, null);
        TextView questionTV = convertView.findViewById(R.id.question_tv);
        TextView selectedTV = convertView.findViewById(R.id.selected_tv);
        TextView correctTV = convertView.findViewById(R.id.correct_tv);
        TextView selectedTVConst = convertView.findViewById(R.id.selected_tv_const);
        TextView correctTVConst = convertView.findViewById(R.id.correct_tv_const);

        questionTV.setText(questionNumber.get(position) + ". " + question.get(position));
        selectedTV.setText(selected.get(position));
        correctTV.setText(correct.get(position));

        if (selected.get(position).equals(correct.get(position)))
            selectedTV.setTextColor(Color.rgb(0, 223, 29));
        else
            selectedTV.setTextColor(Color.rgb(221, 44, 0));

        if (!darkTheme) {
            questionTV.setTextColor(Color.rgb(37, 37, 37));
            correctTV.setTextColor(Color.rgb(37, 37, 37));
            selectedTVConst.setTextColor(Color.rgb(37, 37, 37));
            correctTVConst.setTextColor(Color.rgb(37, 37, 37));
        }
        else {
            questionTV.setTextColor(Color.rgb(225, 225, 225));
            correctTV.setTextColor(Color.rgb(225, 225, 225));
            selectedTVConst.setTextColor(Color.rgb(225, 225, 225));
            correctTVConst.setTextColor(Color.rgb(225, 225, 225));
        }

        questionTV.setTextSize(textSize);
        selectedTV.setTextSize(textSize);
        correctTV.setTextSize(textSize);
        selectedTVConst.setTextSize(textSize);
        correctTVConst.setTextSize(textSize);

        return convertView;
    }
}