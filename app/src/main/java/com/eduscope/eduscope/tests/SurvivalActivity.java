package com.eduscope.eduscope.tests;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.eduscope.eduscope.R;
import com.eduscope.eduscope.sqlite.SQLiteHelper;

import java.util.List;
import java.util.Random;

public class SurvivalActivity extends AppCompatActivity {

    private SharedPreferences def_pref;
    private int questionsAnsweredCounter = 0;
    private int questionNumCounter = 1;
    private int crossCount = 0; //отслеживать количество отработавших попыток
    private final int[] ticketOrder = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
    private final int[] optionOrder = {0, 1, 2, 3};
    private int iQuestionOrder = 0;
    private int iTicketOrder = 0;
    private int[] questionOrder;

    private SharedPreferences pref;
    private long appSessionStart;
    private long sessionTimeStart;
    private long backPressedTime;
    private boolean wasFinishedPressed = false;
    private int testsMode;
    private int currentQuestionPosition = 0;
    private String selectedOptionByUser = "";
    private boolean isChronometerRunning = false;
    private Chronometer chronometer;
    private long pauseOffset;
    private TextView question_number, question;
    private AppCompatButton option1, option2, option3, option4, check_button;
    private ImageView cross1, cross2, cross3;
    private List<QuestionList> questionList;
    private SQLiteHelper sqLiteHelper = new SQLiteHelper(SurvivalActivity.this);
    private String subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survival);

        pref = getSharedPreferences("Statistics", Context.MODE_PRIVATE);

        final ImageView back_button = findViewById(R.id.back_button);
        chronometer = findViewById(R.id.chronometer);

        question_number = findViewById(R.id.question_number);
        question = findViewById(R.id.question);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        check_button = findViewById(R.id.check_button);
        cross1 = findViewById(R.id.cross1);
        cross2 = findViewById(R.id.cross2);
        cross3 = findViewById(R.id.cross3);

        def_pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (def_pref.getBoolean("theme_switch_preference", false)) {
            question_number.setTextColor(getResources().getColor(R.color.light_text));
            question_number.setBackgroundResource(R.drawable.round_back_dark_10);
            question.setTextColor(getResources().getColor(R.color.light_text));
            question.setBackgroundResource(R.drawable.round_back_dark_10);
            option1.setBackgroundResource(R.drawable.round_back_dark_10);
            option1.setTextColor(getResources().getColor(R.color.light_text));
            option2.setBackgroundResource(R.drawable.round_back_dark_10);
            option2.setTextColor(getResources().getColor(R.color.light_text));
            option3.setBackgroundResource(R.drawable.round_back_dark_10);
            option3.setTextColor(getResources().getColor(R.color.light_text));
            option4.setBackgroundResource(R.drawable.round_back_dark_10);
            option4.setTextColor(getResources().getColor(R.color.light_text));
            check_button.setBackgroundResource(R.drawable.round_button_dark_20);
            check_button.setTextColor(getResources().getColor(R.color.light_text));
        }

        back_button.setOnClickListener(view -> {
            if (backPressedTime + 2000 > System.currentTimeMillis())
                finish();
            else
                Toast.makeText(getBaseContext(), getText(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
            backPressedTime = System.currentTimeMillis();
        });

        testsMode = getIntent().getIntExtra("testsMode", 6);
        subject = getIntent().getStringExtra("subject");

        Shuffle(ticketOrder);
        questionList = sqLiteHelper.selectAllTopicsTrainingQuestions(subject, (getString(R.string.ticket) + " " + ticketOrder[iTicketOrder]));
        //questionList = QuestionBankAllTopics.getQuestionsSurvival(ticketOrder[iTicketOrder]);

        questionOrder = new int[questionList.size()];
        for (int i = 0; i < questionList.size(); i++) {
            questionOrder[i] = i;
        }
        Shuffle(questionOrder);
        Shuffle(optionOrder);
        currentQuestionPosition = questionOrder[iQuestionOrder];

        startChronometer();
        sessionTimeStart = System.currentTimeMillis();

        question_number.setText(String.valueOf(questionNumCounter));
        question.setText(questionList.get(currentQuestionPosition).getQuestion());
        option1.setText(questionList.get(currentQuestionPosition).getOption(optionOrder[0]));
        option2.setText(questionList.get(currentQuestionPosition).getOption(optionOrder[1]));
        option3.setText(questionList.get(currentQuestionPosition).getOption(optionOrder[2]));
        option4.setText(questionList.get(currentQuestionPosition).getOption(optionOrder[3]));


        option1.setOnClickListener(view -> {
            if (selectedOptionByUser.isEmpty()) {
                if (iTicketOrder == 19 && iQuestionOrder == 9)
                    check_button.setText(R.string.test_complete);
                else
                    check_button.setText(R.string.check_answer);
                selectedOptionByUser = option1.getText().toString();
                if (!def_pref.getBoolean("theme_switch_preference", false))
                    option1.setBackgroundResource(R.drawable.round_back_red10);
                else
                    option1.setBackgroundResource(R.drawable.round_back_red10_dark_theme);
                option1.setTextColor(Color.WHITE);
                questionList.get(questionOrder[iQuestionOrder]).setUserSelectedOption(selectedOptionByUser);
                revealAnswer();
                questionsAnsweredCounter++;
            }
        });
        option2.setOnClickListener(view -> {
            if (selectedOptionByUser.isEmpty()) {
                if (iTicketOrder == 19 && iQuestionOrder == 9) //изменится, если игрок дошел до самого конца
                    check_button.setText(R.string.test_complete);
                else
                    check_button.setText(R.string.check_answer);
                selectedOptionByUser = option2.getText().toString();
                if (!def_pref.getBoolean("theme_switch_preference", false))
                    option2.setBackgroundResource(R.drawable.round_back_red10);
                else
                    option2.setBackgroundResource(R.drawable.round_back_red10_dark_theme);
                option2.setTextColor(Color.WHITE);
                questionList.get(questionOrder[iQuestionOrder]).setUserSelectedOption(selectedOptionByUser);
                revealAnswer();
                questionsAnsweredCounter++;
            }
        });
        option3.setOnClickListener(view -> {
            if (selectedOptionByUser.isEmpty()) {
                if (iTicketOrder == 19 && iQuestionOrder == 9)
                    check_button.setText(R.string.test_complete);
                else
                    check_button.setText(R.string.check_answer);
                selectedOptionByUser = option3.getText().toString();
                if (!def_pref.getBoolean("theme_switch_preference", false))
                    option3.setBackgroundResource(R.drawable.round_back_red10);
                else
                    option3.setBackgroundResource(R.drawable.round_back_red10_dark_theme);
                option3.setTextColor(Color.WHITE);
                questionList.get(questionOrder[iQuestionOrder]).setUserSelectedOption(selectedOptionByUser);
                revealAnswer();
                questionsAnsweredCounter++;
            }
        });
        option4.setOnClickListener(view -> {
            if (selectedOptionByUser.isEmpty()) {
                if (iTicketOrder == 19 && iQuestionOrder == 9)
                    check_button.setText(R.string.test_complete);
                else
                    check_button.setText(R.string.check_answer);
                selectedOptionByUser = option4.getText().toString();
                if (!def_pref.getBoolean("theme_switch_preference", false))
                    option4.setBackgroundResource(R.drawable.round_back_red10);
                else
                    option4.setBackgroundResource(R.drawable.round_back_red10_dark_theme);
                option4.setTextColor(Color.WHITE);
                questionList.get(questionOrder[iQuestionOrder]).setUserSelectedOption(selectedOptionByUser);
                revealAnswer();
                questionsAnsweredCounter++;
            }
        });
        check_button.setOnClickListener(view -> {
            if (!wasFinishedPressed) {
                if (crossCount == 3) {
                    showResults();
                } else {
                    if (selectedOptionByUser.isEmpty())
                        check_button.setText(R.string.check_button_no_selected_option);
                    else
                        questionChange();
                }
            }
            if (crossCount == 3)
                wasFinishedPressed = true;
        });


        //Крестики будут вырисовываться по анимации translationZ

    }


    ///////Переделать, нам по сути важно знать только количество пройденных ответов, можно и без
    //этой функции обойтись


    private void showResults() {
        String survivalCounterKey = "SurvivalCounter";
        StatisticsSave(survivalCounterKey, pref.getInt(survivalCounterKey, 0) + 1);
        String rightAnswersKey = "RightAnswers";
        StatisticsSave(rightAnswersKey, pref.getInt(rightAnswersKey, 0) + questionsAnsweredCounter - crossCount);
        String questionsAnsweredKey = "QuestionsAnswered";
        StatisticsSave(questionsAnsweredKey, pref.getInt(questionsAnsweredKey, 0) + questionsAnsweredCounter);
        String testsCompletedKey = "TestsCompleted";
        StatisticsSave(testsCompletedKey, /*++testsCompletedCount*/ pref.getInt(testsCompletedKey, 0) + 1);
        Intent intent = new Intent(SurvivalActivity.this, TopicTrainingQuizResultsActivity.class);
        intent.putExtra("chronometer", String.valueOf(chronometer.getText()));
        intent.putExtra("correct", questionsAnsweredCounter - crossCount);
        intent.putExtra("all_questions", questionsAnsweredCounter);
        intent.putExtra("testsMode", testsMode);
        startActivity(intent);
        resetChronometer();
        finish();
    }


    private void revealAnswer() {
        final String getAnswer = questionList.get(questionOrder[iQuestionOrder]).getAnswer();
        if (option1.getText().toString().equals(getAnswer)) {
            if (!def_pref.getBoolean("theme_switch_preference", false))
                option1.setBackgroundResource(R.drawable.round_back_green_10);
            else
                option1.setBackgroundResource(R.drawable.round_back_green10_dark_theme);
            option1.setTextColor(Color.WHITE);
        } else if (option2.getText().toString().equals(getAnswer)) {
            if (!def_pref.getBoolean("theme_switch_preference", false))
                option2.setBackgroundResource(R.drawable.round_back_green_10);
            else
                option2.setBackgroundResource(R.drawable.round_back_green10_dark_theme);
            option2.setTextColor(Color.WHITE);
        } else if (option3.getText().toString().equals(getAnswer)) {
            if (!def_pref.getBoolean("theme_switch_preference", false))
                option3.setBackgroundResource(R.drawable.round_back_green_10);
            else
                option3.setBackgroundResource(R.drawable.round_back_green10_dark_theme);
            option3.setTextColor(Color.WHITE);
        } else if (option4.getText().toString().equals(getAnswer)) {
            if (!def_pref.getBoolean("theme_switch_preference", false))
                option4.setBackgroundResource(R.drawable.round_back_green_10);
            else
                option4.setBackgroundResource(R.drawable.round_back_green10_dark_theme);
            option4.setTextColor(Color.WHITE);
        }
        if (!(questionList.get(questionOrder[iQuestionOrder]).getUserSelectedOption().equals(questionList.get(questionOrder[iQuestionOrder]).getAnswer()))) {
            crossFill();
        }
        if (crossCount == 3) {
            check_button.setText("Завершить"); //Убрать хардкод
            pauseChronometer();
            String timeInTestsKey = "TimeInTests";
            StatisticsSaveLong(timeInTestsKey, /*timeInTestsCount*/ pref.getLong(timeInTestsKey, 0) + (System.currentTimeMillis() - sessionTimeStart));
        }
    }


    private void crossFill() {
        switch (crossCount++) {
            case 0:
                cross1.setImageResource(R.drawable.cross);
                break;
            case 1:
                cross2.setImageResource(R.drawable.cross);
                break;
            case 2:
                cross3.setImageResource(R.drawable.cross);
                break;
        }
    }


    private void questionChange() {
        //currentQuestionPosition++;
        //progressBar.animate()
        if (iQuestionOrder + 1 != questionList.size())
            currentQuestionPosition = questionOrder[++iQuestionOrder];
        else
            iQuestionOrder++;


        if (iQuestionOrder < questionList.size()) {
            selectedOptionByUser = "";
            if (!def_pref.getBoolean("theme_switch_preference", false)) {
                option1.setBackgroundResource(R.drawable.round_back_white_10);
                option2.setBackgroundResource(R.drawable.round_back_white_10);
                option3.setBackgroundResource(R.drawable.round_back_white_10);
                option4.setBackgroundResource(R.drawable.round_back_white_10);
                option1.setTextColor(getResources().getColor(R.color.dark_text));
                option2.setTextColor(getResources().getColor(R.color.dark_text));
                option3.setTextColor(getResources().getColor(R.color.dark_text));
                option4.setTextColor(getResources().getColor(R.color.dark_text));
            } else {
                option1.setBackgroundResource(R.drawable.round_back_dark_10);
                option2.setBackgroundResource(R.drawable.round_back_dark_10);
                option3.setBackgroundResource(R.drawable.round_back_dark_10);
                option4.setBackgroundResource(R.drawable.round_back_dark_10);
                option1.setTextColor(getResources().getColor(R.color.light_text));
                option2.setTextColor(getResources().getColor(R.color.light_text));
                option3.setTextColor(getResources().getColor(R.color.light_text));
                option4.setTextColor(getResources().getColor(R.color.light_text));
            }

            Shuffle(optionOrder);
            question_number.setText(String.valueOf(++questionNumCounter));
            question.setText(questionList.get(questionOrder[iQuestionOrder]).getQuestion());
            option1.setText(questionList.get(questionOrder[iQuestionOrder]).getOption(optionOrder[0]));
            option2.setText(questionList.get(questionOrder[iQuestionOrder]).getOption(optionOrder[1]));
            option3.setText(questionList.get(questionOrder[iQuestionOrder]).getOption(optionOrder[2]));
            option4.setText(questionList.get(questionOrder[iQuestionOrder]).getOption(optionOrder[3]));
        } else {
            if (iTicketOrder == 19) {
                wasFinishedPressed = true;
                showResults();
            } else {
                //Чтобы не было двойных нажатий, портящих статистику, добавлен boolean:
                questionList.clear();
                questionList = sqLiteHelper.selectAllTopicsTrainingQuestions(subject, (getString(R.string.ticket) + " " + ticketOrder[++iTicketOrder]));
                //questionList = QuestionBankAllTopics.getQuestionsSurvival(ticketOrder[++iTicketOrder]);
                selectedOptionByUser = "";
                iQuestionOrder = 0;
                //currentQuestionPosition = questionOrder[iQuestionOrder];
                if (!def_pref.getBoolean("theme_switch_preference", false)) {
                    option1.setBackgroundResource(R.drawable.round_back_white_10);
                    option2.setBackgroundResource(R.drawable.round_back_white_10);
                    option3.setBackgroundResource(R.drawable.round_back_white_10);
                    option4.setBackgroundResource(R.drawable.round_back_white_10);
                    option1.setTextColor(getResources().getColor(R.color.dark_text));
                    option2.setTextColor(getResources().getColor(R.color.dark_text));
                    option3.setTextColor(getResources().getColor(R.color.dark_text));
                    option4.setTextColor(getResources().getColor(R.color.dark_text));
                } else {
                    option1.setBackgroundResource(R.drawable.round_back_dark_10);
                    option2.setBackgroundResource(R.drawable.round_back_dark_10);
                    option3.setBackgroundResource(R.drawable.round_back_dark_10);
                    option4.setBackgroundResource(R.drawable.round_back_dark_10);
                    option1.setTextColor(getResources().getColor(R.color.light_text));
                    option2.setTextColor(getResources().getColor(R.color.light_text));
                    option3.setTextColor(getResources().getColor(R.color.light_text));
                    option4.setTextColor(getResources().getColor(R.color.light_text));
                }
                Shuffle(questionOrder);
                Shuffle(optionOrder);
                question_number.setText(String.valueOf(++questionNumCounter));
                question.setText(questionList.get(questionOrder[iQuestionOrder]).getQuestion());
                option1.setText(questionList.get(questionOrder[iQuestionOrder]).getOption(optionOrder[0]));
                option2.setText(questionList.get(questionOrder[iQuestionOrder]).getOption(optionOrder[1]));
                option3.setText(questionList.get(questionOrder[iQuestionOrder]).getOption(optionOrder[2]));
                option4.setText(questionList.get(questionOrder[iQuestionOrder]).getOption(optionOrder[3]));
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            finish();
            super.onBackPressed();
        } else
            Toast.makeText(getBaseContext(), getText(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
        backPressedTime = System.currentTimeMillis();
    }


    private void startChronometer() { //View view parameter deleted, might cause troubles
        if (!isChronometerRunning) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset); //chronometer считает время даже после pause, поэтому используем pauseOffset, который считает, сколько времени в миллисекундах насчиталось после pause и до start
            chronometer.start();
            isChronometerRunning = true;
        }
    }

    private void pauseChronometer() {
        if (isChronometerRunning) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            isChronometerRunning = false;
        }
    }

    private void resetChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime()); //resets back to 0
        pauseOffset = 0;
    }

    public void StatisticsSave(String key, int value) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putInt(key, value);
        edit.apply();
    }

    public void StatisticsSaveLong(String key, long value) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putLong(key, value);
        edit.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timeInAppKey = "TimeInApp";
        StatisticsSaveLong(timeInAppKey, pref.getLong(timeInAppKey, 0) + (System.currentTimeMillis() - appSessionStart));

    }

    @Override
    protected void onResume() {
        super.onResume();
        appSessionStart = System.currentTimeMillis();
    }


    private static void Shuffle(int[] array) {
        Random random = new Random();
        for (int i = 0; i < array.length; ++i) {
            int j = random.nextInt(array.length);
            int tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }


    @Override
    public void onDestroy() {
        pauseChronometer();
        resetChronometer();
        finish();
        super.onDestroy();
    }
}