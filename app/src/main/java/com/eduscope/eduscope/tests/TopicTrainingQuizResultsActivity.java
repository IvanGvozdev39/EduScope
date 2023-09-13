package com.eduscope.eduscope.tests;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.eduscope.eduscope.R;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class TopicTrainingQuizResultsActivity extends AppCompatActivity {

    private boolean wasFinished; //чтобы не было дабл кликов
    private final String setsCompletedKey = "SetsCompleted";
    private final String setsCompletedKeyGBI = "SetsCompletedGuessByImage";
    private String spinnerChoiceAllTopics;
    private long appSessionStartTTQRA;
    private SharedPreferences prefQRA;

    private ArrayList<Integer> viewMistakesQuestionNumberList; //Для exam (просмотр ошибок)
    private ArrayList<String> viewMistakesQuestionList;
    private ArrayList<String> viewMistakesUserAnswerList;
    private ArrayList<String> viewMistakesCorrectAnswerList;

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic_training_activity_quiz_results);

        prefQRA = getSharedPreferences("Statistics", MODE_PRIVATE);
        final SharedPreferences def_pref = PreferenceManager.getDefaultSharedPreferences(this);

        if (def_pref.getString("animation_quiz_results_preference", "С анимацией").equals("С анимацией")) {
            final LinearLayout linearLayoutQuizResults = findViewById(R.id.linear_quiz_results);
            TranslateAnimation anim = new TranslateAnimation(0, 0, -500, 0);
            anim.setDuration(450);
            linearLayoutQuizResults.startAnimation(anim);
        }

        TextView markTV = findViewById(R.id.mark);
        TextView your_mark_isTV = findViewById(R.id.your_mark_is);
        TextView correctTV = findViewById(R.id.correct_answers);
        TextView chronometerTV = findViewById(R.id.chronometerTV);
        TextView incorrectTV = findViewById(R.id.incorrectTV);
        AppCompatButton view_mistakes_button = findViewById(R.id.view_mistakes_button);
        AppCompatButton retry_button = findViewById(R.id.retry_button);
        AppCompatButton exit_button = findViewById(R.id.exit_button);
        TextView youNeed8 = findViewById(R.id.you_need_8);
        youNeed8.setVisibility(View.GONE);


        if (def_pref.getBoolean("theme_switch_preference", false)) {
            view_mistakes_button.setTextColor(getResources().getColor(R.color.light_text));
            view_mistakes_button.setBackgroundResource(R.drawable.button_round_back_dark_when_pressed_grey_10);
            retry_button.setTextColor(getResources().getColor(R.color.light_text));
            retry_button.setBackgroundResource(R.drawable.button_round_back_dark_when_pressed_grey_10);
            exit_button.setTextColor(getResources().getColor(R.color.light_text));
            exit_button.setBackgroundResource(R.drawable.button_round_back_dark_when_pressed_grey_10);
        }


        final String chronometer;
        final float correct;
        final float all_questions;
        final String spinnerChoice;
        final int testsMode;

        chronometer = getIntent().getExtras().getString("chronometer");
        correct = getIntent().getIntExtra("correct", 0);
        all_questions = getIntent().getIntExtra("all_questions", 0);
        final int all_questions_int = (int) all_questions;
        spinnerChoice = getIntent().getExtras().getString("spinnerChoice");
        testsMode = getIntent().getExtras().getInt("testsMode");
        spinnerChoiceAllTopics = getIntent().getExtras().getString("spinnerChoiceAllTopics");


        CircularProgressBar circularProgressBar = findViewById(R.id.circularProgressBar);
        if (testsMode != 6)
            circularProgressBar.setProgressWithAnimation((int) (correct / all_questions * 100), (long) (correct / all_questions * 2000));
        else
            circularProgressBar.setVisibility(View.GONE);


        //if (correct != all_questions) {
        viewMistakesQuestionNumberList = getIntent().getIntegerArrayListExtra("viewMistakesQuestionNumberList");
        viewMistakesQuestionList = getIntent().getStringArrayListExtra("viewMistakesQuestionList");
        viewMistakesUserAnswerList = getIntent().getStringArrayListExtra("viewMistakesUserAnswerList");
        viewMistakesCorrectAnswerList = getIntent().getStringArrayListExtra("viewMistakesCorrectAnswerList");
        //}

        if (testsMode != 3) {
            view_mistakes_button.setVisibility(View.INVISIBLE);
            if (testsMode == 4 || testsMode == 5) { //если прохождение уровня будет усложнено, то добавить сюда условия
                view_mistakes_button.setVisibility(View.VISIBLE);
                view_mistakes_button.setText(getText(R.string.tests_continue));
                if (correct < 8) {
                    if (!def_pref.getBoolean("theme_switch_preference", false))
                        view_mistakes_button.setBackgroundResource(R.drawable.round_back_grey10);
                    else
                        view_mistakes_button.setBackgroundResource(R.drawable.round_back_grey10_dark_theme);
                    view_mistakes_button.setClickable(false);
                    youNeed8.setVisibility(View.VISIBLE);
                }
            }
        }


        float incorrect = all_questions - correct;

        float mark = 10 * correct / all_questions; //если в переменную float занести результат вычислений переменных int, то оно округлится до int
        @SuppressLint("DefaultLocale") String mark_string = String.format("%.1f", mark);

        chronometerTV.setText(chronometer);

        correctTV.setText(String.format("%.0f", correct));
        incorrectTV.setText(String.format("%.0f", incorrect));
        if (testsMode == 6) {
            your_mark_isTV.setTextSize(21);
            Timer timer = new Timer();
            final int[] counter = {0};
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (counter[0] == all_questions_int) {
                                markTV.setText(String.valueOf(all_questions_int));
                                timer.cancel();
                                return;
                            }
                            markTV.setText(String.valueOf((counter[0]++)));
                        }
                    });
                }
            };
            if (all_questions < 15)
                timer.schedule(timerTask, 0, 500 / (long) all_questions);
            else if (all_questions < 100)
                timer.schedule(timerTask, 0, 1000 / (long) all_questions);
            else
                timer.schedule(timerTask, 0, 2500 / (long) all_questions);

            String highestSurvivalScoreKey = "HighestSurvivalScore";
            if (prefQRA.getInt(highestSurvivalScoreKey, 0) < all_questions_int) {
                your_mark_isTV.setText("Новый рекорд: " + all_questions_int);
                StatisticsSave(highestSurvivalScoreKey, all_questions_int);
            } else
                your_mark_isTV.setText("Счет: " + all_questions_int);

        } else {
            Timer timer = new Timer();
            final float[] counter = {0};
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (counter[0] >= mark) {
                                if (mark == 10)
                                    markTV.setText("10");
                                else
                                    markTV.setText(mark_string);
                                timer.cancel();
                                return;
                            }
                            markTV.setText(String.format("%.1f", (counter[0])));
                            counter[0] += 0.1;
                        }
                    });
                }
            };
            timer.schedule(timerTask, 0, 20);

            //markTV.setText(mark_string);
            your_mark_isTV.setText("Ваша оценка за тест: " + mark_string + "/10"); //хардкод убрать!
        }


        //view_mistakes кнопка в режиме true oor false выполняет функцию "продолжить на новом сете"
        view_mistakes_button.setOnClickListener(view -> {
            if (!wasFinished) {
                if (testsMode == 3) {
                    //создать новый intent на новое активити с просмотром ошибок
                    Intent intentViewMistakes = new Intent(TopicTrainingQuizResultsActivity.this, ExamViewMistakesActivity.class);
                    intentViewMistakes.putExtra("viewMistakesQuestionNumberList", viewMistakesQuestionNumberList);
                    intentViewMistakes.putExtra("viewMistakesQuestionList", viewMistakesQuestionList);
                    intentViewMistakes.putExtra("viewMistakesUserAnswerList", viewMistakesUserAnswerList);
                    intentViewMistakes.putExtra("viewMistakesCorrectAnswerList", viewMistakesCorrectAnswerList);
                    startActivity(intentViewMistakes);
                    wasFinished = true;
                } else if (testsMode == 4) {
                    if (correct >= 8) {
                        Intent intentTOFA = new Intent(TopicTrainingQuizResultsActivity.this, TrueOrFalseActivity.class);
                        intentTOFA.putExtra("testsMode", testsMode);
                        startActivity(intentTOFA);
                        finish();
                        wasFinished = true;
                    }
                } else {
                    if (correct >= 8) {
                        Intent intentGBI = new Intent(TopicTrainingQuizResultsActivity.this, GuessByImageActivity.class);
                        intentGBI.putExtra("testsMode", testsMode);
                        startActivity(intentGBI);
                        finish();
                        wasFinished = true;
                    }
                }
            }
        });


        retry_button.setOnClickListener(view -> {
            if (!wasFinished) {
                if (testsMode != 4 && testsMode != 5 && testsMode != 6) {
                    Intent intent = new Intent(TopicTrainingQuizResultsActivity.this, TopicTrainingActivity.class);
                    intent.putExtra("spinnerChoice", spinnerChoice);
                    intent.putExtra("spinnerChoiceAllTopics", spinnerChoiceAllTopics);
                    intent.putExtra("testsMode", testsMode);
                    startActivity(intent);
                    finish();
                } else if (testsMode == 4) {
                    if (correct >= 8)
                        StatisticsSave(setsCompletedKey, prefQRA.getInt(setsCompletedKey, 1) - 1);
                    Intent intentTOFAretry = new Intent(TopicTrainingQuizResultsActivity.this, TrueOrFalseActivity.class);
                    intentTOFAretry.putExtra("testsMode", testsMode);
                    startActivity(intentTOFAretry);
                    finish();
                } else if (testsMode == 5) {
                    if (correct >= 8)
                        StatisticsSave(setsCompletedKeyGBI, prefQRA.getInt(setsCompletedKeyGBI, 1) - 1);
                    Intent intentGBIretry = new Intent(TopicTrainingQuizResultsActivity.this, GuessByImageActivity.class);
                    intentGBIretry.putExtra("testsMode", testsMode);
                    startActivity(intentGBIretry);
                    finish();
                } else { //if testsMode == 6
                    Intent intentSurvival = new Intent(TopicTrainingQuizResultsActivity.this, SurvivalActivity.class);
                    intentSurvival.putExtra("testsMode", testsMode);
                    startActivity(intentSurvival);
                    finish();
                }
                wasFinished = true;
            }
        });

        exit_button.setOnClickListener(view -> finish());
    }


    @Override
    protected void onResume() {
        super.onResume();
        appSessionStartTTQRA = System.currentTimeMillis();
        wasFinished = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timeInAppKeyQRA = "TimeInApp";
        StatisticsSaveLongQRA(timeInAppKeyQRA, prefQRA.getLong(timeInAppKeyQRA, 0) + (System.currentTimeMillis() - appSessionStartTTQRA));
    }


    public void StatisticsSave(String key, int value) {
        SharedPreferences.Editor edit = prefQRA.edit();
        edit.putInt(key, value);
        edit.apply();
    }

    private void StatisticsSaveLongQRA(String key, long value) {
        SharedPreferences.Editor edit = prefQRA.edit();
        edit.putLong(key, value);
        edit.apply();
    }


    @Override
    protected void onDestroy() {
        finish();
        super.onDestroy();
    }
}