package com.eduscope.eduscope.tests;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.eduscope.eduscope.R;
import com.eduscope.eduscope.sqlite.SQLiteHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TopicTrainingActivity extends AppCompatActivity {

    private SharedPreferences def_pref;
    private ProgressBar progressBar;
    private final int[] optionOrder = {0, 1, 2, 3}; //чтобы не переписывать функцию Shuffle, будет 0 1 2 3 вместо 1 2 3 4
    private int iQuestionOrder = 0;
    private int[] questionOrder;

    private SharedPreferences pref;
    private long appSessionStartTTA;

    private long sessionTimeStart; //для статистики времени, проведенного в тестах
    private long backPressedTime; //относится к предохранению случайных выходов
    private boolean wasFinishPressed = false;
    private String spinnerChoice, spinnerChoiceAllTopics;
    private String subject;
    private int testsMode;
    private int currentQuestionPosition = 0;
    private String selectedOptionByUser = "";
    private boolean isChronometerRunning = false;
    private Chronometer chronometer;
    private long pauseOffset;
    private TextView question_number, question;
    private AppCompatButton option1, option2, option3, option4, check_button;
    //Results:
    private List<QuestionList> questionList;
    private final String ttCounterKey = "TTCounter",
            ttAllTopicsCounterKey = "TTAllTopicsCounter", examCounterKey = "ExamCounter", counter10sKey = "10sCounter";

    private final List<Integer> viewMistakesQuestionNumberList = new ArrayList<>(); //Для exam (просмотр ошибок)
    private final List<String> viewMistakesQuestionList = new ArrayList<>();
    private final List<String> viewMistakesUserAnswerList = new ArrayList<>();
    private final List<String> viewMistakesCorrectAnswerList = new ArrayList<>();

    private SQLiteHelper sqLiteHelper = new SQLiteHelper(TopicTrainingActivity.this);

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_training);
        pref = getSharedPreferences("Statistics", Context.MODE_PRIVATE);

        final ImageView back_button = findViewById(R.id.back_button);
        chronometer = findViewById(R.id.chronometer);

        //Добавить сюда listview, оперировать лэйаутами с помощью setVisibility, чтобы не создавать активити из-за мелочи

        question_number = findViewById(R.id.question_number);
        question = findViewById(R.id.question);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        check_button = findViewById(R.id.check_button);

        def_pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (def_pref.getBoolean("theme_switch_preference", false)) {
            question.setBackgroundResource(R.drawable.round_back_dark_10);
            question.setTextColor(getResources().getColor(R.color.light_text));
            question_number.setBackgroundResource(R.drawable.round_back_dark_10);
            question_number.setTextColor(getResources().getColor(R.color.light_text));
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

        TextView actionbarTitle = findViewById(R.id.actionbar_title);

        progressBar = findViewById(R.id.progress_bar_tests);

        back_button.setOnClickListener(view -> {
            if (backPressedTime + 2000 > System.currentTimeMillis())
                finish();
            else
                Toast.makeText(getBaseContext(), getText(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
            backPressedTime = System.currentTimeMillis();
        });


        //Хз, зачем надо все эти условия, может можно сократить код
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                spinnerChoice = null;
            } else {
                spinnerChoice = extras.getString("spinnerChoice");
                spinnerChoiceAllTopics = extras.getString("spinnerChoiceAllTopics");
                testsMode = extras.getInt("testsMode");
                subject = extras.getString("subject");
            }
        } else {
            spinnerChoice = (String) savedInstanceState.getSerializable("spinnerChoice");
            spinnerChoiceAllTopics = (String) savedInstanceState.getSerializable("spinnerChoiceAllTopics");
            testsMode = (int) savedInstanceState.getSerializable("testsMode");
            subject = (String) savedInstanceState.getSerializable("subject");

        }

        if (testsMode == 1) {
            assert spinnerChoice != null;
            questionList = sqLiteHelper.selectTopicTrainingQuestions(subject, spinnerChoice);
            //questionList = QuestionBank.getQuestions(spinnerChoice);
            actionbarTitle.setText(getString(R.string.training_on_chapter) + " " + spinnerChoice.substring(0, spinnerChoice.indexOf(".")));
            //actionbarTitle.setText(QuestionBank.getActionbarTitle(spinnerChoice));
        } else if (testsMode == 2) {
            questionList = sqLiteHelper.selectAllTopicsTrainingQuestions(subject, spinnerChoiceAllTopics);
            //questionList = QuestionBankAllTopics.getQuestions(spinnerChoiceAllTopics);
            actionbarTitle.setText(getString(R.string.training) + " / " + getString(R.string.ticket) + " " + spinnerChoiceAllTopics.substring((spinnerChoiceAllTopics.indexOf(" ") + 1)));
            //actionbarTitle.setText(QuestionBankAllTopics.getActionbarTitle(spinnerChoiceAllTopics));
        } else if (testsMode == 3) {
            questionList = sqLiteHelper.selectAllTopicsTrainingQuestions(subject, spinnerChoiceAllTopics);
            actionbarTitle.setText(getString(R.string.exam) + " / " + getString(R.string.ticket) + " " + spinnerChoiceAllTopics.substring((spinnerChoiceAllTopics.indexOf(" ") + 1)));
        }

        //Заполнение questionOrder по порядку индексами всех вопросов:
        questionOrder = new int[questionList.size()];
        for (int i = 0; i < questionList.size(); i++) {
            questionOrder[i] = i;
        }
        Shuffle(questionOrder);
        Shuffle(optionOrder);
        currentQuestionPosition = questionOrder[iQuestionOrder];

        startChronometer();
        sessionTimeStart = System.currentTimeMillis();

        question_number.setText((iQuestionOrder + 1) + "/" + questionList.size());
        question.setText(questionList.get(currentQuestionPosition).getQuestion());
        option1.setText(questionList.get(currentQuestionPosition).getOption(optionOrder[0]));
        option2.setText(questionList.get(currentQuestionPosition).getOption(optionOrder[1]));
        option3.setText(questionList.get(currentQuestionPosition).getOption(optionOrder[2]));
        option4.setText(questionList.get(currentQuestionPosition).getOption(optionOrder[3]));

        option1.setOnClickListener(view -> {
            if (selectedOptionByUser.isEmpty()) {
                if (iQuestionOrder + 1 == questionList.size())
                    check_button.setText(R.string.test_complete);
                else
                    check_button.setText(R.string.check_answer);
                selectedOptionByUser = option1.getText().toString();
                if (testsMode != 3) {
                    if (!def_pref.getBoolean("theme_switch_preference", false))
                        option1.setBackgroundResource(R.drawable.round_back_red10);
                    else
                        option1.setBackgroundResource(R.drawable.round_back_red10_dark_theme);
                    option1.setTextColor(Color.WHITE);
                    revealAnswer();
                } else {
                    if (!def_pref.getBoolean("theme_switch_preference", false))
                        option1.setBackgroundResource(R.drawable.round_back_grey10);
                    else
                        option1.setBackgroundResource(R.drawable.round_back_grey10_dark_theme);
                }
                questionList.get(currentQuestionPosition).setUserSelectedOption(selectedOptionByUser);
            } else { //В экзаменационном режиме можно перевыбирать ответ
                if (testsMode == 3 && iQuestionOrder != questionList.size()) {
                    if (!def_pref.getBoolean("theme_switch_preference", false)) {
                        option1.setBackgroundResource(R.drawable.round_back_grey10);
                        option2.setBackgroundResource(R.drawable.round_back_white_10);
                        option3.setBackgroundResource(R.drawable.round_back_white_10);
                        option4.setBackgroundResource(R.drawable.round_back_white_10);
                    } else {
                        option1.setBackgroundResource(R.drawable.round_back_grey10_dark_theme);
                        option2.setBackgroundResource(R.drawable.round_back_dark_10);
                        option3.setBackgroundResource(R.drawable.round_back_dark_10);
                        option4.setBackgroundResource(R.drawable.round_back_dark_10);
                    }
                    selectedOptionByUser = option1.getText().toString();
                    questionList.get(currentQuestionPosition).setUserSelectedOption(selectedOptionByUser);
                }
            }

        });
        option2.setOnClickListener(view -> {
            if (selectedOptionByUser.isEmpty()) {
                if (iQuestionOrder + 1 == questionList.size())
                    check_button.setText(R.string.test_complete);
                else
                    check_button.setText(R.string.check_answer);
                selectedOptionByUser = option2.getText().toString();
                if (testsMode != 3) {
                    if (!def_pref.getBoolean("theme_switch_preference", false))
                        option2.setBackgroundResource(R.drawable.round_back_red10);
                    else
                        option2.setBackgroundResource(R.drawable.round_back_red10_dark_theme);
                    option2.setTextColor(Color.WHITE);
                    revealAnswer();
                } else {
                    if (!def_pref.getBoolean("theme_switch_preference", false))
                        option2.setBackgroundResource(R.drawable.round_back_grey10);
                    else
                        option2.setBackgroundResource(R.drawable.round_back_grey10_dark_theme);
                }
                questionList.get(currentQuestionPosition).setUserSelectedOption(selectedOptionByUser);
            } else { //В экзаменационном режиме можно перевыбирать ответ
                if (testsMode == 3 && iQuestionOrder != questionList.size()) {
                    if (!def_pref.getBoolean("theme_switch_preference", false)) {
                        option1.setBackgroundResource(R.drawable.round_back_white_10);
                        option2.setBackgroundResource(R.drawable.round_back_grey10);
                        option3.setBackgroundResource(R.drawable.round_back_white_10);
                        option4.setBackgroundResource(R.drawable.round_back_white_10);
                    } else {
                        option1.setBackgroundResource(R.drawable.round_back_dark_10);
                        option2.setBackgroundResource(R.drawable.round_back_grey10_dark_theme);
                        option3.setBackgroundResource(R.drawable.round_back_dark_10);
                        option4.setBackgroundResource(R.drawable.round_back_dark_10);
                    }
                    selectedOptionByUser = option2.getText().toString();
                    questionList.get(currentQuestionPosition).setUserSelectedOption(selectedOptionByUser);
                }
            }
        });
        option3.setOnClickListener(view -> {
            if (selectedOptionByUser.isEmpty()) {
                if (iQuestionOrder + 1 == questionList.size())
                    check_button.setText(R.string.test_complete);
                else
                    check_button.setText(R.string.check_answer);
                selectedOptionByUser = option3.getText().toString();
                if (testsMode != 3) {
                    if (!def_pref.getBoolean("theme_switch_preference", false))
                        option3.setBackgroundResource(R.drawable.round_back_red10);
                    else
                        option3.setBackgroundResource(R.drawable.round_back_red10_dark_theme);
                    option3.setTextColor(Color.WHITE);
                    revealAnswer();
                } else {
                    if (!def_pref.getBoolean("theme_switch_preference", false))
                        option3.setBackgroundResource(R.drawable.round_back_grey10);
                    else
                        option3.setBackgroundResource(R.drawable.round_back_grey10_dark_theme);
                }
                questionList.get(currentQuestionPosition).setUserSelectedOption(selectedOptionByUser);
            } else { //В экзаменационном режиме можно перевыбирать ответ
                if (testsMode == 3 && iQuestionOrder != questionList.size()) {
                    if (!def_pref.getBoolean("theme_switch_preference", false)) {
                        option1.setBackgroundResource(R.drawable.round_back_white_10);
                        option2.setBackgroundResource(R.drawable.round_back_white_10);
                        option3.setBackgroundResource(R.drawable.round_back_grey10);
                        option4.setBackgroundResource(R.drawable.round_back_white_10);
                    } else {
                        option1.setBackgroundResource(R.drawable.round_back_dark_10);
                        option2.setBackgroundResource(R.drawable.round_back_dark_10);
                        option3.setBackgroundResource(R.drawable.round_back_grey10_dark_theme);
                        option4.setBackgroundResource(R.drawable.round_back_dark_10);
                    }
                    selectedOptionByUser = option3.getText().toString();
                    questionList.get(currentQuestionPosition).setUserSelectedOption(selectedOptionByUser);
                }
            }
        });
        option4.setOnClickListener(view -> {
            if (selectedOptionByUser.isEmpty()) {
                if (iQuestionOrder + 1 == questionList.size())
                    check_button.setText(R.string.test_complete);
                else
                    check_button.setText(R.string.check_answer);
                selectedOptionByUser = option4.getText().toString();
                if (testsMode != 3) {
                    if (!def_pref.getBoolean("theme_switch_preference", false))
                        option4.setBackgroundResource(R.drawable.round_back_red10);
                    else
                        option4.setBackgroundResource(R.drawable.round_back_red10_dark_theme);
                    option4.setTextColor(Color.WHITE);
                    revealAnswer();
                } else {
                    if (!def_pref.getBoolean("theme_switch_preference", false))
                        option4.setBackgroundResource(R.drawable.round_back_grey10);
                    else
                        option4.setBackgroundResource(R.drawable.round_back_grey10_dark_theme);
                }
                questionList.get(currentQuestionPosition).setUserSelectedOption(selectedOptionByUser);
            } else { //В экзаменационном режиме можно перевыбирать ответ
                if (testsMode == 3 && iQuestionOrder != questionList.size()) {
                    if (!def_pref.getBoolean("theme_switch_preference", false)) {
                        option1.setBackgroundResource(R.drawable.round_back_white_10);
                        option2.setBackgroundResource(R.drawable.round_back_white_10);
                        option3.setBackgroundResource(R.drawable.round_back_white_10);
                        option4.setBackgroundResource(R.drawable.round_back_grey10);
                    } else {
                        option1.setBackgroundResource(R.drawable.round_back_dark_10);
                        option2.setBackgroundResource(R.drawable.round_back_dark_10);
                        option3.setBackgroundResource(R.drawable.round_back_dark_10);
                        option4.setBackgroundResource(R.drawable.round_back_grey10_dark_theme);
                    }
                    selectedOptionByUser = option4.getText().toString();
                    questionList.get(currentQuestionPosition).setUserSelectedOption(selectedOptionByUser);
                }
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


    private int getCorrectAnswers() {
        int correctAnswers = 0;
        for (int i = 0; i < questionList.size(); i++) {
            final String getUserSelectedAnswer = questionList.get(i).getUserSelectedOption();
            final String getAnswer = questionList.get(i).getAnswer();

            if (getUserSelectedAnswer.equals(getAnswer))
                correctAnswers++;
        }

        if (testsMode == 3) {
            for (int i = 0; i < questionList.size(); i++) {
                //final String userSelectedAnswer = questionList.get(questionOrder[i]).getUserSelectedOption();
                //final String answer = questionList.get(questionOrder[i]).getAnswer();
                //if (!userSelectedAnswer.equals(answer)) {
                viewMistakesQuestionNumberList.add(i + 1);
                viewMistakesQuestionList.add(questionList.get(questionOrder[i]).getQuestion());
                viewMistakesCorrectAnswerList.add(questionList.get(questionOrder[i]).getAnswer());
                viewMistakesUserAnswerList.add(questionList.get(questionOrder[i]).getUserSelectedOption());
                //}
            }
        }

        String questionsAnsweredKey = "QuestionsAnswered";
        StatisticsSave(questionsAnsweredKey, /*questionsAnsweredCount*/ pref.getInt(questionsAnsweredKey, 0) + questionList.size());
        String rightAnswersKey = "RightAnswers";
        StatisticsSave(rightAnswersKey, /*rightAnswersCount*/ pref.getInt(rightAnswersKey, 0) + correctAnswers);
        if (correctAnswers == questionList.size())
            StatisticsSave(counter10sKey, pref.getInt(counter10sKey, 0) + 1);
        return correctAnswers;
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


    private void showResults() {
        if (testsMode == 1)
            StatisticsSave(ttCounterKey, pref.getInt(ttCounterKey, 0) + 1);
        else if (testsMode == 2)
            StatisticsSave(ttAllTopicsCounterKey, pref.getInt(ttAllTopicsCounterKey, 0) + 1);
        else if (testsMode == 3)
            StatisticsSave(examCounterKey, pref.getInt(examCounterKey, 0) + 1);

        String testsCompletedKey = "TestsCompleted";
        StatisticsSave(testsCompletedKey, /*++testsCompletedCount*/ pref.getInt(testsCompletedKey, 0) + 1);
        pauseChronometer();
        String timeInTestsKey = "TimeInTests";
        StatisticsSaveLong(timeInTestsKey, /*timeInTestsCount*/ pref.getLong(timeInTestsKey, 0) + (System.currentTimeMillis() - sessionTimeStart));
        //StatisticsSaveLong(timeInTestsKey, 0);
        Intent intent = new Intent(TopicTrainingActivity.this, TopicTrainingQuizResultsActivity.class);
        intent.putExtra("chronometer", String.valueOf(chronometer.getText()));
        intent.putExtra("correct", getCorrectAnswers());
        intent.putExtra("all_questions", questionList.size());
        intent.putExtra("spinnerChoice", spinnerChoice);
        intent.putExtra("spinnerChoiceAllTopics", spinnerChoiceAllTopics);
        intent.putExtra("testsMode", testsMode);
        if (testsMode == 3) {
            intent.putExtra("viewMistakesQuestionNumberList", (Serializable) viewMistakesQuestionNumberList);
            intent.putExtra("viewMistakesQuestionList", (Serializable) viewMistakesQuestionList); //Serializable под вопросом, без него ошибка, может List передается не так
            intent.putExtra("viewMistakesUserAnswerList", (Serializable) viewMistakesUserAnswerList);
            intent.putExtra("viewMistakesCorrectAnswerList", (Serializable) viewMistakesCorrectAnswerList);
        }
        startActivity(intent);
        resetChronometer();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timeInAppKey = "TimeInApp";
        StatisticsSaveLong(timeInAppKey, pref.getLong(timeInAppKey, 0) + (System.currentTimeMillis() - appSessionStartTTA));

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


    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    private void questionChange() {
        //currentQuestionPosition++;
        //progressBar.animate()
        if (iQuestionOrder + 1 != questionList.size())
            currentQuestionPosition = questionOrder[++iQuestionOrder];
        else
            iQuestionOrder++;

        if (Build.VERSION.SDK_INT >= 24)
            progressBar.setProgress((int) ((float) iQuestionOrder / questionList.size() * 100), true);
        else
            progressBar.setProgress((int) ((float) iQuestionOrder / questionList.size() * 100));

        if (testsMode != 3) {
            if (questionList.get(questionOrder[iQuestionOrder - 1]).getAnswer().equals(questionList.get(questionOrder[iQuestionOrder - 1]).getUserSelectedOption()))
                progressBar.setProgressDrawable(getDrawable(R.drawable.custom_progress_bar));
            else
                progressBar.setProgressDrawable(getDrawable(R.drawable.custom_progress_bar_red));
        }

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
            question_number.setText((iQuestionOrder + 1) + "/" + questionList.size());
            question.setText(questionList.get(currentQuestionPosition).getQuestion());
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

    @Override
    protected void onResume() {
        super.onResume();
        appSessionStartTTA = System.currentTimeMillis();
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