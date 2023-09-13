package com.eduscope.eduscope.tests;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;

import com.eduscope.eduscope.R;
import com.eduscope.eduscope.sqlite.SQLiteHelper;

import java.util.List;
import java.util.Random;

public class GuessByImageActivity extends AppCompatActivity {

    private SharedPreferences def_pref;
    private int correctAnswers;
    private ProgressBar progressBar;
    private int testsMode;
    private ImageView question;
    private int iQuestionOrder = 0;
    private int[] questionOrder;
    private final int[] optionOrder = {0, 1, 2, 3};
    private long backPressedTime;
    private SharedPreferences pref;
    private long appSessionStart;
    private final String setsCompletedGBIKey = "SetsCompletedGuessByImage", guessByImageCounterKey = "GuessByImageCounter", counter10sKey = "10sCounter";
    private long sessionTimeStart;
    private boolean wasFinishPressed = false;
    private int currentQuestionPosition = 0;
    private String selectedOptionByUser = "";
    private boolean isChronometerRunning = false;
    private Chronometer chronometer;
    private long pauseOffset;
    private TextView questionNumber;
    private AppCompatButton option1, option2, option3, option4, check_button;
    private List<QuestionListGuessByImage> questionList;
    private SQLiteHelper sqLiteHelper = new SQLiteHelper(GuessByImageActivity.this);
    private String subject;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_by_image);

        //Image zoom dialog:
        Dialog imageZoomDialog = new Dialog(this);
        imageZoomDialog.setCanceledOnTouchOutside(true);
        imageZoomDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        imageZoomDialog.setContentView(R.layout.dialog_image_guess);
        imageZoomDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        imageZoomDialog.setCancelable(true);
        final ImageView imageZoomDialogIV = imageZoomDialog.findViewById(R.id.image_zoom_dialog_iv);


        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //Скрыть встроенный заголовок
        dialog.setContentView(R.layout.preview_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //Прозрачный фон окна
        dialog.setCancelable(false); //окно нельзя закрыть кнопкой назад
        final TextView dialogText = dialog.findViewById(R.id.dialog_textview);
        dialogText.setText(getString(R.string.dialog_completed_text_gbi));
        final AppCompatButton continueBtnDialogCompleted = dialog.findViewById(R.id.continue_button_dialog);
        continueBtnDialogCompleted.setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.button_when_pressed_green_stroke_10_not_transparent));
        continueBtnDialogCompleted.setOnClickListener(view -> dialog.dismiss());


        final ImageView back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(view -> {
            if (backPressedTime + 2000 > System.currentTimeMillis())
                finish();
            else
                Toast.makeText(getBaseContext(), getText(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
            backPressedTime = System.currentTimeMillis();
        });

        testsMode = getIntent().getIntExtra("testsMode", 5);
        subject = getIntent().getStringExtra("subject");

        pref = getSharedPreferences("Statistics", Context.MODE_PRIVATE);
        chronometer = findViewById(R.id.chronometer);
        questionNumber = findViewById(R.id.question_number);
        question = findViewById(R.id.question_image);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        check_button = findViewById(R.id.check_button);
        progressBar = findViewById(R.id.progress_bar_tests);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);

        def_pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (def_pref.getBoolean("theme_switch_preference", false)) {
            questionNumber.setBackgroundResource(R.drawable.round_back_dark_10);
            questionNumber.setTextColor(getResources().getColor(R.color.light_text));
            option1.setBackgroundResource(R.drawable.round_back_dark_10);
            option2.setBackgroundResource(R.drawable.round_back_dark_10);
            option3.setBackgroundResource(R.drawable.round_back_dark_10);
            option4.setBackgroundResource(R.drawable.round_back_dark_10);
            option1.setTextColor(getResources().getColor(R.color.light_text));
            option2.setTextColor(getResources().getColor(R.color.light_text));
            option3.setTextColor(getResources().getColor(R.color.light_text));
            option4.setTextColor(getResources().getColor(R.color.light_text));
            check_button.setBackgroundResource(R.drawable.round_button_dark_20);
            check_button.setTextColor(getResources().getColor(R.color.light_text));
        }


        String wereAllSetsCompletedKey = "WereAllSetsCompletedGBI";


        int setsCompleted = pref.getInt(setsCompletedGBIKey, 0);
        if (setsCompleted == 4) {
            if (!pref.getBoolean(wereAllSetsCompletedKey, false)) {
                dialog.show(); //появится только один раз
                StatisticsSaveBoolean(wereAllSetsCompletedKey, true);
            }
            StatisticsSave(setsCompletedGBIKey, 0);
            //Диалоговое окно, типа вы молодцы, все прошли
        }

        questionList = sqLiteHelper.selectGuessByImage(subject, setsCompleted);
        //questionList = QuestionBankGuessByImage.getQuestions(setsCompleted);

        questionOrder = new int[questionList.size()];
        for (int i = 0; i < questionList.size(); i++) {
            questionOrder[i] = i;
        }
        Shuffle(questionOrder);
        Shuffle(optionOrder);
        currentQuestionPosition = questionOrder[iQuestionOrder];

        actionBarTitle.setText(getString(R.string.level) + " " + (pref.getInt(setsCompletedGBIKey, 0) + 1));
        //actionBarTitle.setText(QuestionBankGuessByImage.getActionBarTitle(pref.getInt(setsCompletedGBIKey, 0)));

        startChronometer();
        sessionTimeStart = System.currentTimeMillis();

        questionNumber.setText((iQuestionOrder + 1) + "/" + questionList.size());
        float scale = getResources().getDisplayMetrics().density;
        int widthInDp = 170; // your desired width in dp
        int heightInDp = 215; // your desired height in dp
        int widthInPx = (int) (widthInDp * scale + 0.5f); // add 0.5f for rounding
        int heightInPx = (int) (heightInDp * scale + 0.5f);
        question.setImageBitmap(Bitmap.createScaledBitmap(questionList.get(currentQuestionPosition).getQuestion(), widthInPx, heightInPx, true));
        option1.setText(questionList.get(currentQuestionPosition).getOption(optionOrder[0]));
        option2.setText(questionList.get(currentQuestionPosition).getOption(optionOrder[1]));
        option3.setText(questionList.get(currentQuestionPosition).getOption(optionOrder[2]));
        option4.setText(questionList.get(currentQuestionPosition).getOption(optionOrder[3]));


        imageZoomDialogIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageZoomDialog.dismiss();
            }
        });

        question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float scale = getResources().getDisplayMetrics().density;
                int widthInDp = 250; // your desired width in dp
                int heightInDp = 335; // your desired height in dp
                int widthInPx = (int) (widthInDp * scale + 0.5f); // add 0.5f for rounding
                int heightInPx = (int) (heightInDp * scale + 0.5f);
                imageZoomDialogIV.setImageBitmap(Bitmap.createScaledBitmap(questionList.get(currentQuestionPosition).getQuestion(), widthInPx, heightInPx, true));
                //imageZoomDialogIV.setImageBitmap(questionList.get(currentQuestionPosition).getQuestion());
                imageZoomDialog.show();
            }
        });


        option1.setOnClickListener(view -> {
            if (selectedOptionByUser.isEmpty()) {
                if (iQuestionOrder + 1 == questionList.size())
                    check_button.setText(R.string.test_complete);
                else
                    check_button.setText(R.string.check_answer);
                selectedOptionByUser = option1.getText().toString();
                if (!def_pref.getBoolean("theme_switch_preference", false))
                    option1.setBackgroundResource(R.drawable.round_back_red10);
                else
                    option1.setBackgroundResource(R.drawable.round_back_red10_dark_theme);
                option1.setTextColor(Color.WHITE);
                revealAnswer();
                questionList.get(currentQuestionPosition).setUserSelectedOption(selectedOptionByUser);
            }
        });
        option2.setOnClickListener(view -> {
            if (selectedOptionByUser.isEmpty()) {
                if (iQuestionOrder + 1 == questionList.size())
                    check_button.setText(R.string.test_complete);
                else
                    check_button.setText(R.string.check_answer);
                selectedOptionByUser = option2.getText().toString();
                if (!def_pref.getBoolean("theme_switch_preference", false))
                    option2.setBackgroundResource(R.drawable.round_back_red10);
                else
                    option2.setBackgroundResource(R.drawable.round_back_red10_dark_theme);
                option2.setTextColor(Color.WHITE);
                revealAnswer();
                questionList.get(currentQuestionPosition).setUserSelectedOption(selectedOptionByUser);
            }
        });
        option3.setOnClickListener(view -> {
            if (selectedOptionByUser.isEmpty()) {
                if (iQuestionOrder + 1 == questionList.size())
                    check_button.setText(R.string.test_complete);
                else
                    check_button.setText(R.string.check_answer);
                selectedOptionByUser = option3.getText().toString();
                if (!def_pref.getBoolean("theme_switch_preference", false))
                    option3.setBackgroundResource(R.drawable.round_back_red10);
                else
                    option3.setBackgroundResource(R.drawable.round_back_red10_dark_theme);
                option3.setTextColor(Color.WHITE);
                revealAnswer();
                questionList.get(currentQuestionPosition).setUserSelectedOption(selectedOptionByUser);
            }
        });
        option4.setOnClickListener(view -> {
            if (selectedOptionByUser.isEmpty()) {
                if (iQuestionOrder + 1 == questionList.size())
                    check_button.setText(R.string.test_complete);
                else
                    check_button.setText(R.string.check_answer);
                selectedOptionByUser = option4.getText().toString();
                if (!def_pref.getBoolean("theme_switch_preference", false))
                    option4.setBackgroundResource(R.drawable.round_back_red10);
                else
                    option4.setBackgroundResource(R.drawable.round_back_red10_dark_theme);
                option4.setTextColor(Color.WHITE);
                revealAnswer();
                questionList.get(currentQuestionPosition).setUserSelectedOption(selectedOptionByUser);
            }
        });
        check_button.setOnClickListener(view -> {
            if (!wasFinishPressed) {
                if (selectedOptionByUser.isEmpty()) {
                    check_button.setText(R.string.check_button_no_selected_option);
                } else {
                    questionChange();
                }
            }
        });


    }


    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    private void questionChange() {
        //currentQuestionPosition++;
        if (iQuestionOrder + 1 != questionList.size())
            currentQuestionPosition = questionOrder[++iQuestionOrder];
        else
            iQuestionOrder++;

        if (Build.VERSION.SDK_INT >= 24)
            progressBar.setProgress((int) ((float) iQuestionOrder / questionList.size() * 100), true);
        else
            progressBar.setProgress((int) ((float) iQuestionOrder / questionList.size() * 100));

        if (questionList.get(questionOrder[iQuestionOrder - 1]).getAnswer().equals(questionList.get(questionOrder[iQuestionOrder - 1]).getUserSelectedOption()))
            progressBar.setProgressDrawable(getDrawable(R.drawable.custom_progress_bar));
        else
            progressBar.setProgressDrawable(getDrawable(R.drawable.custom_progress_bar_red));


        if (iQuestionOrder + 1 == questionList.size()) {
            check_button.setText("Завершить");
        }
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
            questionNumber.setText((iQuestionOrder + 1) + "/" + questionList.size());
            float scale = getResources().getDisplayMetrics().density;
            int widthInDp = 170; // your desired width in dp
            int heightInDp = 215; // your desired height in dp
            int widthInPx = (int) (widthInDp * scale + 0.5f); // add 0.5f for rounding
            int heightInPx = (int) (heightInDp * scale + 0.5f);
            question.setImageBitmap(Bitmap.createScaledBitmap(questionList.get(currentQuestionPosition).getQuestion(), widthInPx, heightInPx, true));
            option1.setText(questionList.get(currentQuestionPosition).getOption(optionOrder[0]));
            option2.setText(questionList.get(currentQuestionPosition).getOption(optionOrder[1]));
            option3.setText(questionList.get(currentQuestionPosition).getOption(optionOrder[2]));
            option4.setText(questionList.get(currentQuestionPosition).getOption(optionOrder[3]));
        } else {
            //Чтобы не было двойных нажатий, портящих статистику, добавлен boolean:
            if (!wasFinishPressed)
                showResults();
            wasFinishPressed = true;
        }
    }


    private int getCorrectAnswers() {
        correctAnswers = 0;
        for (int i = 0; i < questionList.size(); i++) {
            final String getUserSelectedAnswer = questionList.get(i).getUserSelectedOption();
            final String getAnswer = questionList.get(i).getAnswer();

            if (getUserSelectedAnswer.equals(getAnswer))
                correctAnswers++;
        }

        String questionsAnsweredKey = "QuestionsAnswered";
        StatisticsSave(questionsAnsweredKey, /*questionsAnsweredCount*/ pref.getInt(questionsAnsweredKey, 0) + questionList.size());
        String rightAnswersKey = "RightAnswers";
        StatisticsSave(rightAnswersKey, /*rightAnswersCount*/ pref.getInt(rightAnswersKey, 0) + correctAnswers);
        if (correctAnswers == questionList.size())
            StatisticsSave(counter10sKey, pref.getInt(counter10sKey, 0) + 1);
        return correctAnswers;
    }


    private void showResults() {
        StatisticsSave(guessByImageCounterKey, pref.getInt(guessByImageCounterKey, 0) + 1);
        String testsCompletedKey = "TestsCompleted";
        StatisticsSave(testsCompletedKey, /*++testsCompletedCount*/ pref.getInt(testsCompletedKey, 0) + 1);
        pauseChronometer();
        String timeInTestsKey = "TimeInTests";
        StatisticsSaveLong(timeInTestsKey, /*timeInTestsCount*/ pref.getLong(timeInTestsKey, 0) + (System.currentTimeMillis() - sessionTimeStart));
        //StatisticsSaveLong(timeInTestsKey, 0);
        Intent intent = new Intent(GuessByImageActivity.this, TopicTrainingQuizResultsActivity.class);
        intent.putExtra("chronometer", String.valueOf(chronometer.getText()));
        intent.putExtra("correct", getCorrectAnswers());
        intent.putExtra("all_questions", questionList.size());
        intent.putExtra("testsMode", testsMode);
        if (correctAnswers >= 8)
            StatisticsSave(setsCompletedGBIKey, pref.getInt(setsCompletedGBIKey, 0) + 1);
        startActivity(intent);
        resetChronometer();
        finish();
    }


    private void revealAnswer() {
        final String getAnswer = questionList.get(currentQuestionPosition).getAnswer();
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


    public void StatisticsSaveBoolean(String key, boolean value) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean(key, value);
        edit.apply();
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

    private static void Shuffle(int[] array) {
        Random random = new Random();
        for (int i = 0; i < array.length; ++i) {
            int j = random.nextInt(array.length);
            int tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pauseChronometer();
        resetChronometer();
        finish();
    }
}