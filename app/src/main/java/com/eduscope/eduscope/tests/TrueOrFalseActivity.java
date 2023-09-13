package com.eduscope.eduscope.tests;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
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

public class TrueOrFalseActivity extends AppCompatActivity {

    private SharedPreferences def_pref;
    private ProgressBar progressBar;
    private int iQuestionOrder = 0;
    private int[] questionOrder;

    private int correctAnswers;
    private Dialog dialogCompleted;
    private Dialog dialog;
    private TextView dialogTitle;
    private TextView dialogText;

    private int testsMode;
    private SharedPreferences pref;
    private long appSessionStart;
    private final String setsCompletedKey = "SetsCompleted", trueOrFalseCounterKey = "TrueOrFalseCounter", counter10sKey = "10sCounter";
    private long sessionTimeStart; //для статистики времени, проведенного в тестах
    private long backPressedTime; //относится к предохранению случайных выходов
    private boolean wasFinishPressed = false;

    private int currentQuestionPosition = 0;
    private String selectedOptionByUser = "";
    private boolean isChronometerRunning = false;
    private Chronometer chronometer;
    private long pauseOffset;
    private TextView question_number;
    private TextView question;
    private SQLiteHelper sqLiteHelper = new SQLiteHelper(TrueOrFalseActivity.this);

    private List<QuestionListTrueOrFalse> questionListTrueOrFalse;
    private String subject;
    //Здесь дописать аналог класса QuestionList


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_true_or_false);

        pref = getSharedPreferences("Statistics", Context.MODE_PRIVATE);
        def_pref = PreferenceManager.getDefaultSharedPreferences(this);

        //Диалоговое окно после неправильного ответа:
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //Скрыть встроенный заголовок
        if (!def_pref.getBoolean("theme_switch_preference", false))
            dialog.setContentView(R.layout.preview_dialog);
        else
            dialog.setContentView(R.layout.preview_dialog_dark_theme);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //Прозрачный фон окна
        dialog.setCancelable(false); //окно нельзя закрыть кнопкой назад
        AppCompatButton continueBtnDialog = dialog.findViewById(R.id.continue_button_dialog);
        dialogTitle = dialog.findViewById(R.id.dialog_title);
        dialogText = dialog.findViewById(R.id.dialog_textview);
        progressBar = findViewById(R.id.progress_bar_tests);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);

        continueBtnDialog.setOnClickListener(view -> {
            dialog.dismiss();
            QuestionChange();
        });

        testsMode = getIntent().getIntExtra("testsMode", 4); //Зачем передавать testsMode через интент, когда можно в каждом режиме по-своему его инициализировать
        subject = getIntent().getStringExtra("subject");
        //и передавать в QuizResults?

        //Диалоговое окно после прохождения всех сетов вопросов:
        //чтобы не показывать одно и то же диалоговое окно после завершения всех сетов дважды
        String wereAllSetsCompletedKey = "IsAllSetsCompleted";
        if (!pref.getBoolean(wereAllSetsCompletedKey, false)) { //чтобы каждый раз не инициализировать, а только до первого завершения
            dialogCompleted = new Dialog(this);
            dialogCompleted.requestWindowFeature(Window.FEATURE_NO_TITLE);
            if (!def_pref.getBoolean("theme_switch_preference", false))
                dialogCompleted.setContentView(R.layout.preview_dialog);
            else
                dialogCompleted.setContentView(R.layout.preview_dialog_dark_theme);
            dialogCompleted.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialogCompleted.setCancelable(false);
            AppCompatButton continueBtnDialogCompleted = dialogCompleted.findViewById(R.id.continue_button_dialog);
            continueBtnDialogCompleted.setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.button_when_pressed_green_stroke_10_not_transparent));
            //зеленая кнопка, чтобы привлечь внимание, обычно она синяя

            continueBtnDialogCompleted.setOnClickListener(view -> dialogCompleted.dismiss());
        }

        pref.getBoolean(wereAllSetsCompletedKey, false);
        final ImageView back_button = findViewById(R.id.back_button);
        chronometer = findViewById(R.id.chronometer);

        question = findViewById(R.id.question);
        AppCompatButton option1true = findViewById(R.id.option1);
        AppCompatButton option2false = findViewById(R.id.option2);

        question_number = findViewById(R.id.question_number);
        if (def_pref.getBoolean("theme_switch_preference", false)) {
            question_number.setTextColor(getResources().getColor(R.color.light_text));
            question_number.setBackgroundResource(R.drawable.round_back_dark_10);
            question.setTextColor(getResources().getColor(R.color.light_text));
            question.setBackgroundResource(R.drawable.round_back_dark_10);
            option1true.setBackgroundResource(R.drawable.button_green_when_pressed_green_stroke_10_dark_theme);
            option1true.setTextColor(getResources().getColor(R.color.light_text));
            option2false.setBackgroundResource(R.drawable.button_red_when_pressed_red_stroke_10_dark_theme);
            option2false.setTextColor(getResources().getColor(R.color.light_text));
        }

        back_button.setOnClickListener(view -> {
            if (backPressedTime + 2000 > System.currentTimeMillis())
                finish();
            else
                Toast.makeText(getBaseContext(), getText(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
            backPressedTime = System.currentTimeMillis();
        });

        //Сохраняем счетчик завершенных наборов вопросов в Shared preferences, если = кол-ву сетов, то обнуляем, запуская цикл по новому кругу
        int setsCompleted = pref.getInt(setsCompletedKey, 0);
        if (setsCompleted == 10) {
            if (!pref.getBoolean(wereAllSetsCompletedKey, false)) {
                dialogCompleted.show(); //появится только один раз
                StatisticsSaveBoolean(wereAllSetsCompletedKey, true);
            }
            //StatisticsSaveBoolean(wereAllSetsCompletedKey, false); //для сброса появления окна после прохождения сетов
            setsCompleted = 0;
            StatisticsSave(setsCompletedKey, setsCompleted); //не забыть инкремировать и пересохранить после прохождения сета
        }

        actionBarTitle.setText(getString(R.string.level) + " " + (setsCompleted+1));

        startChronometer();
        sessionTimeStart = System.currentTimeMillis();

        questionListTrueOrFalse = sqLiteHelper.selectTrueOrFalseQuestions(subject, pref.getInt(setsCompletedKey, 0));
        //questionListTrueOrFalse = QuestionBankTrueOrFalse.getQuestions(setsCompleted);

        questionOrder = new int[questionListTrueOrFalse.size()];
        for (int i = 0; i < questionListTrueOrFalse.size(); i++) {
            questionOrder[i] = i;
        }
        Shuffle(questionOrder);
        currentQuestionPosition = questionOrder[iQuestionOrder];

        question.setText(questionListTrueOrFalse.get(currentQuestionPosition).getQuestion());
        question_number.setText((iQuestionOrder + 1) + "/" + questionListTrueOrFalse.size());


        option1true.setOnClickListener(view -> {
            if (selectedOptionByUser.isEmpty()) { //Чтобы не было проблемы с двойным нажатием на последнем вопросе, как в exam
                selectedOptionByUser = "Правда";
                questionListTrueOrFalse.get(currentQuestionPosition).setUserSelectedOption(selectedOptionByUser);
                if (questionListTrueOrFalse.get(currentQuestionPosition).getAnswer().equals(questionListTrueOrFalse.get(currentQuestionPosition).getUserSelectedOption()))
                    QuestionChange();
                else {
                    RevealAnswer();
                }
            }
        });

        option2false.setOnClickListener(view -> {
            if (selectedOptionByUser.isEmpty()) { //Чтобы не было проблемы с двойным нажатием на последнем вопросе, как в exam
                selectedOptionByUser = "Ложь";
                questionListTrueOrFalse.get(currentQuestionPosition).setUserSelectedOption(selectedOptionByUser);
                if (questionListTrueOrFalse.get(currentQuestionPosition).getAnswer().equals(questionListTrueOrFalse.get(currentQuestionPosition).getUserSelectedOption()))
                    QuestionChange();
                else {
                    RevealAnswer();
                }
            }
        });

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


    private void RevealAnswer() {
        dialog.show();
        if (questionListTrueOrFalse.get(currentQuestionPosition).getAnswer().equals("Правда"))
            dialogTitle.setText(getText(R.string.this_is_true));
        else
            dialogTitle.setText(getText(R.string.this_is_false));
        dialogText.setText(questionListTrueOrFalse.get(currentQuestionPosition).getRevealAnswer());

    }


    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    private void QuestionChange() {
        if (iQuestionOrder + 1 != questionListTrueOrFalse.size())
            currentQuestionPosition = questionOrder[++iQuestionOrder];
        else
            iQuestionOrder++;

        if (Build.VERSION.SDK_INT >= 24)
            progressBar.setProgress((int) ((float) iQuestionOrder / questionListTrueOrFalse.size() * 100), true);
        else
            progressBar.setProgress((int) ((float) iQuestionOrder / questionListTrueOrFalse.size() * 100));

        if (questionListTrueOrFalse.get(questionOrder[iQuestionOrder - 1]).getAnswer().equals(questionListTrueOrFalse.get(questionOrder[iQuestionOrder - 1]).getUserSelectedOption()))
            progressBar.setProgressDrawable(getDrawable(R.drawable.custom_progress_bar));
        else
            progressBar.setProgressDrawable(getDrawable(R.drawable.custom_progress_bar_red));


        if (iQuestionOrder < questionListTrueOrFalse.size()) {
            selectedOptionByUser = "";
            question_number.setText((iQuestionOrder + 1) + "/" + questionListTrueOrFalse.size());
            question.setText(questionListTrueOrFalse.get(currentQuestionPosition).getQuestion());
        } else {
            //Чтобы не было двойных нажатий, портящих статистику, добавлен boolean:
            if (!wasFinishPressed)
                showResults();
            wasFinishPressed = true;
        }
    }

    private void showResults() {
        StatisticsSave(trueOrFalseCounterKey, pref.getInt(trueOrFalseCounterKey, 0) + 1);
        //private long timeInTestsCount;
        String testsCompletedKey = "TestsCompleted";
        StatisticsSave(testsCompletedKey, /*++testsCompletedCount*/ pref.getInt(testsCompletedKey, 0) + 1);
        pauseChronometer();
        String timeInTestsKey = "TimeInTests";
        StatisticsSaveLong(timeInTestsKey, /*timeInTestsCount*/ pref.getLong(timeInTestsKey, 0) + (System.currentTimeMillis() - sessionTimeStart));
        //StatisticsSaveLong(timeInTestsKey, 0);
        Intent intent = new Intent(TrueOrFalseActivity.this, TopicTrainingQuizResultsActivity.class);
        intent.putExtra("chronometer", String.valueOf(chronometer.getText()));
        intent.putExtra("correct", getCorrectAnswers());
        intent.putExtra("all_questions", questionListTrueOrFalse.size());
        intent.putExtra("testsMode", testsMode);
        startActivity(intent);
        if (correctAnswers >= 8)
            StatisticsSave(setsCompletedKey, pref.getInt(setsCompletedKey, 0) + 1);
        resetChronometer();
        finish();
    }


    private int getCorrectAnswers() {
        correctAnswers = 0;
        for (int i = 0; i < questionListTrueOrFalse.size(); i++) {
            final String getUserSelectedAnswer = questionListTrueOrFalse.get(i).getUserSelectedOption();
            final String getAnswer = questionListTrueOrFalse.get(i).getAnswer();

            if (getUserSelectedAnswer.equals(getAnswer))
                correctAnswers++;
        }
        String questionsAnsweredKey = "QuestionsAnswered";
        StatisticsSave(questionsAnsweredKey, /*questionsAnsweredCount*/ pref.getInt(questionsAnsweredKey, 0) + questionListTrueOrFalse.size());
        String rightAnswersKey = "RightAnswers";
        StatisticsSave(rightAnswersKey, /*rightAnswersCount*/ pref.getInt(rightAnswersKey, 0) + correctAnswers);
        if (correctAnswers == questionListTrueOrFalse.size())
            StatisticsSave(counter10sKey, pref.getInt(counter10sKey, 0) + 1);
        return correctAnswers;
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
    protected void onResume() {
        super.onResume();
        appSessionStart = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timeInAppKey = "TimeInApp";
        StatisticsSaveLong(timeInAppKey, pref.getLong(timeInAppKey, 0) + (System.currentTimeMillis() - appSessionStart));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pauseChronometer();
        resetChronometer();
        finish();
    }
}