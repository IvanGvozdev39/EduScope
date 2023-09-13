package com.eduscope.eduscope;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.eduscope.eduscope.sqlite.SQLiteHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DetailedContentActivity extends AppCompatActivity {

    private boolean wasImagePressed = false;

    private SharedPreferences pref, def_pref;
    private long appSessionStartDCA;

    private boolean isSwitch;
    private String text_size_str;
    private int category;
    private int position;
    private String subject;

    private SQLiteHelper sqLiteHelper = new SQLiteHelper(DetailedContentActivity.this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailed_content_summary); //detailed_content с философами придется присваивать позже переменной text_content_philosophers

        pref = getSharedPreferences("Statistics", Context.MODE_PRIVATE);
        def_pref = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences def_pref = PreferenceManager.getDefaultSharedPreferences(this);
        text_size_str = def_pref.getString("text_size_preference", "Средний");
        isSwitch = def_pref.getBoolean("theme_switch_preference", false);

        receiveIntent();

    }


    public void StatisticsSaveLong(String key, long value) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putLong(key, value);
        edit.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        appSessionStartDCA = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timeInAppKey = "TimeInApp";
        StatisticsSaveLong(timeInAppKey, pref.getLong(timeInAppKey, 0) + (System.currentTimeMillis() - appSessionStartDCA));
    }


    private int FindPhilosophersPosition(String word) {
        //Чтобы от составных имен тоже оставались только фамилии:
        String[] parts;
        String[] philosophersList = sqLiteHelper.selectScientistsTitles(subject);
        if (word.contains(" ")) {
            parts = word.split(" ");//не работает
            for (int i = 0; i < philosophersList.length; i++)
                if (philosophersList[i].contains(parts[parts.length - 1]))
                    return i;
        } else {
            for (int i = 0; i < philosophersList.length; i++)
                if (philosophersList[i].contains(word))
                    return i;
        }
        return -1;
    }


    private Spannable SearchWords(String text, @NonNull String[] searchWord) {
        Spannable newText = new SpannableString(text);

        if (searchWord.length != 0) {
            for (int i = 0; i < searchWord.length; i++) {

                int beginIndex = -1;
                while (text.indexOf(searchWord[i], beginIndex + 1) != -1) {
                    beginIndex = text.indexOf(searchWord[i], beginIndex + 1);
                    int endIndex = beginIndex + searchWord[i].length();
                    while (text.charAt(endIndex) != ' ' && text.charAt(endIndex) != ')' && text.charAt(endIndex) != '.' && text.charAt(endIndex) != ',' &&
                            text.charAt(endIndex) != ';' && text.charAt(endIndex) != ':' && text.charAt(endIndex) != '!' && text.charAt(endIndex) != '?' &&
                            text.charAt(endIndex) != '-') {
                        endIndex++;
                    }

                    int finalI = i;

                    ClickableSpan clickSpan = new ClickableSpan() {
                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            super.updateDrawState(ds);
                            if (def_pref.getBoolean("theme_switch_preference", false))
                                ds.setColor(getResources().getColor(R.color.BSUIR_Blue_light));
                            else
                                ds.setColor(getResources().getColor(R.color.BSUIR_Blue));
                            ds.setUnderlineText(true);
                        }

                        @Override
                        public void onClick(@NonNull View view) {
                            Intent spanIntent = new Intent(DetailedContentActivity.this, DetailedContentActivity.class);
                            spanIntent.putExtra("category", 3);
                            spanIntent.putExtra("position", FindPhilosophersPosition(searchWord[finalI]));
                            startActivity(spanIntent);
                        }
                    };
                    newText.setSpan(
                            clickSpan,
                            beginIndex,
                            endIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    //чтобы слово "марксизм" не пересылало на Карла Маркса:
                    if (text.indexOf("Марксизм", beginIndex) == beginIndex || text.indexOf("марксизм") == beginIndex ||
                            text.indexOf("Платонизм", beginIndex) == beginIndex || text.indexOf("платонизм", beginIndex) == beginIndex) {
                        newText.removeSpan(clickSpan);
                    }
                }
            }
        }
        return newText;
    }

    private Spannable bigBTitle(Spannable text) {
        Spannable newText = new SpannableString(text);
        String searchSpaces = "\n\n";
        int endIndex = String.valueOf(newText).indexOf(searchSpaces);
        if (String.valueOf(newText).contains(searchSpaces)) {
            newText.setSpan(new RelativeSizeSpan(1.2f), 0, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            newText.setSpan(new StyleSpan(Typeface.BOLD), 0, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return newText;
    }


    private void receiveIntent() {
        Intent i = getIntent(); //получаем категорию и индекс, далее проверяем, пришел ли Интент
        if (i != null) {
            category = i.getIntExtra("category", 0); //если ничего не содержит, то передаем 0, чтобы хоть что-то было
            position = i.getIntExtra("position", 0);
            subject = i.getStringExtra("subject");
        }

        switch (category) {
            case 2:
                TextView text_content_summary = findViewById(R.id.detailed_content_textView_Summary);
                FrameLayout frame_layout_summary = findViewById(R.id.frame_layout_summary);

                String[] searchWords11 = sqLiteHelper.selectSummarySpanSearchWords(subject);
                Spannable newText = SearchWords(sqLiteHelper.selectSummaryContent(subject)[position], searchWords11);
                newText = bigBTitle(newText);

                text_content_summary.setText(newText, TextView.BufferType.SPANNABLE);
                text_content_summary.setMovementMethod(LinkMovementMethod.getInstance());

                //text_content_summary.setText(array_summary[position]);
                switch (text_size_str) {
                    case "Крупный":
                        text_content_summary.setTextSize(22);
                        break;
                    case "Обычный":
                        text_content_summary.setTextSize(18);
                        break;
                    case "Мелкий":
                        text_content_summary.setTextSize(15);
                        break;
                }

                if (isSwitch) {
                    text_content_summary.setTextColor(getResources().getColor(R.color.light_text));
                    frame_layout_summary.setBackgroundColor(getResources().getColor(R.color.dark_theme));
                }
                break;
            case 3:
                setContentView(R.layout.detailed_content);
                LinearLayout imageLinearLayout = findViewById(R.id.image_linear_layout);
                imageLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!wasImagePressed) {
                            Animation anim = new ShowAnim(imageLinearLayout, 600, 601);
                            anim.setDuration(200);
                            imageLinearLayout.startAnimation(anim);
                            //imageLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1150));
                        } else {
                            Animation anim = new ShowAnim(imageLinearLayout, 1201, 601); //3 параметр - разница в высоте, а не высота
                            anim.setDuration(200);
                            imageLinearLayout.startAnimation(anim);
                            //imageLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 650));
                        }
                        wasImagePressed = !wasImagePressed;
                    }
                });
                TextView text_content_philosophers = findViewById(R.id.detailed_content_philosophers);
                FrameLayout frame_layout_philosophers = findViewById(R.id.frame_layout_philosophers);
                ImageView image_content_philosophers = findViewById(R.id.detailed_content_imageView);


                Map<byte[], String> scientistsMap = new HashMap<>();
                scientistsMap = sqLiteHelper.selectScientistsContent(subject, position);
                Set<Map.Entry<byte[], String>> entrySetScientists = scientistsMap.entrySet();
                Map.Entry<byte[], String>[] entryArrayScientists = entrySetScientists.toArray(new Map.Entry[entrySetScientists.size()]);
                text_content_philosophers.setText(entryArrayScientists[0].getValue());

                Bitmap bitmap = BitmapFactory.decodeByteArray(entryArrayScientists[0].getKey(), 0, entryArrayScientists[0].getKey().length);
                image_content_philosophers.setImageBitmap(bitmap);

                switch (text_size_str) {
                    case "Крупный":
                        text_content_philosophers.setTextSize(22);
                        break;
                    case "Обычный":
                        text_content_philosophers.setTextSize(18);
                        break;
                    case "Мелкий":
                        text_content_philosophers.setTextSize(15);
                        break;
                }

                if (isSwitch) {
                    text_content_philosophers.setTextColor(getResources().getColor(R.color.light_text));
                    frame_layout_philosophers.setBackgroundColor(getResources().getColor(R.color.dark_theme));
                }

                //text_content_philosophers.setText();
                break;
        }
    }
}
