package com.eduscope.eduscope;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.eduscope.eduscope.internet_connection.Common;
import com.eduscope.eduscope.login.LoginActivity;
import com.eduscope.eduscope.notes.CreateNoteActivity;
import com.eduscope.eduscope.notes.EditNoteActivity;
import com.eduscope.eduscope.notes.FirebaseModel;
import com.eduscope.eduscope.notes.NoteViewHolder;
import com.eduscope.eduscope.settings.SettingsActivity;
import com.eduscope.eduscope.sqlite.SQLiteHelper;
import com.eduscope.eduscope.tests.GuessByImageActivity;
import com.eduscope.eduscope.tests.SurvivalActivity;
import com.eduscope.eduscope.tests.TopicTrainingActivity;
import com.eduscope.eduscope.tests.TrueOrFalseActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {

    private TextView notesMessage;
    private FirebaseUser user;
    private LinearLayout topic_training, all_topics_training, exam, true_or_false, guess_by_image, survival;
    private Button startQuiz;
    private boolean wasStatisticsOpen = false;
    private String[] leftArrGlobal, middleArrGlobal, rightArrGlobal;
    private long[] rightChronometerArrGlobal;
    private boolean statisticsDarkTheme = false;
    private int statisticsTextSize = 18;
    private final long[] chronometerArr = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    private boolean startStopChronometer = true;
    private String statisticsFavMode;
    private CircularProgressBar circularProgressBar, circularProgressBar2;
    private TextView mark;
    private ConstraintLayout mainConstraintLayout;
    private AppCompatButton updateFactButton;
    private String animPreference;
    private int wasAppOpen = 0; //чтобы анимация тестов срабатывала только первый раз после захода в приложение, int потому что после изменения настроек в значении
    //boolean один раз все равно анимация срабатывала (режим один раз при открытии приложения)
    private ScrollView scrollViewTests;
    //Statistics:
    private boolean isChronometerRunning = false;
    //private boolean isAppHidden = true; //чтобы при сворачивании счет времени останавливался, а при переходе в другое активити - нет
    //private Chronometer chronometerTimeInApp;
    private long appSessionStart;
    public SharedPreferences pref;
    private final String testsCompletedKey = "TestsCompleted", rightAnswersKey = "RightAnswers", questionsAnsweredKey = "QuestionsAnswered",
            timeInTestsKey = "TimeInTests", timeInAppKey = "TimeInApp", survivalHighestKey = "HighestSurvivalScore", ttCounterKey = "TTCounter",
            ttAllTopicsCounterKey = "TTAllTopicsCounter", trueOrFalseCounterKey = "TrueOrFalseCounter", examCounterKey = "ExamCounter", guessByImageCounterKey = "GuessByImageCounter", //каунтеры для определения любимого режима
            survivalCounterKey = "SurvivalCounter", counter10sKey = "10sCounter";
    private ScrollView statisticsScrollView;

    private String text_size_str_was;
    private boolean wasListItemPressed;

    private int testsMode; //Чтобы использовать одно активити для topic training и all topics training. 1 означает, что TT, 2 - All, 3 - exam

    private boolean wasStartQuizPressed; //Чтобы не было двойных нажатий на старт квиза
    private boolean wasSwitch;
    private int ONIS = 1; //OnNavigationItemSelected
    private Spinner spinner, spinnerAllTopics;
    private String spinnerChoice;
    private boolean is_topic_training_pressed = false, is_all_topics_training_pressed = false,
            is_true_or_false_pressed = false, is_exam_pressed = false, is_guess_by_image_pressed = false, is_survival_pressed = false;
    private String selectedTestsMode = "";
    private int category_index;
    private FrameLayout fragment_container_tests;
    private FrameLayout mainActivityFrameLayout;
    private NavigationView navView;
    private SharedPreferences defPref;
    private DrawerLayout drawer;
    private ListView list, listPh, statisticsLV;
    private String[] array;
    private ArrayAdapter<String> adapter;
    private customStatisticsBaseAdapter statisticsAdapter;
    private Toolbar toolbar;
    private TextView activityMainTV; // for the random fact section
    private ArrayList<String> randFactContents = new ArrayList<>();
    private ArrayList<byte[]> randFactImages = new ArrayList<>();
    private TextView activityMainRandomFact;
    private ImageView randfactImage;
    private int randindex;
    private boolean loggedInManually = false;
    private String statisticsGpa;
    private int idVar;
    private LinearLayout statisticsLinearLayout;
    private ProgressBar loadingStatistics;
    private boolean statisticsUploaded;
    private final String statisticsUploadedKey = "statisticsUploaded";
    private Timer timer;
    private TimerTask timerTask;
    private ImageView noWifiImage;
    private FloatingActionButton createNoteBtn;
    private FrameLayout notesSection;
    private FirestoreRecyclerAdapter<FirebaseModel, NoteViewHolder> noteAdapter;
    private DocumentReference docId;
    private String docIdStr;
    private ImageView notesViewMode;
    private float previousGPA = 0;
    private Query query;
    private RecyclerView notesRecyclerView;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private final String notesViewModeKey = "NotesViewMode";
    private AppCompatButton sortButton;
    private byte loggingOut = 0;
    private String notesSearchStr = "";
    private SearchView searchView;
    private SQLiteHelper sqLiteHelper = new SQLiteHelper(MainActivity.this);
    private String subject;
    private Map<byte[], String> randFactMap = new HashMap<>();


    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    //activity_main_TextView is created for displaying a random fact, that way we don't have to open another activity, app's gon be smooth
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appSessionStart = System.currentTimeMillis();


        notesViewMode = findViewById(R.id.notes_view_mode);
        pref = getSharedPreferences("Statistics", Context.MODE_PRIVATE);

        loadingStatistics = findViewById(R.id.loading_progress_bar);
        statisticsLinearLayout = findViewById(R.id.statisticsLinearLayout);

        idVar = R.id.nav_home;
        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderTV = headerView.findViewById(R.id.nav_header_textview);

        firestore.collection("Users").document(FirebaseAuth.getInstance().getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                try {
                    navHeaderTV.setText(document.get("name") + " " + document.get("aftername"));
                } catch (Exception ex) {
                    navHeaderTV.setText(getString(R.string.app_name));
                }
            }
        });

        Bundle extras = getIntent().getExtras();
        subject = extras.getString("subject");
        Log.d("subjectWhich", subject);
        loggedInManually = extras.getBoolean("loggedInManually");
        if (loggedInManually && !pref.getBoolean(statisticsUploadedKey, false)) {
            try {
                firestore.collection("Users").document(FirebaseAuth.getInstance().getUid()).get().addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    loadingStatistics.setVisibility(View.VISIBLE);
                    statisticsLinearLayout.setVisibility(View.GONE);
                    //try {
                    StatisticsSave(questionsAnsweredKey, (int) (long) document.get("questionsAnswered"));
                    StatisticsSave(rightAnswersKey, (int) (long) document.get("rightAnswers"));
                    StatisticsSave(testsCompletedKey, (int) (long) document.get("testsCompleted"));
                    StatisticsSave(ttCounterKey, (int) (long) document.get("ttCounter"));
                    StatisticsSave(ttAllTopicsCounterKey, (int) (long) document.get("ttAllTopicsCounter"));
                    StatisticsSave(trueOrFalseCounterKey, (int) (long) document.get("trueOrFalseCounter"));
                    StatisticsSave(examCounterKey, (int) (long) document.get("examCounter"));
                    StatisticsSave(guessByImageCounterKey, (int) (long) document.get("guessByImageCounter"));
                    StatisticsSave(survivalCounterKey, (int) (long) document.get("survivalCounter"));
                    StatisticsSave(survivalHighestKey, (int) (long) document.get("survivalHighest"));
                    StatisticsSave(counter10sKey, (int) (long) document.get("counter10s"));
                    StatisticsSaveLong(timeInTestsKey, (int) (long) document.get("timeInTests"));
                    StatisticsSaveLong(timeInAppKey, (int) (long) document.get("timeInApp"));
                    //}
                    //catch (NullPointerException ex) {
                    //Add statistics upload error message
                    //}
                    //Statistics page update:
                    loadingStatistics.setVisibility(View.GONE);
                    statisticsLinearLayout.setVisibility(View.VISIBLE);
                    if (idVar == R.id.nav_statistics) {
                        NavStatistics();
                    } else
                        StatisticsSaveBoolean(statisticsUploadedKey, statisticsUploaded);
                });
            } catch (RuntimeExecutionException ex) {
                ex.printStackTrace();
            }
        }

        scrollViewTests = findViewById(R.id.scrollViewTests);
        mark = findViewById(R.id.mark);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        circularProgressBar2 = findViewById(R.id.circularProgressBar2);

        statisticsScrollView = findViewById(R.id.statisticsScrollView);
        statisticsScrollView.setVisibility(View.GONE);

        mainConstraintLayout = findViewById(R.id.main_constraint_layout);

        updateFactButton = findViewById(R.id.update_fact_button);


        //Tests:
        topic_training = findViewById(R.id.topic_training_layout);
        all_topics_training = findViewById(R.id.all_topics_training_layout);
        true_or_false = findViewById(R.id.true_or_false_layout);
        exam = findViewById(R.id.exam_layout);
        guess_by_image = findViewById(R.id.guess_by_image_layout);
        survival = findViewById(R.id.survival_layout);
        startQuiz = findViewById(R.id.start_quiz_button); //start button

        //Spinner:
        spinner = findViewById(R.id.spinner);
        spinnerAllTopics = findViewById(R.id.spinner_all_topics);
        spinner.setVisibility(View.INVISIBLE);
        spinnerAllTopics.setVisibility(View.GONE);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, sqLiteHelper.selectSummaryTitles(subject));
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        int ATTRowCount = sqLiteHelper.countAllTopicsTrainingQuestionSets(subject);
        String[] allTopicsTrainingTitleArray = new String[ATTRowCount];
        for (int i = 0; i < ATTRowCount; i++)
            allTopicsTrainingTitleArray[i] = getString(R.string.ticket) + " " + (i + 1);
        ArrayAdapter<String> spinnerAdapterAllTopics = new ArrayAdapter<>(this, R.layout.simple_spinner_item, allTopicsTrainingTitleArray);
        spinnerAdapterAllTopics.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinnerAllTopics.setAdapter(spinnerAdapterAllTopics);
        spinner.setOnItemSelectedListener(this);
//Оптимизация?
        topic_training.setOnClickListener(view -> {
            if (!is_topic_training_pressed) {
                testsMode = 1;
                spinner.setVisibility(View.VISIBLE);
                is_topic_training_pressed = true;
                is_all_topics_training_pressed = false;
                is_true_or_false_pressed = false;
                is_exam_pressed = false;
                is_guess_by_image_pressed = false;
                is_survival_pressed = false;

                startQuiz.setText(R.string.start_button);
                selectedTestsMode = "topic_training";
                if (!defPref.getBoolean("theme_switch_preference", false)) {
                    topic_training.setBackgroundResource(R.drawable.round_back_white_stroke10);
                    exam.setBackgroundResource(R.drawable.round_back_white_10);
                    true_or_false.setBackgroundResource(R.drawable.round_back_white_10);
                    all_topics_training.setBackgroundResource(R.drawable.round_back_white_10);
                    guess_by_image.setBackgroundResource(R.drawable.round_back_white_10);
                    survival.setBackgroundResource(R.drawable.round_back_white_10);
                } else {
                    topic_training.setBackgroundResource(R.drawable.round_back_dark_stroke_10);
                    exam.setBackgroundResource(R.drawable.round_back_dark_10);
                    true_or_false.setBackgroundResource(R.drawable.round_back_dark_10);
                    all_topics_training.setBackgroundResource(R.drawable.round_back_dark_10);
                    guess_by_image.setBackgroundResource(R.drawable.round_back_dark_10);
                    survival.setBackgroundResource(R.drawable.round_back_dark_10);
                }
                spinnerAllTopics.setVisibility(View.INVISIBLE);
            } else {
                is_topic_training_pressed = false;
                selectedTestsMode = "";
                if (!defPref.getBoolean("theme_switch_preference", false))
                    topic_training.setBackgroundResource(R.drawable.round_back_white_10);
                else
                    topic_training.setBackgroundResource(R.drawable.round_back_dark_10);
                spinner.setVisibility(View.INVISIBLE);
            }
        });

        notesMessage = findViewById(R.id.notes_message);

        all_topics_training.setOnClickListener(view -> {
            if (!is_all_topics_training_pressed) {
                testsMode = 2;
                spinnerAllTopics.setVisibility(View.VISIBLE);
                is_all_topics_training_pressed = true;
                is_topic_training_pressed = false;
                is_true_or_false_pressed = false;
                is_exam_pressed = false;
                is_guess_by_image_pressed = false;
                is_survival_pressed = false;
                startQuiz.setText(R.string.start_button);
                selectedTestsMode = "all_topics_training";
                if (!defPref.getBoolean("theme_switch_preference", false)) {
                    all_topics_training.setBackgroundResource(R.drawable.round_back_white_stroke10);
                    exam.setBackgroundResource(R.drawable.round_back_white_10);
                    topic_training.setBackgroundResource(R.drawable.round_back_white_10);
                    true_or_false.setBackgroundResource(R.drawable.round_back_white_10);
                    guess_by_image.setBackgroundResource(R.drawable.round_back_white_10);
                    survival.setBackgroundResource(R.drawable.round_back_white_10);
                } else {
                    all_topics_training.setBackgroundResource(R.drawable.round_back_dark_stroke_10);
                    exam.setBackgroundResource(R.drawable.round_back_dark_10);
                    topic_training.setBackgroundResource(R.drawable.round_back_dark_10);
                    true_or_false.setBackgroundResource(R.drawable.round_back_dark_10);
                    guess_by_image.setBackgroundResource(R.drawable.round_back_dark_10);
                    survival.setBackgroundResource(R.drawable.round_back_dark_10);
                }
                spinner.setVisibility(View.INVISIBLE);
            } else {
                is_all_topics_training_pressed = false;
                selectedTestsMode = "";
                if (!defPref.getBoolean("theme_switch_preference", false))
                    all_topics_training.setBackgroundResource(R.drawable.round_back_white_10);
                else
                    all_topics_training.setBackgroundResource(R.drawable.round_back_dark_10);
                spinnerAllTopics.setVisibility(View.INVISIBLE);
            }
        });

        true_or_false.setOnClickListener(view -> {
            if (!is_true_or_false_pressed) {
                testsMode = 4;
                is_true_or_false_pressed = true;
                is_topic_training_pressed = false;
                is_all_topics_training_pressed = false;
                is_guess_by_image_pressed = false;
                is_exam_pressed = false;
                is_survival_pressed = false;
                startQuiz.setText(R.string.start_button);
                selectedTestsMode = "true_or_false";
                if (!defPref.getBoolean("theme_switch_preference", false)) {
                    true_or_false.setBackgroundResource(R.drawable.round_back_white_stroke10);
                    exam.setBackgroundResource(R.drawable.round_back_white_10);
                    topic_training.setBackgroundResource(R.drawable.round_back_white_10);
                    all_topics_training.setBackgroundResource(R.drawable.round_back_white_10);
                    guess_by_image.setBackgroundResource(R.drawable.round_back_white_10);
                    survival.setBackgroundResource(R.drawable.round_back_white_10);
                } else {
                    true_or_false.setBackgroundResource(R.drawable.round_back_dark_stroke_10);
                    exam.setBackgroundResource(R.drawable.round_back_dark_10);
                    topic_training.setBackgroundResource(R.drawable.round_back_dark_10);
                    all_topics_training.setBackgroundResource(R.drawable.round_back_dark_10);
                    guess_by_image.setBackgroundResource(R.drawable.round_back_dark_10);
                    survival.setBackgroundResource(R.drawable.round_back_dark_10);
                }
                spinner.setVisibility(View.INVISIBLE);
                spinnerAllTopics.setVisibility(View.INVISIBLE);
            } else {
                is_true_or_false_pressed = false;
                selectedTestsMode = "";
                if (!defPref.getBoolean("theme_switch_preference", false))
                    true_or_false.setBackgroundResource(R.drawable.round_back_white_10);
                else
                    true_or_false.setBackgroundResource(R.drawable.round_back_dark_10);
            }
        });

        exam.setOnClickListener(view -> {
            if (!is_exam_pressed) {
                testsMode = 3;
                spinnerAllTopics.setVisibility(View.VISIBLE);
                is_exam_pressed = true;
                is_topic_training_pressed = false;
                is_all_topics_training_pressed = false;
                is_true_or_false_pressed = false;
                is_guess_by_image_pressed = false;
                is_survival_pressed = false;
                startQuiz.setText(R.string.start_button);
                selectedTestsMode = "exam";
                if (!defPref.getBoolean("theme_switch_preference", false)) {
                    exam.setBackgroundResource(R.drawable.round_back_white_stroke10);
                    true_or_false.setBackgroundResource(R.drawable.round_back_white_10);
                    topic_training.setBackgroundResource(R.drawable.round_back_white_10);
                    all_topics_training.setBackgroundResource(R.drawable.round_back_white_10);
                    guess_by_image.setBackgroundResource(R.drawable.round_back_white_10);
                    survival.setBackgroundResource(R.drawable.round_back_white_10);
                } else {
                    exam.setBackgroundResource(R.drawable.round_back_dark_stroke_10);
                    true_or_false.setBackgroundResource(R.drawable.round_back_dark_10);
                    topic_training.setBackgroundResource(R.drawable.round_back_dark_10);
                    all_topics_training.setBackgroundResource(R.drawable.round_back_dark_10);
                    guess_by_image.setBackgroundResource(R.drawable.round_back_dark_10);
                    survival.setBackgroundResource(R.drawable.round_back_dark_10);
                }
                spinner.setVisibility(View.INVISIBLE);
            } else {
                is_exam_pressed = false;
                selectedTestsMode = "";
                if (!defPref.getBoolean("theme_switch_preference", false))
                    exam.setBackgroundResource(R.drawable.round_back_white_10);
                else
                    exam.setBackgroundResource(R.drawable.round_back_dark_10);
                spinnerAllTopics.setVisibility(View.INVISIBLE);
            }
        });

        guess_by_image.setOnClickListener(view -> {
            if (subject.equals("Philosophy")) {
                if (!is_guess_by_image_pressed) {
                    testsMode = 5;
                    spinner.setVisibility(View.INVISIBLE);
                    spinnerAllTopics.setVisibility(View.INVISIBLE);
                    is_guess_by_image_pressed = true;
                    is_topic_training_pressed = false;
                    is_all_topics_training_pressed = false;
                    is_true_or_false_pressed = false;
                    is_exam_pressed = false;
                    is_survival_pressed = false;
                    startQuiz.setText(R.string.start_button);
                    selectedTestsMode = "guess_by_image";
                    if (!defPref.getBoolean("theme_switch_preference", false)) {
                        guess_by_image.setBackgroundResource(R.drawable.round_back_white_stroke10);
                        exam.setBackgroundResource(R.drawable.round_back_white_10);
                        true_or_false.setBackgroundResource(R.drawable.round_back_white_10);
                        topic_training.setBackgroundResource(R.drawable.round_back_white_10);
                        all_topics_training.setBackgroundResource(R.drawable.round_back_white_10);
                        survival.setBackgroundResource(R.drawable.round_back_white_10);
                    } else {
                        guess_by_image.setBackgroundResource(R.drawable.round_back_dark_stroke_10);
                        exam.setBackgroundResource(R.drawable.round_back_dark_10);
                        true_or_false.setBackgroundResource(R.drawable.round_back_dark_10);
                        topic_training.setBackgroundResource(R.drawable.round_back_dark_10);
                        all_topics_training.setBackgroundResource(R.drawable.round_back_dark_10);
                        survival.setBackgroundResource(R.drawable.round_back_dark_10);
                    }
                } else {
                    is_guess_by_image_pressed = false;
                    selectedTestsMode = "";
                    if (!defPref.getBoolean("theme_switch_preference", false))
                        guess_by_image.setBackgroundResource(R.drawable.round_back_white_10);
                    else
                        guess_by_image.setBackgroundResource(R.drawable.round_back_dark_10);
                }
            } else
                Toast.makeText(this, getString(R.string.mode_unavailable), Toast.LENGTH_SHORT).show();
        });

        survival.setOnClickListener(view -> {
            if (!is_survival_pressed) {
                testsMode = 6;
                spinner.setVisibility(View.INVISIBLE);
                spinnerAllTopics.setVisibility(View.INVISIBLE);
                is_survival_pressed = true;
                is_guess_by_image_pressed = false;
                is_topic_training_pressed = false;
                is_all_topics_training_pressed = false;
                is_true_or_false_pressed = false;
                is_exam_pressed = false;
                startQuiz.setText(R.string.start_button);
                selectedTestsMode = "survival";
                if (!defPref.getBoolean("theme_switch_preference", false)) {
                    survival.setBackgroundResource(R.drawable.round_back_white_stroke10);
                    exam.setBackgroundResource(R.drawable.round_back_white_10);
                    true_or_false.setBackgroundResource(R.drawable.round_back_white_10);
                    topic_training.setBackgroundResource(R.drawable.round_back_white_10);
                    all_topics_training.setBackgroundResource(R.drawable.round_back_white_10);
                    guess_by_image.setBackgroundResource(R.drawable.round_back_white_10);
                } else {
                    survival.setBackgroundResource(R.drawable.round_back_dark_stroke_10);
                    exam.setBackgroundResource(R.drawable.round_back_dark_10);
                    true_or_false.setBackgroundResource(R.drawable.round_back_dark_10);
                    topic_training.setBackgroundResource(R.drawable.round_back_dark_10);
                    all_topics_training.setBackgroundResource(R.drawable.round_back_dark_10);
                    guess_by_image.setBackgroundResource(R.drawable.round_back_dark_10);
                }
            } else {
                is_survival_pressed = false;
                selectedTestsMode = "";
                if (!defPref.getBoolean("theme_switch_preference", false))
                    survival.setBackgroundResource(R.drawable.round_back_white_10);
                else
                    survival.setBackgroundResource(R.drawable.round_back_dark_10);
            }
        });


        startQuiz.setOnClickListener(view -> {
            if (!wasStartQuizPressed) {
                switch (selectedTestsMode) {
                    case "":
                        //сделать анимацию, надпись на кнопке съезжает вверх и сменяется другой "Не выбран режим", может другого цвета
                        startQuiz.setText(R.string.mode_not_chosen);
                        //Toast.makeText(MainActivity.this, "Не выбран режим", Toast.LENGTH_SHORT).show();
                        break;
                    case "topic_training":
                    case "all_topics_training":
                    case "exam":
                        IntentTests();
                        //isAppHidden = false;
                        break;
                    //isAppHidden = false;
                    case "true_or_false":
                        Intent intentTOF = new Intent(MainActivity.this, TrueOrFalseActivity.class);
                        intentTOF.putExtra("testsMode", testsMode);
                        intentTOF.putExtra("subject", subject);
                        startActivity(intentTOF);
                        //isAppHidden = false;
                        break;
                    case "guess_by_image":
                        Intent intentGBI = new Intent(MainActivity.this, GuessByImageActivity.class);
                        intentGBI.putExtra("testsMode", testsMode);
                        intentGBI.putExtra("subject", subject);
                        startActivity(intentGBI);
                        break;
                    case "survival":
                        Intent intentSurvival = new Intent(MainActivity.this, SurvivalActivity.class);
                        intentSurvival.putExtra("testsMode", testsMode);
                        intentSurvival.putExtra("subject", subject);
                        startActivity(intentSurvival);
                        break;

                }
            }
            wasStartQuizPressed = !selectedTestsMode.equals(""); //вместо if else, метод equals возвращает true или false
        });

        //

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.home_toolbar_title);
        setSupportActionBar(toolbar);


        randfactImage = findViewById(R.id.activity_main_RandomFact_Image);
        activityMainTV = findViewById(R.id.activity_main_TextView);
        activityMainRandomFact = findViewById(R.id.activity_main_RandomFact);
        mainActivityFrameLayout = findViewById(R.id.fragment_container);
        navView = findViewById(R.id.nav_view);
        if (!subject.equals("Philosophy")) {
            navView.getMenu().clear();
            navView.inflateMenu(R.menu.drawer_menu_no_scientists);
        }

        //написать функцию выбора случайного факта из activity_main_TV. Соответственно, сделать activity_main_TV массивом.
        //Предусмотреть переменную для запоминания выпавшего факта после открытия приложения для его сохранения при переключении
        //между разделами. Очищать TextView при выходе из раздела Main, предусмотреть синхронизированную работу main при выборе его в меню
        //и main при открытии приложения.
        activityMainRandomFact.setText(R.string.random_fact);


        randFactMap = sqLiteHelper.selectRandomFact(subject);
        Set<Map.Entry<byte[], String>> entrySetRandFact = randFactMap.entrySet();
        Map.Entry<byte[], String>[] entryArrayRandFact = entrySetRandFact.toArray(new Map.Entry[entrySetRandFact.size()]);

        for (int i = 0; i < entryArrayRandFact.length; i++) {
            randFactImages.add(entryArrayRandFact[i].getKey());
            randFactContents.add(entryArrayRandFact[i].getValue());
        }
        ArrayList<Bitmap> randFactImagesBitmap = new ArrayList<>();
        for (byte[] imageData : randFactImages) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            randFactImagesBitmap.add(bitmap);
        }

        randindex = new Random().nextInt(randFactContents.size());
        activityMainTV.setText(randFactContents.get(randindex));
        randfactImage.setImageBitmap(randFactImagesBitmap.get(randindex));
        fragment_container_tests = findViewById(R.id.fragment_container_tests);
        fragment_container_tests.setVisibility(View.GONE);

        updateFactButton.setOnClickListener(view -> {
            randindex = new Random().nextInt(randFactContents.size());
            activityMainTV.setText(randFactContents.get(randindex));
            randfactImage.setImageBitmap(randFactImagesBitmap.get(randindex));
            fragment_container_tests = findViewById(R.id.fragment_container_tests);
            fragment_container_tests.setVisibility(View.GONE);
        });


        list = findViewById(R.id.activityMainListViewSummary);
        listPh = findViewById(R.id.activityMainListViewPhilosophers);

        array = sqLiteHelper.selectScientistsTitles(subject);
        //array = getResources().getStringArray(R.array.philosophers_array);


        defPref = PreferenceManager.getDefaultSharedPreferences(this);
        //Значение по дефолту, чтобы не крашилось при запуске, для очистки кода от повтора проверки настроек с onResume и запоминания позиции прокрутки списка
        adapter = new ArrayAdapter<>(this, R.layout.arraylist_item_text_color, new ArrayList<>(Arrays.asList(array)));
        list.setAdapter(adapter);
        listPh.setAdapter(adapter);
        list.setVisibility(View.GONE);
        listPh.setVisibility(View.GONE);

        statisticsAdapter = new customStatisticsBaseAdapter(this, null, null, null, null, startStopChronometer, statisticsDarkTheme, statisticsTextSize);
        statisticsLV = findViewById(R.id.statisticsListView);

        drawer = findViewById(R.id.drawer_layout);
        navView.setNavigationItemSelectedListener(this);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //if (savedInstanceState == null) {
        //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
        //new MainFragment()).commit();
        navView.setCheckedItem(R.id.nav_home);
        // }

        list.setOnItemClickListener((parent, view, position, id) -> {
            if (!wasListItemPressed) {
                Intent intent = new Intent(MainActivity.this, DetailedContentActivity.class);
                intent.putExtra("category", category_index); //для открытия нужного нам контента, присущего только конкретному полю array'я
                intent.putExtra("position", position); //там передаем индекс раздела (home, tests...), здесь индекс конкретного поля
                intent.putExtra("subject", subject);
                startActivity(intent);
                wasListItemPressed = true;
            }
        });

        //второй лист нужен, чтобы позиция не сохранялась (раньше при прокручивание скролла в одном разделе прокручивался список и в другом)
        listPh.setOnItemClickListener((parent, view, position, id) -> {
            if (!wasListItemPressed) {
                Intent intent = new Intent(MainActivity.this, DetailedContentActivity.class);
                intent.putExtra("category", category_index); //для открытия нужного нам контента, присущего только конкретному полю array'я
                intent.putExtra("position", position); //там передаем индекс раздела (home, tests...), здесь индекс конкретного поля
                intent.putExtra("subject", subject);
                startActivity(intent);
                wasListItemPressed = true;
            }
        });

        noWifiImage = findViewById(R.id.no_wifi_image_toolbar);

        createNoteBtn = findViewById(R.id.create_note_floating_button);
        Intent createNoteIntent = new Intent(MainActivity.this, CreateNoteActivity.class);
        createNoteBtn.setOnClickListener(view -> {
            searchView.clearFocus();
            startActivity(createNoteIntent);
        });
        notesSection = findViewById(R.id.notes_section);
        sortButton = findViewById(R.id.sort_button); //Sort Button
        searchView = findViewById(R.id.search_view);
    }


    @SuppressLint("DefaultLocale")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.nav_home) { //если использовать switch, то ругается, что Resource IDs will be non-final in Gradle 8.0
            notesViewMode.setVisibility(View.GONE);
            notesSection.setVisibility(View.GONE);
            idVar = R.id.nav_home;
            statisticsLV.setVisibility(View.GONE);
            mainConstraintLayout.setVisibility(View.VISIBLE);
            statisticsScrollView.setVisibility(View.GONE);
            ONIS = 1;
            list.setVisibility(View.GONE);
            listPh.setVisibility(View.GONE);
            fragment_container_tests.setVisibility(View.GONE);
            toolbar.setTitle(R.string.home_toolbar_title);
            adapter.clear();
            //activity_main_TV.setText(...some R.string...>); //разработанную функцию для рандом факта выведем сначала
            //в onCreate, потом запомним инфу и выведем ее уже определенную здесь, чтобы рандомный факт генерировался при запуске
            //приложения и оставался не изменным после переключения между разделами, пока приложение не закрыто
            activityMainRandomFact.setText(R.string.random_fact);
            activityMainTV.setText(randFactContents.get(randindex)); //но здесь должна будет быть переменная для запоминания уже
            //сгенерированного факта
            category_index = 0;
        } else if (id == R.id.nav_tests) {
            notesViewMode.setVisibility(View.GONE);
            notesSection.setVisibility(View.GONE);
            idVar = R.id.nav_tests;
            statisticsLV.setVisibility(View.GONE);
            mainConstraintLayout.setVisibility(View.GONE);
            wasAppOpen++;
            scrollViewTests.scrollBy(0, -500);
            statisticsScrollView.setVisibility(View.GONE);
            ONIS = 2;
            fragment_container_tests.setVisibility(View.VISIBLE);
            //main_activity_frame_layout.setBackgroundColor(getResources().getColor(R.color.BSUIR_Blue));
            toolbar.setTitle(R.string.tests_toolbar_title);
            adapter.clear();
            category_index = 1;
            //Анимация прокрутки ScorllView, чтобы намекнуть пользователю о наличии других режимов
            if (animPreference.equals("Один раз после открытия приложения")) { //закинуть в ресурсы
                if (wasAppOpen == 1) {//добавить возможность отключения в настройках
                    TranslateAnimation anim = new TranslateAnimation(0, 0, -500, 0);
                    anim.setDuration(400);
                    FrameLayout frameLayoutUnderAnimation = findViewById(R.id.frame_layout_under_animation);
                    frameLayoutUnderAnimation.startAnimation(anim);
                    wasAppOpen++;
                }
            } else if (animPreference.equals("При каждом заходе в тесты")) { //закинуть в ресурсы
                TranslateAnimation anim = new TranslateAnimation(0, 0, -500, 0);
                anim.setDuration(400);
                FrameLayout frameLayoutUnderAnimation = findViewById(R.id.frame_layout_under_animation);
                frameLayoutUnderAnimation.startAnimation(anim);
            }
        } else if (id == R.id.nav_summary) {
            notesViewMode.setVisibility(View.GONE);
            notesSection.setVisibility(View.GONE);
            idVar = R.id.nav_summary;
            statisticsLV.setVisibility(View.GONE);
            mainConstraintLayout.setVisibility(View.GONE);
            statisticsScrollView.setVisibility(View.GONE);
            ONIS = 3;
            fragment_container_tests.setVisibility(View.GONE);
            listPh.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            toolbar.setTitle(R.string.summary_toolbar_title);
            adapter.clear();
            array = sqLiteHelper.selectSummaryTitles(subject);
            //array = getResources().getStringArray(R.array.summary_array);
            adapter.addAll(array);
            adapter.notifyDataSetChanged();
            category_index = 2;
        } else if (id == R.id.nav_philosophers) {
            notesViewMode.setVisibility(View.GONE);
            notesSection.setVisibility(View.GONE);
            idVar = R.id.nav_philosophers;
            statisticsLV.setVisibility(View.GONE);
            mainConstraintLayout.setVisibility(View.GONE);
            statisticsScrollView.setVisibility(View.GONE);
            ONIS = 4;
            fragment_container_tests.setVisibility(View.GONE);
            list.setVisibility(View.GONE);
            listPh.setVisibility(View.VISIBLE);
            toolbar.setTitle(R.string.philosophers_toolbar_title);
            adapter.clear();
            array = sqLiteHelper.selectScientistsTitles(subject);
            //array = getResources().getStringArray(R.array.philosophers_array);
            adapter.addAll(array);
            adapter.notifyDataSetChanged();
            category_index = 3;
        } else if (id == R.id.nav_statistics) {
            notesViewMode.setVisibility(View.GONE);
            notesSection.setVisibility(View.GONE);
            ONIS = 5;
            idVar = R.id.nav_statistics;
            NavStatistics();
            category_index = 4;
        } else if (id == R.id.nav_notes) {
            toolbar.setTitle(R.string.notes_toolbar_title);
            notesViewMode.setVisibility(View.VISIBLE);
            idVar = R.id.nav_notes;
            ONIS = 6; //надо ли оно?
            notesSection.setVisibility(View.VISIBLE);
            notesSection.setOnClickListener(view -> searchView.clearFocus());
            toolbar.setOnClickListener(view -> searchView.clearFocus());
            statisticsLV.setVisibility(View.GONE);
            mainConstraintLayout.setVisibility(View.GONE);
            statisticsScrollView.setVisibility(View.GONE);
            fragment_container_tests.setVisibility(View.GONE);
            list.setVisibility(View.GONE);
            listPh.setVisibility(View.GONE);
            category_index = 5; //надо ли оно?

            if (defPref.getBoolean("notes_when_launched_switch_preference", false))
                StatisticsSaveBoolean("NotesHintHidden", true);
            else {
                if (!pref.getBoolean("NotesHintHidden", false)) {
                    FrameLayout hintLayout = findViewById(R.id.notes_when_launched_hint);
                    hintLayout.setVisibility(View.VISIBLE);
                    ImageView hintCross = findViewById(R.id.notes_when_launched_hint_cross);
                    hintCross.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            hintLayout.setVisibility(View.GONE);
                            StatisticsSaveBoolean("NotesHintHidden", true);
                        }
                    });
                }
            }

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            final String sortButtonModeKey = "SortButtonMode";
            int sortMode = pref.getInt(sortButtonModeKey, 0);
            if (sortMode == 0 || sortMode == 1)
                sortButton.setText(getString(R.string.sort_by_date));
            else if (sortMode == 2 || sortMode == 3)
                sortButton.setText(getString(R.string.sort_by_alphabet));
            switch (sortMode) {
                case 0:
                    query = firestore.collection("Notes").document(user.getUid())
                            .collection("UserNotes").orderBy("date", Query.Direction.DESCENDING);
                    sortButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_down_24, 0);
                    break;
                case 1:
                    query = firestore.collection("Notes").document(user.getUid())
                            .collection("UserNotes").orderBy("date", Query.Direction.ASCENDING);
                    sortButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_up_24, 0);
                    break;
                case 2:
                    query = firestore.collection("Notes").document(user.getUid())
                            .collection("UserNotes").orderBy("title", Query.Direction.ASCENDING);
                    sortButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_up_24, 0);
                    break;
                case 3:
                    query = firestore.collection("Notes").document(user.getUid())
                            .collection("UserNotes").orderBy("title", Query.Direction.DESCENDING);
                    sortButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_down_24, 0);
                    break;
            }

            noteAdapterSetUp();

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    notesSearchStr = newText;
                    noteAdapterSetUp();
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }
            });

            notesViewMode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchView.clearFocus();
                    if (pref.getInt(notesViewModeKey, 0) == 0) {
                        notesViewMode.setImageResource(R.drawable.ic_baseline_view_list_24);
                        staggeredGridLayoutManager.setSpanCount(1);
                        StatisticsSave(notesViewModeKey, 1);
                    } else if (pref.getInt(notesViewModeKey, 0) == 1) {
                        notesViewMode.setImageResource(R.drawable.ic_baseline_grid_view_24);
                        staggeredGridLayoutManager.setSpanCount(2);
                        StatisticsSave(notesViewModeKey, 0);
                    }
                }
            });

            sortButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchView.clearFocus();
                    switch (pref.getInt(sortButtonModeKey, 0)) {
                        case 0:
                            query = firestore.collection("Notes").document(user.getUid())
                                    .collection("UserNotes").orderBy("date", Query.Direction.ASCENDING);
                            sortButton.setText(getString(R.string.sort_by_date));
                            sortButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_up_24, 0);
                            StatisticsSave(sortButtonModeKey, 1);
                            break;
                        case 1:
                            query = firestore.collection("Notes").document(user.getUid())
                                    .collection("UserNotes").orderBy("title", Query.Direction.ASCENDING);
                            sortButton.setText(getString(R.string.sort_by_alphabet));
                            sortButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_up_24, 0);
                            StatisticsSave(sortButtonModeKey, 2);
                            break;
                        case 2:
                            query = firestore.collection("Notes").document(user.getUid())
                                    .collection("UserNotes").orderBy("title", Query.Direction.DESCENDING);
                            sortButton.setText(getString(R.string.sort_by_alphabet));
                            sortButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_down_24, 0);
                            StatisticsSave(sortButtonModeKey, 3);
                            break;
                        case 3:
                            query = firestore.collection("Notes").document(user.getUid())
                                    .collection("UserNotes").orderBy("date", Query.Direction.DESCENDING);
                            sortButton.setText(getString(R.string.sort_by_date));
                            sortButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_down_24, 0);
                            StatisticsSave(sortButtonModeKey, 0);
                            break;
                    }
                    noteAdapterSetUp();
                }
            });

        } else if (id == R.id.nav_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.are_you_sure_logout))
                    .setCancelable(true)
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        statisticsSaveThread thread = new statisticsSaveThread();
                        thread.start();
                        loggingOut++;
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    })
                    .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel()).show();
        } else if (id == R.id.nav_subject_select) {
            Intent subjectSelectIntent = new Intent(MainActivity.this, SubjectSelect.class);
            subjectSelectIntent.putExtra("loggedInManually", loggedInManually);
            startActivity(subjectSelectIntent);
            finish();
        }

        if (id != R.id.nav_logout && id != R.id.nav_subject_select)
            drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void noteAdapterSetUp() {
        query.addSnapshotListener((value, error) -> {
            TextView notesDisplayedHere = findViewById(R.id.no_notes_yet);
            if (value != null && !value.getDocuments().isEmpty()) {
                notesDisplayedHere.setVisibility(View.GONE);
            } else {
                notesDisplayedHere.setVisibility(View.VISIBLE);
            }
        });

        final FirestoreRecyclerOptions<FirebaseModel>[] allUserNotes = new FirestoreRecyclerOptions[]{new FirestoreRecyclerOptions.Builder<FirebaseModel>().setQuery(query, FirebaseModel.class).build()};
        noteAdapter = new FirestoreRecyclerAdapter<FirebaseModel, NoteViewHolder>(allUserNotes[0]) {
            @SuppressLint("ResourceAsColor")
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull FirebaseModel model) {
                ImageView notesDeleteBtn = holder.itemView.findViewById(R.id.delete_button);
                if (model.getTitle().toLowerCase().contains(notesSearchStr.toLowerCase())) {
                    holder.getNoteLinearLayout().setBackgroundColor(Color.parseColor(model.getColor()));
                    String noteColor = model.getColor();
                    String titleText = model.getTitle();
                    String contentText = model.getContent();
                    if (titleText.length() > 45) {
                        titleText = titleText.substring(0, 41) + "...";
                    }
                    if (contentText.length() > 150) {
                        contentText = contentText.substring(0, 146) + "...";
                    }

                    int newLineCounter = 0;
                    int indexOfTheLastNewLine = 0;

                    Matcher m = Pattern.compile("\r\n|\r|\n").matcher(contentText);
                    while (m.find()) {
                        newLineCounter++;
                        if (newLineCounter < 11)
                            indexOfTheLastNewLine = m.start();
                    }

                    if (newLineCounter > 9)
                        contentText = contentText.substring(0, indexOfTheLastNewLine) + "...";

                    holder.getNoteTitle().setText(titleText);
                    holder.getNoteContent().setText(contentText);

                    docId = getSnapshots().getSnapshot(holder.getBindingAdapterPosition()).getReference();

                    holder.getNoteLinearLayout().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            searchView.clearFocus();
                            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                            intent.putExtra("title", model.getTitle());
                            intent.putExtra("content", model.getContent());
                            intent.putExtra("color", noteColor);
                            docIdStr = noteAdapter.getSnapshots().getSnapshot(holder.getBindingAdapterPosition()).getId();
                            intent.putExtra("docIdStr", docIdStr);
                            startActivity(intent);
                        }
                    });

                    notesDeleteBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            searchView.clearFocus();
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage(getString(R.string.are_you_sure_delete_note))
                                    .setCancelable(true)
                                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                                        getSnapshots().getSnapshot(holder.getBindingAdapterPosition()).getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(MainActivity.this, getString(R.string.note_deleted), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    })
                                    .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel()).show();

                        }
                    });
                } else {
                    holder.getNoteLinearLayout().setVisibility(View.GONE);
                    notesDeleteBtn.setVisibility(View.GONE);
                }
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout, parent, false);
                return new NoteViewHolder(view);
            }
        };
        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        notesRecyclerView.setHasFixedSize(false);
        notesRecyclerView.setItemAnimator(null);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        notesRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        notesRecyclerView.setAdapter(noteAdapter);

        noteAdapter.startListening();

        int notesViewModeSelected = pref.getInt(notesViewModeKey, 0);
        if (notesViewModeSelected == 1) {
            notesViewMode.setImageResource(R.drawable.ic_baseline_view_list_24);
            staggeredGridLayoutManager.setSpanCount(1);
        }
    }


    public void NavStatistics() {
        TextView statisticsMarkDynamics = findViewById(R.id.statistics_mark_dynamics);
        statisticsMarkDynamics.setVisibility(View.GONE);
        ONIS = 5;
        fragment_container_tests.setVisibility(View.GONE);
        list.setVisibility(View.GONE);
        listPh.setVisibility(View.GONE);
        mainConstraintLayout.setVisibility(View.GONE);
        toolbar.setTitle(R.string.statistics_toolbar_title);
        final String previousGPAKey = "PreviousGPA";

        category_index = 4;
        float QuestionsAnsweredFloat = (float) pref.getInt(questionsAnsweredKey, 0); //чтобы rightAnswersFloat считался правильно, хотя надо попробовать с parse float или (float)
        float RightAnswersFloat = (float) pref.getInt(rightAnswersKey, 0);
        float gpaFloat = (RightAnswersFloat / QuestionsAnsweredFloat) * 10;
        if (QuestionsAnsweredFloat == 0)
            statisticsGpa = "0";
        else
            statisticsGpa = String.format("%.1f", gpaFloat);
        int hours = (int) (pref.getLong(timeInTestsKey, 0) / 3600000);
        int minutes = (int) ((pref.getLong(timeInTestsKey, 0) - hours * 3600000) / 60000);
        int seconds = (int) ((pref.getLong(timeInTestsKey, 0) - (hours * 3600000) - (minutes * 60000)) / 1000);
        String hoursS = String.valueOf(hours), minutesS = String.valueOf(minutes), secondsS = String.valueOf(seconds);
        if (minutes < 10)
            minutesS = String.format("0%s", minutesS);
        if (seconds < 10)
            secondsS = String.format("0%s", secondsS);
        String statisticsTimeInTests = String.format("%s:%s:%s", hoursS, minutesS, secondsS);
        int averageTimeMillis;
        if (pref.getInt(testsCompletedKey, 0) == 0)
            averageTimeMillis = 0;
        else
            averageTimeMillis = (int) pref.getLong(timeInTestsKey, 0) / pref.getInt(testsCompletedKey, 0);
        int avHours = averageTimeMillis / 3600000;
        int avMinutes = (averageTimeMillis - avHours * 3600000) / 60000;
        int avSeconds = (averageTimeMillis - (avHours * 3600000) - (avMinutes * 60000)) / 1000;
        String avHoursS = String.valueOf(avHours), avMinutesS = String.valueOf(avMinutes), avSecondsS = String.valueOf(avSeconds);
        if (avMinutes < 10)
            avMinutesS = String.format("0%s", avMinutesS);
        if (avSeconds < 10)
            avSecondsS = String.format("0%s", avSecondsS);
        String statisticsAverageTime = String.format("%s:%s:%s", avHoursS, avMinutesS, avSecondsS);

        int ttCount = pref.getInt(ttCounterKey, 0), ttATCount = pref.getInt(ttAllTopicsCounterKey, 0);
        int TOFCount = pref.getInt(trueOrFalseCounterKey, 0), examCount = pref.getInt(examCounterKey, 0);
        int GBICount = pref.getInt(guessByImageCounterKey, 0), survivalCount = pref.getInt(survivalCounterKey, 0);
        int[] favModeArr = {ttCount, ttATCount, TOFCount, examCount, GBICount, survivalCount};
        ShellSort(favModeArr);
        String statisticsFavModeQuantity;
        if (favModeArr[favModeArr.length - 1] == 0) {
            statisticsFavMode = "—";
            statisticsFavModeQuantity = "0";
        } else {
            if (favModeArr[favModeArr.length - 1] == ttCount)
                statisticsFavMode = getString(R.string.mode1);
            else if (favModeArr[favModeArr.length - 1] == ttATCount)
                statisticsFavMode = getString(R.string.mode2);
            else if (favModeArr[favModeArr.length - 1] == TOFCount)
                statisticsFavMode = getString(R.string.mode3);
            else if (favModeArr[favModeArr.length - 1] == examCount)
                statisticsFavMode = getString(R.string.mode4);
            else if (favModeArr[favModeArr.length - 1] == GBICount)
                statisticsFavMode = getString(R.string.mode5);
            else if (favModeArr[favModeArr.length - 1] == survivalCount)
                statisticsFavMode = getString(R.string.mode6);
            statisticsFavModeQuantity = String.valueOf(favModeArr[favModeArr.length - 1]);
        }

        previousGPA = pref.getFloat(previousGPAKey, 0);

        if (pref.getInt(questionsAnsweredKey, 0) != 0) {
            Timer timer = new Timer();
            final float[] counter = {0};
            float markFloat = (RightAnswersFloat / QuestionsAnsweredFloat) * 10;
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        if (counter[0] >= markFloat) {
                            if (markFloat == 10)
                                mark.setText("10");
                            else
                                mark.setText(String.format(Locale.getDefault(), "%.1f", markFloat));

                            if (previousGPA != 0) {
                                float gpaDifference = (gpaFloat / previousGPA - 1) * 100;
                                if (gpaDifference < 0 && gpaDifference <= -0.1) {
                                    statisticsMarkDynamics.setTextColor(getResources().getColor(R.color.incorrect_answer_red));
                                    statisticsMarkDynamics.setText(String.format(Locale.getDefault(), "%.1f", gpaDifference) + "%");
                                } else if (gpaDifference > 0 && gpaDifference >= 0.1) {
                                    statisticsMarkDynamics.setTextColor(getResources().getColor(R.color.correct_answer_green));
                                    statisticsMarkDynamics.setText("+" + String.format(Locale.getDefault(), "%.1f", gpaDifference) + "%");
                                }
                                statisticsMarkDynamics.setVisibility(View.VISIBLE);
                            }

                            timer.cancel();
                            return;
                        }
                        mark.setText(String.format(Locale.getDefault(), "%.1f", (counter[0])));
                        counter[0] += 0.1;
                    });
                }
            };
            timer.schedule(timerTask, 0, 20);
            circularProgressBar.setProgress(0);
            circularProgressBar2.setProgress(0);
            circularProgressBar.setProgressWithAnimation(markFloat * 10, (long) (markFloat / 10 * 2000));
            circularProgressBar2.setProgressWithAnimation(markFloat * 10, (long) (markFloat / 10 * 2000));
        } else {
            mark.setText("0.0");
        }
        statisticsScrollView.setVisibility(View.VISIBLE);


        String[] leftArr = {getString(R.string.statistics_tests_completed), getString(R.string.statistics_questions_answered), getString(R.string.statistics_right_answers),
                getString(R.string.statistics_gpa), getString(R.string.statistics_time_in_app), getString(R.string.statistics_time_in_tests), getString(R.string.statistics_average_time),
                getString(R.string.statistics_survival_highest), getString(R.string.statistics_10s), statisticsFavMode};
        String[] middleArr = {null, null, null, null, null, null, null, null, null, getString(R.string.statistics_fav_mode)};
        String[] rightArr = {String.valueOf(pref.getInt(testsCompletedKey, 0)), String.valueOf(pref.getInt(questionsAnsweredKey, 0)), String.valueOf(pref.getInt(rightAnswersKey, 0)),
                statisticsGpa, null, statisticsTimeInTests, statisticsAverageTime, String.valueOf(pref.getInt(survivalHighestKey, 0)),
                String.valueOf(pref.getInt(counter10sKey, 0)), statisticsFavModeQuantity};


        leftArrGlobal = leftArr;
        middleArrGlobal = middleArr;
        rightArrGlobal = rightArr;
        rightChronometerArrGlobal = chronometerArr;
        wasStatisticsOpen = true;

        statisticsAdapter = new customStatisticsBaseAdapter(this, leftArr, middleArr, rightArr, chronometerArr, startStopChronometer, statisticsDarkTheme, statisticsTextSize);
        statisticsLV.setAdapter(statisticsAdapter);
        statisticsLV.setVisibility(View.VISIBLE);
        statisticsLV.setEnabled(false);
        setListViewHeightBasedOnItems(statisticsLV);
        StatisticsSaveFloat(previousGPAKey, gpaFloat);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent subjectSelectIntent = new Intent(MainActivity.this, SubjectSelect.class);
        subjectSelectIntent.putExtra("loggedInManually", loggedInManually);
        startActivity(subjectSelectIntent);
        finish();
    }

    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                float px = 500 * (listView.getResources().getDisplayMetrics().density);
                item.measure(View.MeasureSpec.makeMeasureSpec((int) px, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);
            // Get padding
            int totalPadding = listView.getPaddingTop() + listView.getPaddingBottom();

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight + totalPadding;
            listView.setLayoutParams(params);
            listView.requestLayout();
            return true;

        } else {
            return false;
        }

    }


    private void ShellSort(int[] array) {
        int i, j, step, temp;
        int size = array.length;

        for (step = size / 2; step > 0; step /= 2)
            for (i = step; i < size; i++) {
                temp = array[i];
                for (j = i; j >= step; j -= step) {
                    if (temp < array[j - step])
                        array[j] = array[j - step];
                    else break;
                }
                array[j] = temp;
            }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onResume() {
        super.onResume();

        animPreference = defPref.getString("animation_preference", "Один раз после открытия приложения");
        wasListItemPressed = false;
        wasStartQuizPressed = false;

        appSessionStart = System.currentTimeMillis();

        if (defPref.getBoolean("notes_when_launched_switch_preference", false)) {
            FrameLayout hintLayout = findViewById(R.id.notes_when_launched_hint);
            hintLayout.setVisibility(View.GONE);
            StatisticsSaveBoolean("NotesHintHidden", true);
        }

        String text_size_str = defPref.getString("text_size_preference", "Обычный");
        boolean isSwitch = defPref.getBoolean("theme_switch_preference", false);
        switch (text_size_str) {
            case "Крупный":
                if (!text_size_str.equals(text_size_str_was)) {
                    if (isSwitch)
                        adapter = new ArrayAdapter<>(this, R.layout.arraylist_item_text_color_large_dark_theme, new ArrayList<>(Arrays.asList(array)));
                    else
                        adapter = new ArrayAdapter<>(this, R.layout.arraylist_item_text_color_large, new ArrayList<>(Arrays.asList(array)));
                    list.setAdapter(adapter);
                    listPh.setAdapter(adapter);
                    activityMainTV.setTextSize(21);
                    activityMainRandomFact.setTextSize(21);
                    statisticsTextSize = 21;
                }
                break;
            case "Обычный":
                if (!text_size_str.equals(text_size_str_was)) {
                    if (isSwitch)
                        adapter = new ArrayAdapter<>(this, R.layout.arraylist_item_text_color_dark_theme, new ArrayList<>(Arrays.asList(array)));
                    else
                        adapter = new ArrayAdapter<>(this, R.layout.arraylist_item_text_color, new ArrayList<>(Arrays.asList(array)));
                    list.setAdapter(adapter);
                    listPh.setAdapter(adapter);
                    activityMainTV.setTextSize(18);
                    activityMainRandomFact.setTextSize(22);
                    statisticsTextSize = 18;
                }
                break;
            case "Мелкий":
                if (!text_size_str.equals(text_size_str_was)) {
                    if (isSwitch)
                        adapter = new ArrayAdapter<>(this, R.layout.arraylist_item_text_color_small_dark_theme, new ArrayList<>(Arrays.asList(array)));
                    else
                        adapter = new ArrayAdapter<>(this, R.layout.arraylist_item_text_color_small, new ArrayList<>(Arrays.asList(array)));
                    list.setAdapter(adapter);
                    listPh.setAdapter(adapter);
                    activityMainTV.setTextSize(15);
                    activityMainRandomFact.setTextSize(22);
                    statisticsTextSize = 15;
                }
                break;

        }
        text_size_str_was = text_size_str;

        if (isSwitch) {
            circularProgressBar.setVisibility(View.GONE);
            circularProgressBar2.setVisibility(View.VISIBLE);
            if (isSwitch != wasSwitch) { //чтобы каждый раз в onResume не выполнять кучу преобразований (оптимизация) //ScrollView списка возвращается в начало только непосредственно после изменения темы
                updateFactButton.setTextColor(getResources().getColor(R.color.white));
                updateFactButton.setBackgroundResource(R.drawable.button_grey_when_pressed_grey_stroke_round_20_dark_theme);
                mainActivityFrameLayout.setBackgroundColor(getResources().getColor(R.color.dark_theme));
                activityMainTV.setTextColor(getResources().getColor(R.color.light_text));
                activityMainRandomFact.setTextColor(getResources().getColor(R.color.light_text));
                navView.setBackgroundColor(getResources().getColor(R.color.dark_theme));
                navView.setItemTextColor(ColorStateList.valueOf(getResources().getColor(R.color.light_text)));
                switch (text_size_str) {
                    case "Крупный":
                        adapter = new ArrayAdapter<>(this, R.layout.arraylist_item_text_color_large_dark_theme, new ArrayList<>(Arrays.asList(array)));
                        break;
                    case "Обычный":
                        adapter = new ArrayAdapter<>(this, R.layout.arraylist_item_text_color_dark_theme, new ArrayList<>(Arrays.asList(array)));
                        break;
                    case "Мелкий":
                        adapter = new ArrayAdapter<>(this, R.layout.arraylist_item_text_color_small_dark_theme, new ArrayList<>(Arrays.asList(array)));
                        break;
                }

                mark.setTextColor(getResources().getColor(R.color.light_text));
                wasSwitch = true;
                list.setAdapter(adapter);
                listPh.setAdapter(adapter);

                statisticsDarkTheme = true;

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item_dark_theme, sqLiteHelper.selectSummaryTitles(subject));
                //ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.summary_array, R.layout.simple_spinner_item_dark_theme);
                spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_dark_theme);

                int ATTRowCount = sqLiteHelper.countAllTopicsTrainingQuestionSets(subject);
                String[] allTopicsTrainingTitleArray = new String[ATTRowCount];
                for (int i = 0; i < ATTRowCount; i++)
                    allTopicsTrainingTitleArray[i] = getString(R.string.ticket) + " " + (i + 1);
                ArrayAdapter<String> spinnerAdapterAllTopics = new ArrayAdapter<>(this, R.layout.simple_spinner_item_dark_theme, allTopicsTrainingTitleArray);
                //ArrayAdapter<CharSequence> spinnerAdapterAllTopics = ArrayAdapter.createFromResource(this, R.array.spinners_all_topics_array, R.layout.simple_spinner_item_dark_theme);
                spinnerAdapterAllTopics.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_dark_theme);
                spinner.setAdapter(spinnerAdapter);
                spinnerAllTopics.setAdapter(spinnerAdapterAllTopics);
                spinner.setBackground(getDrawable(R.drawable.round_back_dark_10));
                spinnerAllTopics.setBackground(getDrawable(R.drawable.round_back_dark_10));

                if (!is_topic_training_pressed)
                    topic_training.setBackgroundResource(R.drawable.round_back_dark_10);
                else
                    topic_training.setBackgroundResource(R.drawable.round_back_dark_stroke_10);
                if (!is_all_topics_training_pressed)
                    all_topics_training.setBackgroundResource(R.drawable.round_back_dark_10);
                else
                    all_topics_training.setBackgroundResource(R.drawable.round_back_dark_stroke_10);
                if (!is_true_or_false_pressed)
                    true_or_false.setBackgroundResource(R.drawable.round_back_dark_10);
                else
                    true_or_false.setBackgroundResource(R.drawable.round_back_dark_stroke_10);
                if (!is_exam_pressed)
                    exam.setBackgroundResource(R.drawable.round_back_dark_10);
                else
                    exam.setBackgroundResource(R.drawable.round_back_dark_stroke_10);
                if (!is_guess_by_image_pressed)
                    guess_by_image.setBackgroundResource(R.drawable.round_back_dark_10);
                else
                    guess_by_image.setBackgroundResource(R.drawable.round_back_dark_stroke_10);
                if (!is_survival_pressed)
                    survival.setBackgroundResource(R.drawable.round_back_dark_10);
                else
                    survival.setBackgroundResource(R.drawable.round_back_dark_stroke_10);
                startQuiz.setBackground(getDrawable(R.drawable.button_dark_when_pressed_stroke_20));
                startQuiz.setTextColor(getResources().getColor(R.color.light_text));
                TextView ttTV = findViewById(R.id.topic_training_tv), attTV = findViewById(R.id.all_topic_training_tv),
                        tofTV = findViewById(R.id.true_or_false_tv), eTV = findViewById(R.id.exam_tv),
                        gbiTV = findViewById(R.id.guess_by_image_tv), sTV = findViewById(R.id.survival_tv);
                ttTV.setTextColor(getResources().getColor(R.color.light_text));
                attTV.setTextColor(getResources().getColor(R.color.light_text));
                tofTV.setTextColor(getResources().getColor(R.color.light_text));
                eTV.setTextColor(getResources().getColor(R.color.light_text));
                gbiTV.setTextColor(getResources().getColor(R.color.light_text));
                sTV.setTextColor(getResources().getColor(R.color.light_text));
//                sortButton.setTextColor(getResources().getColor(R.color.light_text));
//                sortButton.setBackgroundResource(R.drawable.sort_notes_dark_button_10);
                searchView.setBackgroundResource(R.drawable.round_back_grey10);
                notesMessage.setTextColor(getResources().getColor(R.color.light_text));
            }

        } else {
            circularProgressBar.setVisibility(View.VISIBLE);
            circularProgressBar2.setVisibility(View.GONE);
            if (isSwitch != wasSwitch) {
                updateFactButton.setTextColor(getResources().getColor(R.color.dark_text));
                updateFactButton.setBackgroundResource(R.drawable.button_grey_when_pressed_grey_stroke_round_20);
                mainActivityFrameLayout.setBackgroundColor(getResources().getColor(R.color.white));
                activityMainTV.setTextColor(getResources().getColor(R.color.dark_text));
                activityMainRandomFact.setTextColor(getResources().getColor(R.color.dark_text));
                navView.setBackgroundColor(getResources().getColor(R.color.white));
                navView.setItemTextColor(ColorStateList.valueOf(getResources().getColor(R.color.dark_text)));
                switch (text_size_str) {
                    case "Крупный":
                        adapter = new ArrayAdapter<>(this, R.layout.arraylist_item_text_color_large, new ArrayList<>(Arrays.asList(array)));
                        break;
                    case "Обычный":
                        adapter = new ArrayAdapter<>(this, R.layout.arraylist_item_text_color, new ArrayList<>(Arrays.asList(array)));
                        break;
                    case "Мелкий":
                        adapter = new ArrayAdapter<>(this, R.layout.arraylist_item_text_color_small, new ArrayList<>(Arrays.asList(array)));
                        break;
                }
                mark.setTextColor(getResources().getColor(R.color.dark_text));
                wasSwitch = false;
                list.setAdapter(adapter);
                listPh.setAdapter(adapter);

                statisticsDarkTheme = false;

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, sqLiteHelper.selectSummaryTitles(subject));
                spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                int ATTRowCount = sqLiteHelper.countAllTopicsTrainingQuestionSets(subject);
                String[] allTopicsTrainingTitleArray = new String[ATTRowCount];
                for (int i = 0; i < ATTRowCount; i++)
                    allTopicsTrainingTitleArray[i] = getString(R.string.ticket) + " " + (i + 1);
                ArrayAdapter<String> spinnerAdapterAllTopics = new ArrayAdapter<>(this, R.layout.simple_spinner_item, allTopicsTrainingTitleArray);
                spinnerAdapterAllTopics.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);
                spinnerAllTopics.setAdapter(spinnerAdapterAllTopics);
                spinner.setBackground(getDrawable(R.drawable.round_back_white_10));
                spinnerAllTopics.setBackground(getDrawable(R.drawable.round_back_white_10));

                if (!is_topic_training_pressed)
                    topic_training.setBackgroundResource(R.drawable.round_back_white_10);
                else
                    topic_training.setBackgroundResource(R.drawable.round_back_white_stroke10);
                if (!is_all_topics_training_pressed)
                    all_topics_training.setBackgroundResource(R.drawable.round_back_white_10);
                else
                    all_topics_training.setBackgroundResource(R.drawable.round_back_white_stroke10);
                if (!is_true_or_false_pressed)
                    true_or_false.setBackgroundResource(R.drawable.round_back_white_10);
                else
                    true_or_false.setBackgroundResource(R.drawable.round_back_white_stroke10);
                if (!is_exam_pressed)
                    exam.setBackgroundResource(R.drawable.round_back_white_10);
                else
                    exam.setBackgroundResource(R.drawable.round_back_white_stroke10);
                if (!is_guess_by_image_pressed)
                    guess_by_image.setBackgroundResource(R.drawable.round_back_white_10);
                else
                    guess_by_image.setBackgroundResource(R.drawable.round_back_white_stroke10);
                if (!is_survival_pressed)
                    survival.setBackgroundResource(R.drawable.round_back_white_10);
                else
                    survival.setBackgroundResource(R.drawable.round_back_white_stroke10);
                startQuiz.setBackground(getDrawable(R.drawable.button_white_when_pressed_stroke_20));
                startQuiz.setTextColor(getResources().getColor(R.color.dark_text));
                TextView ttTV = findViewById(R.id.topic_training_tv), attTV = findViewById(R.id.all_topic_training_tv),
                        tofTV = findViewById(R.id.true_or_false_tv), eTV = findViewById(R.id.exam_tv),
                        gbiTV = findViewById(R.id.guess_by_image_tv), sTV = findViewById(R.id.survival_tv);
                ttTV.setTextColor(getResources().getColor(R.color.dark_text));
                attTV.setTextColor(getResources().getColor(R.color.dark_text));
                tofTV.setTextColor(getResources().getColor(R.color.dark_text));
                eTV.setTextColor(getResources().getColor(R.color.dark_text));
                gbiTV.setTextColor(getResources().getColor(R.color.dark_text));
                sTV.setTextColor(getResources().getColor(R.color.dark_text));
//                sortButton.setTextColor(getResources().getColor(R.color.dark_text));
//                sortButton.setBackgroundResource(R.drawable.button_light_grey_when_pressed_grey_10);
                searchView.setBackgroundResource(R.drawable.round_back_not_so_light_grey_10);
                notesMessage.setTextColor(getResources().getColor(R.color.dark_text));
            }
        }


        if (ONIS == 1 || ONIS == 2) {
            list.setVisibility(View.GONE);
            listPh.setVisibility(View.GONE);
        }


        appSessionStart = System.currentTimeMillis();

        if (!isChronometerRunning) {
            long chronometerTimeInAppBase = SystemClock.elapsedRealtime() - pref.getLong(timeInAppKey, 0);
            startStopChronometer = true;
            for (int i = 0; i < 11; i++) {
                if (i == 4)
                    chronometerArr[i] = chronometerTimeInAppBase;
                else
                    chronometerArr[i] = -1;
            }
            statisticsAdapter.notifyDataSetChanged();
            isChronometerRunning = true;
        }

        statisticsAdapter.notifyDataSetChanged();
        if (wasStatisticsOpen) {
            statisticsAdapter = new customStatisticsBaseAdapter(this, leftArrGlobal, middleArrGlobal, rightArrGlobal, rightChronometerArrGlobal, startStopChronometer, statisticsDarkTheme, statisticsTextSize);
            statisticsLV.setAdapter(statisticsAdapter);
        }

        setListViewHeightBasedOnItems(statisticsLV);
    }


    @Override
    protected void onPause() { //onPause срабатывает только при сворачивании приложения, при переходе в другое активити не вызывается!!!!!!!!!!!!!!!
        //onStop тоже не вызывается
        super.onPause();
        StatisticsSaveLong(timeInAppKey, pref.getLong(timeInAppKey, 0) + (System.currentTimeMillis() - appSessionStart));
        if (isChronometerRunning) {
            startStopChronometer = false;
            statisticsAdapter.notifyDataSetChanged();
            isChronometerRunning = false;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        spinnerChoice = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        spinnerChoice = adapterView.getItemAtPosition(0).toString();
    }

    public void StatisticsSave(String key, int value) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putInt(key, value);
        edit.apply();
    }

    public void StatisticsSaveFloat(String key, float value) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putFloat(key, value);
        edit.apply();
    }

    public void StatisticsSaveLong(String key, long value) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putLong(key, value);
        edit.apply();
    }

    public void StatisticsSaveBoolean(String key, boolean value) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean(key, value);
        edit.apply();
    }

    private void IntentTests() {
        Intent intent = new Intent(MainActivity.this, TopicTrainingActivity.class);
        spinnerChoice = spinner.getSelectedItem().toString();
        String spinnerChoiceAllTopics = spinnerAllTopics.getSelectedItem().toString();
        intent.putExtra("spinnerChoice", spinnerChoice);
        intent.putExtra("spinnerChoiceAllTopics", spinnerChoiceAllTopics);
        intent.putExtra("testsMode", testsMode);
        intent.putExtra("subject", subject);
        startActivity(intent);
    }


    private void statisticsSaveDB(byte loggingOut) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("questionsAnswered", pref.getInt(questionsAnsweredKey, 0));
        dataMap.put("rightAnswers", pref.getInt(rightAnswersKey, 0));
        dataMap.put("testsCompleted", pref.getInt(testsCompletedKey, 0));
        dataMap.put("ttCounter", pref.getInt(ttCounterKey, 0));
        dataMap.put("ttAllTopicsCounter", pref.getInt(ttAllTopicsCounterKey, 0));
        dataMap.put("trueOrFalseCounter", pref.getInt(trueOrFalseCounterKey, 0));
        dataMap.put("examCounter", pref.getInt(examCounterKey, 0));
        dataMap.put("guessByImageCounter", pref.getInt(guessByImageCounterKey, 0));
        dataMap.put("survivalCounter", pref.getInt(survivalCounterKey, 0));
        dataMap.put("survivalHighest", pref.getInt(survivalHighestKey, 0));
        dataMap.put("counter10s", pref.getInt(counter10sKey, 0));
        dataMap.put("timeInApp", pref.getLong(timeInAppKey, 0));
        dataMap.put("timeInTests", pref.getLong(timeInTestsKey, 0));
        if (loggingOut == 0) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("Users").document(FirebaseAuth.getInstance().getUid()).set(dataMap, SetOptions.merge()).addOnSuccessListener(unused -> {
            });
        }
    }


    @Override
    protected void onStart() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (!Common.isConnectedToInternet(MainActivity.this))
                        noWifiImage.setVisibility(View.VISIBLE);
                    else
                        noWifiImage.setVisibility(View.GONE);
                    Log.d("TimerTask", "Timer task is running");
                });
            }
        };
        timer.schedule(timerTask, 0, 2000);

        if (idVar == R.id.nav_notes) {
            noteAdapter.startListening();
            Log.d("noteAdapter", "start listening from onStart");
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        statisticsSaveThread thread = new statisticsSaveThread();
        thread.start();
        timer.cancel();
        timer.purge();
        if (noteAdapter != null) {
            noteAdapter.stopListening();
        }
        super.onStop();
    }


    class statisticsSaveThread extends Thread {
        @Override
        public void run() {
            statisticsSaveDB(loggingOut);
        }
    }

}