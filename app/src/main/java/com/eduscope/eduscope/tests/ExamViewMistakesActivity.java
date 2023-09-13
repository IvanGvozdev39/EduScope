package com.eduscope.eduscope.tests;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.eduscope.eduscope.R;
import com.eduscope.eduscope.customViewMistakesBaseAdapter;

import java.util.ArrayList;


public class ExamViewMistakesActivity extends AppCompatActivity {

    private long appSessionStartEVMA;
    private SharedPreferences prefEVMA, def_pref;
    private ListView viewMistakesLV;
    private customViewMistakesBaseAdapter adapter;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_mistakes);
        prefEVMA = getSharedPreferences("Statistics", MODE_PRIVATE);

        ImageView backButton = findViewById(R.id.back_button);
        viewMistakesLV = findViewById(R.id.view_mistakes_lv);

        //Для exam (просмотр ошибок)
        ArrayList<Integer> viewMistakesQuestionNumberList = getIntent().getIntegerArrayListExtra("viewMistakesQuestionNumberList");
        ArrayList<String> viewMistakesQuestionList = getIntent().getStringArrayListExtra("viewMistakesQuestionList");
        ArrayList<String> viewMistakesUserAnswerList = getIntent().getStringArrayListExtra("viewMistakesUserAnswerList");
        ArrayList<String> viewMistakesCorrectAnswerList = getIntent().getStringArrayListExtra("viewMistakesCorrectAnswerList");

        def_pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkTheme = false;
        int textSize = 18;
        if(def_pref.getBoolean("theme_switch_preference", false)) {
            darkTheme = true;
            LinearLayout viewMistakesLL = findViewById(R.id.view_mistakes_linear_layout);
            viewMistakesLL.setBackgroundColor(getResources().getColor(R.color.dark_theme));
        }

        if (def_pref.getString("text_size_preference", "Обычный").equals("Крупный"))
            textSize = 21;
        else if (def_pref.getString("text_size_preference", "Обычный").equals("Мелкий"))
            textSize = 15;

        adapter = new customViewMistakesBaseAdapter(this, viewMistakesQuestionNumberList, viewMistakesQuestionList, viewMistakesUserAnswerList, viewMistakesCorrectAnswerList, darkTheme, textSize);
        viewMistakesLV.setAdapter(adapter);


        backButton.setOnClickListener(view -> finish());


    }




    @Override
    protected void onResume() {
        super.onResume();
        appSessionStartEVMA = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timeInAppKeyQRA = "TimeInApp";
        StatisticsSaveLongEVMA(timeInAppKeyQRA, prefEVMA.getLong(timeInAppKeyQRA, 0) + (System.currentTimeMillis() - appSessionStartEVMA));
    }

    private void StatisticsSaveLongEVMA(String key, long value) {
        SharedPreferences.Editor edit = prefEVMA.edit();
        edit.putLong(key, value);
        edit.apply();
    }
}