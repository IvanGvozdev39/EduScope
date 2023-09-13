package com.eduscope.eduscope;

import static java.lang.String.valueOf;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.eduscope.eduscope.subject_select.Subject;
import com.eduscope.eduscope.subject_select.SubjectRecyclerViewAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubjectSelect extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SubjectRecyclerViewAdapter.SubjectViewHolder.recyclerViewClickListener {

    private NavigationView navView;
    private DrawerLayout drawer;
    private int idVar = R.id.nav_subject_select;
    private ProgressBar loading;
    private final boolean loggedInManually = false;
    private long appSessionStart;
    private boolean wentToMainActivity = false; //saving statistics to database isn't necessary if a user goes to MainActivity, so the app only saves data if !wentToMainActivity
    private final String timeInAppKey = "TimeInApp";
    private SharedPreferences pref;
    private ImageView noWifiImage;
    private Timer timer;
    private SharedPreferences defPref;
    private final String testsCompletedKey = "TestsCompleted", rightAnswersKey = "RightAnswers", questionsAnsweredKey = "QuestionsAnswered",
            timeInTestsKey = "TimeInTests", survivalHighestKey = "HighestSurvivalScore", ttCounterKey = "TTCounter",
            ttAllTopicsCounterKey = "TTAllTopicsCounter", trueOrFalseCounterKey = "TrueOrFalseCounter", examCounterKey = "ExamCounter", guessByImageCounterKey = "GuessByImageCounter", //каунтеры для определения любимого режима
            survivalCounterKey = "SurvivalCounter", counter10sKey = "10sCounter", statisticsUploadedKey = "statisticsUploaded";
    private AppCompatButton sortButton;
    private DocumentReference docId;

    private RecyclerView recyclerView;
    private SubjectRecyclerViewAdapter recyclerAdapter;
    private byte loggingOut;
    private List<Subject> subjectList;
    private ProgressBar dialogProgressBar;
    private TextView dialogFilesWeigthTV;
    private Dialog dialog;
    private AppCompatButton dialogYesButton;
    private AppCompatButton dialogNoButton;
    private AppCompatButton dialogCancelButton;
    private ProgressBar dialogHorizontalProgressBar;
    private ProgressBar dialogLoadingProgressBar, dialogLoadingProgressBarUpdateCheck;
    private TextView dialogPercentageTV;
    private TextView dialogText;
    private RelativeLayout dialogHorizontalProgressRV;
    private LinearLayout dialogTitleLinearLayout;
    private TextView dialogTitleUpdateCheckTV;
    private long timeAfterDialogShowedUp;
    private int documentNumber;
    private Map<String, Object> dataMapRandFactImages, dataMapRandFactText;
    private boolean[] continueAfterNetworkError;
    private boolean[] cancelClicked;
    private boolean[] wasNNDialogShown;
    private Map<String, Object> dataMapQuestion;
    private Map<String, Object> dataMapOption1;
    private Map<String, Object> dataMapOption2;
    private Map<String, Object> dataMapOption3;
    private Map<String, Object> dataMapOption4;
    private Map<String, Object> dataMapAnswer;
    private Map<String, Integer> dataMapQuestionSet;
    private FirestoreRecyclerAdapter<FirebaseModel, NoteViewHolder> noteAdapter;
    private boolean isChronometerRunning = false;

    private Map<String, Object> dataMapQuestionAT;
    private Map<String, Object> dataMapOption1AT;
    private Map<String, Object> dataMapOption2AT;
    private Map<String, Object> dataMapOption3AT;
    private Map<String, Object> dataMapOption4AT;
    private Map<String, Object> dataMapAnswerAT;
    private Map<String, Integer> dataMapQuestionSetAT;
    private FirebaseUser user;
    private String docIdStr;

    private String statisticsFavMode;
    private float previousGPA = 0;
    private TextView mark;
    private CircularProgressBar circularProgressBar, circularProgressBar2;
    private String[] leftArrGlobal, middleArrGlobal, rightArrGlobal;
    private long[] rightChronometerArrGlobal;
    private final long[] chronometerArr = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    private boolean wasStatisticsOpen = false;
    private customStatisticsBaseAdapter statisticsAdapter;
    private ListView statisticsLV;
    private boolean startStopChronometer = true;
    private boolean statisticsDarkTheme = false;
    private int statisticsTextSize = 18;
    private ProgressBar loadingStatistics;
    private LinearLayout statisticsLinearLayout;
    private boolean statisticsUploaded;
    private String text_size_str_was;
    private ArrayAdapter<String> adapter;
    private SearchView searchView;
    private Query query;
    private String notesSearchStr = "";
    private final String notesViewModeKey = "NotesViewMode";
    private final String sortButtonModeKey = "SortButtonMode";
    private TextView notesMessage;

    private Map<String, Object> dataMapQuestionTOF;
    private Map<String, Object> dataMapAnswerTOF;
    private Map<String, Object> dataMapRevealAnswerTOF;
    private Map<String, Integer> dataMapQuestionSetTOF;

    private Map<String, Object> dataMapQuestionGBI;
    private Map<String, Object> dataMapOption1GBI;
    private Map<String, Object> dataMapOption2GBI;
    private Map<String, Object> dataMapOption3GBI;
    private Map<String, Object> dataMapOption4GBI;
    private Map<String, Object> dataMapAnswerGBI;
    private Map<String, Integer> dataMapQuestionSetGBI;
    private Toolbar toolbar;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    private final int[] numQueriesTopicTraining = {0};
    private final int[] numQueriesAllTopicsTraining = {0};
    private final int[] numQueriesTrueOrFalse = {0};
    private final int[] numQueriesGuessByImage = {0};
    private final float[] progress = {0};
    private int firestoreVersion = 0;
    private final boolean[] wasProgress100ToGoToMain = {false};
    private LinearLayout subjectSelectSectionLinear;
    private FrameLayout notesSectionFrame;
    private ScrollView statisticsSectionScroll;
    private ImageView notesViewMode;

    public SubjectSelect() {
    }


    @SuppressLint({"CutPasteId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_select);

        statisticsAdapter = new customStatisticsBaseAdapter(this, null, null, null, null, startStopChronometer, statisticsDarkTheme, statisticsTextSize);
        toolbar = findViewById(R.id.toolbar);
        subjectSelectSectionLinear = findViewById(R.id.subject_select_section);
        notesSectionFrame = findViewById(R.id.notes_section);
        statisticsSectionScroll = findViewById(R.id.statisticsScrollView);
        noWifiImage = findViewById(R.id.no_wifi_image_toolbar);
        mark = findViewById(R.id.mark);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        circularProgressBar2 = findViewById(R.id.circularProgressBar2);
        statisticsLV = findViewById(R.id.statisticsListView);
        loadingStatistics = findViewById(R.id.loading_progress_bar);
        statisticsLinearLayout = findViewById(R.id.statisticsLinearLayout);
        notesViewMode = findViewById(R.id.notes_view_mode);
        searchView = findViewById(R.id.search_view);
        sortButton = findViewById(R.id.sort_button); //Sort Button
        user = FirebaseAuth.getInstance().getCurrentUser();
        FloatingActionButton createNoteBtn = findViewById(R.id.create_note_floating_button);
        Intent createNoteIntent = new Intent(SubjectSelect.this, CreateNoteActivity.class);
        createNoteBtn.setOnClickListener(view -> {
            searchView.clearFocus();
            startActivity(createNoteIntent);
        });
        notesMessage = findViewById(R.id.notes_message);
        pref = getSharedPreferences("Statistics", Context.MODE_PRIVATE);
        defPref = PreferenceManager.getDefaultSharedPreferences(this);

        ConstraintLayout uniBanner = findViewById(R.id.uni_banner);
        loading = findViewById(R.id.loading_progress_bar);

        List<Integer> subjectImageList = new ArrayList<>();
        List<String> subjectTextList = new ArrayList<>();

        //Temporary solution:
        subjectTextList.add("Философия");
        subjectTextList.add("Маркетинг");
        subjectTextList.add("Финансовая грамотность");
        subjectTextList.add("Психология");

        subjectImageList.add(R.drawable.philosophy);
        subjectImageList.add(R.drawable.marketing);
        subjectImageList.add(R.drawable.financial_literacy);
        subjectImageList.add(R.drawable.psychology);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
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

        recyclerView = findViewById(R.id.subject_recyclerview);
        subjectList = new ArrayList<>();
        for (int i = 0; i < subjectImageList.size(); i++) {
            subjectList.add(new Subject(subjectImageList.get(i), subjectTextList.get(i)));
        }
        recyclerAdapter = new SubjectRecyclerViewAdapter(this, subjectList, defPref.getBoolean("theme_switch_preference", false), this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(recyclerAdapter);

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderTV = headerView.findViewById(R.id.nav_header_textview);

        firestore.collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                try {
                    navHeaderTV.setText(document.get("name") + " " + document.get("aftername"));
                } catch (Exception ex) {
                    navHeaderTV.setText(getString(R.string.app_name));
                }
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.subject_select_title);
        setSupportActionBar(toolbar);

        navView = findViewById(R.id.nav_view);
        drawer = findViewById(R.id.drawer_layout);
        navView.setNavigationItemSelectedListener(this);
        navView.setCheckedItem(R.id.nav_subject_select);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        //Temporary:
        /*uniBanner.setOnClickListener(view -> {
            loading.setVisibility(View.VISIBLE);
            wentToMainActivity = true;
            Intent intent = new Intent(SubjectSelect.this, MainActivity.class);                                //убрать переход на MainActivity по баннеру
            intent.putExtra("loggedInManually", loggedInManually);
            intent.putExtra("subject", "Philosophy"); //not supposed to be like this
            startActivity(intent);
            finish();
        });*/

        if (defPref.getBoolean("notes_when_launched_switch_preference", false)) {
            toolbar.setTitle(getString(R.string.notes_toolbar_title));
            idVar = R.id.nav_notes;
            notesViewMode.setVisibility(View.VISIBLE);
            subjectSelectSectionLinear.setVisibility(View.GONE);
            notesSectionFrame.setVisibility(View.VISIBLE);
            notesSectionFrame.setOnClickListener(view -> searchView.clearFocus());
            toolbar.setOnClickListener(view -> searchView.clearFocus());
            statisticsLV.setVisibility(View.GONE);
            statisticsSectionScroll.setVisibility(View.GONE);
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

            MenuItem menuItem = navView.getMenu().findItem(R.id.nav_notes);
            menuItem.setChecked(true);
            StatisticsSaveBoolean("NotesHintHidden", true);
        } else {
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

        notesViewMode.setOnClickListener(view -> {
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
        });

        sortButton.setOnClickListener(view -> {
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
        });

        if (defPref.getBoolean("notes_when_launched_switch_preference", false)) {
            FrameLayout hintLayout = findViewById(R.id.notes_when_launched_hint);
            hintLayout.setVisibility(View.GONE);
            StatisticsSaveBoolean("NotesHintHidden", true);
        }
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


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        final int id = item.getItemId();

        if (id == R.id.nav_subject_select) {
            idVar = R.id.nav_subject_select;
            subjectSelectSectionLinear.setVisibility(View.VISIBLE);
            notesSectionFrame.setVisibility(View.GONE);
            statisticsSectionScroll.setVisibility(View.GONE);
            toolbar.setTitle(getString(R.string.subject_select_title));
        }
        else if (id == R.id.nav_statistics) {
            idVar = R.id.nav_statistics;
            subjectSelectSectionLinear.setVisibility(View.GONE);
            notesSectionFrame.setVisibility(View.GONE);
            statisticsSectionScroll.setVisibility(View.VISIBLE);
            toolbar.setTitle(getString(R.string.statistics_toolbar_title));
            NavStatistics();
        }
        else if (id == R.id.nav_notes) {
            toolbar.setTitle(getString(R.string.notes_toolbar_title));
            idVar = R.id.nav_notes;
            notesViewMode.setVisibility(View.VISIBLE);
            subjectSelectSectionLinear.setVisibility(View.GONE);
            notesSectionFrame.setVisibility(View.VISIBLE);
            notesSectionFrame.setOnClickListener(view -> searchView.clearFocus());
            toolbar.setOnClickListener(view -> searchView.clearFocus());
            statisticsLV.setVisibility(View.GONE);
            statisticsSectionScroll.setVisibility(View.GONE);
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
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
        }
        else if (id == R.id.nav_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Вы уверены, что хотите выйти из аккаунта?")
                    .setCancelable(true)
                    .setPositiveButton("Да", (dialogInterface, i) -> {
                        loggingOut++;
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(SubjectSelect.this, LoginActivity.class));
                        finish();
                    })
                    .setNegativeButton("Нет", (dialogInterface, i) -> dialogInterface.cancel()).show();
        }
        if (id != R.id.nav_logout)
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

                    holder.getNoteLinearLayout().setOnClickListener(view -> {
                        searchView.clearFocus();
                        Intent intent = new Intent(SubjectSelect.this, EditNoteActivity.class);
                        intent.putExtra("title", model.getTitle());
                        intent.putExtra("content", model.getContent());
                        intent.putExtra("color", noteColor);
                        docIdStr = noteAdapter.getSnapshots().getSnapshot(holder.getBindingAdapterPosition()).getId();
                        intent.putExtra("docIdStr", docIdStr);
                        startActivity(intent);
                    });

                    notesDeleteBtn.setOnClickListener(view -> {
                        searchView.clearFocus();
                        AlertDialog.Builder builder = new AlertDialog.Builder(SubjectSelect.this);
                        builder.setMessage(getString(R.string.are_you_sure_delete_note))
                                .setCancelable(true)
                                .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> getSnapshots().getSnapshot(holder.getBindingAdapterPosition()).getReference().delete().addOnSuccessListener(unused -> Toast.makeText(SubjectSelect.this, getString(R.string.note_deleted), Toast.LENGTH_SHORT).show()))
                                .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel()).show();

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
        RecyclerView notesRecyclerView = findViewById(R.id.notes_recycler_view);
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


    @SuppressLint("DefaultLocale")
    private void NavStatistics() {
        TextView statisticsMarkDynamics = findViewById(R.id.statistics_mark_dynamics);
        statisticsMarkDynamics.setVisibility(View.GONE);
        toolbar.setTitle(R.string.statistics_toolbar_title);
        final String previousGPAKey = "PreviousGPA";

        float QuestionsAnsweredFloat = (float) pref.getInt(questionsAnsweredKey, 0); //чтобы rightAnswersFloat считался правильно, хотя надо попробовать с parse float или (float)
        float RightAnswersFloat = (float) pref.getInt(rightAnswersKey, 0);
        float gpaFloat = (RightAnswersFloat / QuestionsAnsweredFloat) * 10;
        String statisticsGpa;
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
                @SuppressLint("SetTextI18n")
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


    public void StatisticsSaveFloat(String key, float value) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putFloat(key, value);
        edit.apply();
    }


    public static void setListViewHeightBasedOnItems(ListView listView) {

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

        } else {
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


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    protected void onResume() {
        appSessionStart = System.currentTimeMillis();


        NestedScrollView scrollView = findViewById(R.id.subj_select_nested_scroll_view);
        TextView publicCurriculum = findViewById(R.id.public_curriculum_text_view);

        final View recyclerViewLayout = getLayoutInflater().inflate(R.layout.cardview_item_subject, null);
        TextView recyclerText = recyclerViewLayout.findViewById(R.id.subject_text);
        LinearLayout recyclerLinear = recyclerViewLayout.findViewById(R.id.subject_select_linear_layout);
        boolean isSwitch = defPref.getBoolean("theme_switch_preference", false);
        if (isSwitch) {
            scrollView.setBackgroundResource(R.color.dark_theme);
            publicCurriculum.setTextColor(getResources().getColor(R.color.light_text));
            recyclerText.setTextColor(getResources().getColor(R.color.light_text));
            recyclerLinear.setBackground(getResources().getDrawable(R.drawable.round_back_dark_10));
            navView.setBackgroundColor(getResources().getColor(R.color.dark_theme));
            navView.setItemTextColor(ColorStateList.valueOf(getResources().getColor(R.color.light_text)));
            mark.setTextColor(getResources().getColor(R.color.light_text));
            statisticsDarkTheme = true;
            circularProgressBar2.setVisibility(View.VISIBLE);
            circularProgressBar.setVisibility(View.GONE);
            notesMessage.setTextColor(getResources().getColor(R.color.light_text));
        } else {
            scrollView.setBackgroundResource(R.color.white);
            publicCurriculum.setTextColor(getResources().getColor(R.color.dark_text));
            recyclerText.setTextColor(getResources().getColor(R.color.dark_text));
            recyclerLinear.setBackground(getResources().getDrawable(R.drawable.round_back_white_10));
            navView.setBackgroundColor(getResources().getColor(R.color.white));
            navView.setItemTextColor(ColorStateList.valueOf(getResources().getColor(R.color.dark_text)));
            mark.setTextColor(getResources().getColor(R.color.dark_text));
            statisticsDarkTheme = false;
            circularProgressBar2.setVisibility(View.GONE);
            circularProgressBar.setVisibility(View.VISIBLE);
            notesMessage.setTextColor(getResources().getColor(R.color.dark_text));
        }
        recyclerAdapter = new SubjectRecyclerViewAdapter(this, subjectList, defPref.getBoolean("theme_switch_preference", false), this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(recyclerAdapter);

        String text_size_str = defPref.getString("text_size_preference", "Обычный");
        switch (text_size_str) {
            case "Крупный":
                if (!text_size_str.equals(text_size_str_was)) {
                    statisticsTextSize = 21;
                }
                break;
            case "Обычный":
                if (!text_size_str.equals(text_size_str_was)) {
                    statisticsTextSize = 18;
                }
                break;
            case "Мелкий":
                if (!text_size_str.equals(text_size_str_was)) {
                    statisticsTextSize = 15;
                }
                break;
        }
        text_size_str_was = text_size_str;
        super.onResume();

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
    protected void onPause() {
        super.onPause();
        timer.cancel();
        timer.purge();
        StatisticsSaveLong(timeInAppKey, pref.getLong(timeInAppKey, 0) + (System.currentTimeMillis() - appSessionStart));
        if (!wentToMainActivity)
            statisticsSaveDB(loggingOut);
        if (isChronometerRunning) {
            startStopChronometer = false;
            statisticsAdapter.notifyDataSetChanged();
            isChronometerRunning = false;
        }
    }


    public void StatisticsSaveLong(String key, long value) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putLong(key, value);
        edit.apply();
    }


    private void statisticsSaveDB(byte loggingOut) {
        Map<String, Object> dataMap = new HashMap<>();
        String questionsAnsweredKey = "QuestionsAnswered";
        dataMap.put("questionsAnswered", pref.getInt(questionsAnsweredKey, 0));
        String rightAnswersKey = "RightAnswers";
        dataMap.put("rightAnswers", pref.getInt(rightAnswersKey, 0));
        String testsCompletedKey = "TestsCompleted";
        dataMap.put("testsCompleted", pref.getInt(testsCompletedKey, 0));
        String ttCounterKey = "TTCounter";
        dataMap.put("ttCounter", pref.getInt(ttCounterKey, 0));
        String ttAllTopicsCounterKey = "TTAllTopicsCounter";
        dataMap.put("ttAllTopicsCounter", pref.getInt(ttAllTopicsCounterKey, 0));
        String trueOrFalseCounterKey = "TrueOrFalseCounter";
        dataMap.put("trueOrFalseCounter", pref.getInt(trueOrFalseCounterKey, 0));
        String examCounterKey = "ExamCounter";
        dataMap.put("examCounter", pref.getInt(examCounterKey, 0));
        //каунтеры для определения любимого режима
        String guessByImageCounterKey = "GuessByImageCounter";
        dataMap.put("guessByImageCounter", pref.getInt(guessByImageCounterKey, 0));
        String survivalCounterKey = "SurvivalCounter";
        dataMap.put("survivalCounter", pref.getInt(survivalCounterKey, 0));
        String survivalHighestKey = "HighestSurvivalScore";
        dataMap.put("survivalHighest", pref.getInt(survivalHighestKey, 0));
        String counter10sKey = "10sCounter";
        dataMap.put("counter10s", pref.getInt(counter10sKey, 0));
        dataMap.put("timeInApp", pref.getLong(timeInAppKey, 0));
        String timeInTestsKey = "TimeInTests";
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
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (!Common.isConnectedToInternet(SubjectSelect.this))
                        noWifiImage.setVisibility(View.VISIBLE);
                    else
                        noWifiImage.setVisibility(View.GONE);
                    Log.d("TimerTask", "Timer task is running");
                });
            }
        };
        timer.schedule(timerTask, 0, 2000);
        super.onStart();
    }

    @Override
    protected void onStop() {
        timer.cancel();
        timer.purge();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(Subject subject) {
        String[] subjectStrArray = {"Philosophy", "Marketing", "Financial_Literacy", "Psychology"};
        String[] subjectStrArrayRus = { "Философия", "Маркетинг", "Финансовая грамотность", "Психология"};  // если эти списки брать из firestore, то можно будет добавлять новые предметы без обновления приложения в google play
        String subjectStr = null;
        for (int i = 0; i < subjectStrArray.length; i++)
            if (subject.text.equals(subjectStrArray[i]))
                subjectStr = subjectStrArray[i];
        if (subjectStr == null)
            for (int i = 0; i < subjectStrArray.length; i++)
                if (subject.text.equals(subjectStrArrayRus[i]))
                    subjectStr = subjectStrArray[i];

        dialog = new Dialog(SubjectSelect.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //Скрыть встроенный заголовок
        dialog.setContentView(R.layout.dialog_sqlite_upload);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //Прозрачный фон окна
        dialogYesButton = dialog.findViewById(R.id.yes_button_dialog);
        dialogNoButton = dialog.findViewById(R.id.no_button_dialog);
        dialogCancelButton = dialog.findViewById(R.id.cancel_button_dialog);
        dialogHorizontalProgressBar = dialog.findViewById(R.id.horizontal_progress_bar);
        dialogLoadingProgressBar = dialog.findViewById(R.id.loading_progress_bar);
        dialogLoadingProgressBarUpdateCheck = dialog.findViewById(R.id.loading_progress_bar_update_check);
        dialogPercentageTV = dialog.findViewById(R.id.percentageTextView);
        dialogText = dialog.findViewById(R.id.dialog_textview);
        dialogHorizontalProgressRV = dialog.findViewById(R.id.progress_bar_relative_layout);
        dialogProgressBar = dialog.findViewById(R.id.loading_progress_bar);
        dialogFilesWeigthTV = dialog.findViewById(R.id.files_weight);
        dialogTitleLinearLayout = dialog.findViewById(R.id.dialog_title_linear_layout);
        dialogTitleUpdateCheckTV = dialog.findViewById(R.id.dialog_title_update_check);
        dialogFilesWeigthTV.setVisibility(View.GONE);
        dialog.setCancelable(true);
        new Thread(new fromFirestoreToSQLiteThread(subjectStr)).start();

    }


    public class fromFirestoreToSQLiteThread implements Runnable {
        private final String subject;

        public fromFirestoreToSQLiteThread(String subject) {
            this.subject = subject;
        }


        public void countImageWeights(StorageReference ref, OnCompleteListener<Long> listener) {
            final long[] totalSize = {0};
            ref.listAll()
                    .addOnSuccessListener(listResult -> {
                        List<Task<Long>> tasks = new ArrayList<>();
                        for (StorageReference item : listResult.getItems()) {
                            if (item.getName().endsWith(".jpg") || item.getName().endsWith(".jpeg") || item.getName().endsWith(".png")) {
                                tasks.add(item.getMetadata().continueWith(task -> {
                                    if (task.isSuccessful()) {
                                        StorageMetadata metadata = task.getResult();
                                        return metadata.getSizeBytes();
                                    } else {
                                        throw task.getException();
                                    }
                                }));
                            } else if (item.getName().endsWith("/")) {
                                countImageWeights(item, task -> {
                                    if (task.isSuccessful()) {
                                        totalSize[0] += task.getResult();
                                    } else {
                                        listener.onComplete(task);
                                    }
                                });
                            }
                        }
                        Tasks.whenAllSuccess(tasks).addOnSuccessListener(sizes -> {
                            long totalSize1 = 0;
                            for (Object size : sizes) {
                                totalSize1 += (Long) size;
                            }
                            listener.onComplete(Tasks.forResult(totalSize1));
                        }).addOnFailureListener(e -> listener.onComplete(Tasks.forException(e)));
                    })
                    .addOnFailureListener(e -> listener.onComplete(Tasks.forException(e)));
        }


        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            timeAfterDialogShowedUp = System.currentTimeMillis();
            //The number of required images will be predefined. The search filter will contain the subject that i need, and if there's a proper number of images in this
            //subject, then i don't have to insert them from firestore
            SQLiteHelper sqlHelper = new SQLiteHelper(SubjectSelect.this);

            //Данные не скачиваются, если уже скачаны

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            if (Common.isConnectedToInternet(getBaseContext())) {
                runOnUiThread(() -> {
                    dialogHorizontalProgressBar.setProgress(0);
                    dialogPercentageTV.setText("0%");
                    dialogFilesWeigthTV.setVisibility(View.GONE);
                    progress[0] = 0;
                    dialog.show();
                });

                //Проверка обновлений:
//                StatisticsSave("SQLiteVersion" + subject, 0);
                firestore.collection("DatabaseVersion").document(subject).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        firestoreVersion = (int) (long) documentSnapshot.get("databaseVersion");
                        //StatisticsSave("SQLiteVersion" + subject, 0);                                                //УБРАТЬ ЭТО ДЛЯ ВХОДА БЕЗ ЗАГРУЗКИ, ЕСЛИ ВСЕ УЖЕ СКАЧАНО
                        if (pref.getInt("SQLiteVersion" + subject, 0) == firestoreVersion) {
                            /*if (timeAfterDialogShowedUp + 300 > System.currentTimeMillis()) {
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }*/
                            dialogFilesWeigthTV.setVisibility(View.GONE);
                            dialogHorizontalProgressBar.setProgress(0);
                            dialog.dismiss();
                            wentToMainActivity = true;
                            Intent intent = new Intent(SubjectSelect.this, MainActivity.class);
                            intent.putExtra("loggedInManually", loggedInManually);
                            intent.putExtra("subject", subject);
                            startActivity(intent);
                            finish();
                        } else {
                            //sqlHelper.tableCleanup();
                            long[] totalSize = {0};
                            StorageReference[] storageRef = {FirebaseStorage.getInstance().getReference().child(subject + " Images").child("RandomFact")};
                            countImageWeights(storageRef[0], task -> {
                                if (task.isSuccessful()) {
                                    totalSize[0] += task.getResult();
                                    //Log.d("TAG", "Total image weight: " + totalSize[0]);
                                    storageRef[0] = FirebaseStorage.getInstance().getReference().child(subject + " Images").child("Scientists");
                                    countImageWeights(storageRef[0], task1 -> {
                                        totalSize[0] += task1.getResult();

                                        String[] mode = {"AllTopicsTraining", "GuessByImage", "TopicTraining", "TrueOrFalse"};
                                        for (int i = 0; i < mode.length; i++) {
                                            for (int j = 0; j < 40; j++) {
                                                firestore.collection("Questions").document(subject).collection(subject).document(mode[i]).collection("QuestionSet" + j).get().addOnSuccessListener(queryDocumentSnapshots -> {

                                                    for (DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots) {
                                                        Map<String, Object> data = documentSnapshot1.getData();
                                                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                                                            String key = entry.getKey();
                                                            Object value = entry.getValue();
                                                            if (value instanceof String) {
                                                                totalSize[0] += ((String) value).getBytes().length;
                                                            }
                                                            // add more cases for other types if needed
                                                        }
                                                    }

                                                    // totalSize now contains the size in bytes of all fields in the collection
                                                });
                                            }
                                        }

                                        firestore.collection("Scientists").document(subject).collection(subject).get().addOnSuccessListener(queryDocumentSnapshots -> {
                                            for (DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots) {
                                                Map<String, Object> data = documentSnapshot1.getData();
                                                for (Map.Entry<String, Object> entry : data.entrySet()) {
                                                    String key = entry.getKey();
                                                    Object value = entry.getValue();
                                                    if (value instanceof String) {
                                                        totalSize[0] += ((String) value).getBytes().length;
                                                    }
                                                    // add more cases for other types if needed
                                                }
                                            }
                                            dialogProgressBar.setVisibility(View.GONE);
                                            if (totalSize[0] < 1000)
                                                dialogFilesWeigthTV.setText("(" + totalSize[0] + getString(R.string.b) + ")");
                                            else if (totalSize[0] < 1000000)
                                                dialogFilesWeigthTV.setText("(" + totalSize[0] / 1000 + getString(R.string.kb) + ")");
                                            else if (totalSize[0] < 1000000000)
                                                dialogFilesWeigthTV.setText("(" + String.format("%.2f", (float) totalSize[0] / 1000000) + getString(R.string.mb) + ")");
                                            else if (totalSize[0] < 1000000000000L)
                                                dialogFilesWeigthTV.setText("(" + String.format("%.2f", (float) totalSize[0] / 1000000000) + getString(R.string.gb) + ")");
                                            dialogFilesWeigthTV.setVisibility(View.VISIBLE);
                                        });

                                        firestore.collection("Summary").document(subject).collection(subject).get().addOnSuccessListener(queryDocumentSnapshots -> {
                                            for (DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots) {
                                                Map<String, Object> data = documentSnapshot1.getData();
                                                for (Map.Entry<String, Object> entry : data.entrySet()) {
                                                    String key = entry.getKey();
                                                    Object value = entry.getValue();
                                                    if (value instanceof String) {
                                                        totalSize[0] += ((String) value).getBytes().length;

                                                    }
                                                    // add more cases for other types if needed
                                                }
                                            }
                                            dialogProgressBar.setVisibility(View.GONE);
                                            if (totalSize[0] < 1000)
                                                dialogFilesWeigthTV.setText("(" + totalSize[0] + getString(R.string.b) + ")");
                                            else if (totalSize[0] < 1000000)
                                                dialogFilesWeigthTV.setText("(" + totalSize[0] / 1000 + getString(R.string.kb) + ")");
                                            else if (totalSize[0] < 1000000000)
                                                dialogFilesWeigthTV.setText("(" + String.format("%.2f", (float) totalSize[0] / 1000000) + getString(R.string.mb) + ")");
                                            else if (totalSize[0] < 1000000000000L)
                                                dialogFilesWeigthTV.setText("(" + String.format("%.2f", (float) totalSize[0] / 1000000000) + getString(R.string.gb) + ")");
                                            dialogFilesWeigthTV.setVisibility(View.VISIBLE);
                                        });

                                    });
                                } else {
                                    Exception e = task.getException();
                                    Log.w("TAG", "Error counting image weight", e);
                                }
                            });
                            if (timeAfterDialogShowedUp + 300 > System.currentTimeMillis()) {
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            runOnUiThread(() -> {
                                dialogText.setVisibility(View.VISIBLE);
                                dialogYesButton.setVisibility(View.VISIBLE);
                                dialogNoButton.setVisibility(View.VISIBLE);
                                dialogLoadingProgressBarUpdateCheck.setVisibility(View.GONE);
                                dialogLoadingProgressBar.setVisibility(View.VISIBLE);
                                dialogTitleUpdateCheckTV.setVisibility(View.GONE);
                                dialogTitleLinearLayout.setVisibility(View.VISIBLE);
                                dialog.setCancelable(false);
                            });


                            cancelClicked = new boolean[]{false};
                            dialogCancelButton.setOnClickListener(view -> {
                                cancelClicked[0] = true;
                                dialogHorizontalProgressBar.setProgress(0); //из-за того, что это люто асинхронная херотень, после нажатия cancel все еще продолжают
                                //скачиваться файлы, несмотря на cancelClicked[0]
                                dialogFilesWeigthTV.setVisibility(View.GONE);
                                dialog.dismiss();
                            });
                            dialogNoButton.setOnClickListener(view -> {
                                dialogFilesWeigthTV.setVisibility(View.GONE);
                                dialog.dismiss();
                            });
                            dialogYesButton.setOnClickListener(view -> {
                                //Rebuild the xml file into horizontal loading bar using setVisibility's:
                                dialogText.setVisibility(View.GONE);
                                dialogHorizontalProgressRV.setVisibility(View.VISIBLE);
                                dialogYesButton.setVisibility(View.GONE);
                                dialogNoButton.setVisibility(View.GONE);
                                dialogCancelButton.setVisibility(View.VISIBLE);
                                dialogPercentageTV.setText("0%");


                                //Downloading the random fact images:
                                continueAfterNetworkError = new boolean[]{true};
                                wasNNDialogShown = new boolean[]{false};
                                firestore.collection("RandomFact").document(subject).collection(subject)
                                        .get().addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                documentNumber = task.getResult().size();
                                                dataMapRandFactImages = new HashMap<>();
                                                dataMapRandFactText = new HashMap<>();

                                                //CompletableFuture<Void> completableFuture = new CompletableFuture<>();
                                                randomFactDownload1();
                                                //completableFuture.join();

                                            } else {
                                                Log.d("document from firestore", "get failed with task", task.getException());
                                            }
                                        });
                            });
                        }
                    } else {
                        dialog.dismiss();
                        Toast.makeText(SubjectSelect.this, getString(R.string.subject_coming_soon), Toast.LENGTH_SHORT).show();
                    }
                });


            } else {
                if (pref.getInt("SQLiteVersion" + subject, 0) == 0) {
                    runOnUiThread(() -> {
                        Dialog noNetworkDialog = new Dialog(SubjectSelect.this);
                        noNetworkDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        noNetworkDialog.setContentView(R.layout.dialog_sqlite_upload_network_error);
                        noNetworkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        noNetworkDialog.setCancelable(true);
                        AppCompatButton noNetworkDialogOkButton = noNetworkDialog.findViewById(R.id.ok_button_dialog);
                        noNetworkDialogOkButton.setOnClickListener(view -> noNetworkDialog.dismiss());
                        dialogFilesWeigthTV.setVisibility(View.GONE);
                        dialogHorizontalProgressBar.setProgress(0);
                        dialog.dismiss();
                        noNetworkDialog.show();
                    });
                } else {
                    wentToMainActivity = true;
                    Intent intent = new Intent(SubjectSelect.this, MainActivity.class);
                    intent.putExtra("loggedInManually", loggedInManually);
                    intent.putExtra("subject", subject);
                    startActivity(intent);
                    finish();
                }
            }
        }

        private void randomFactDownload1() {
            int[] operationsCompleted = {0};
            for (int i = 0; i < documentNumber; i++) {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("RandomFact").document(subject).collection(subject)
                        .document("RandomFact" + (i + 1)).get().addOnSuccessListener(documentSnapshot -> {
                            String randKey = UUID.randomUUID().toString();
                            dataMapRandFactImages.put(randKey, documentSnapshot.get("image"));
                            dataMapRandFactText.put(randKey, documentSnapshot.get("text"));
                            operationsCompleted[0]++;
                            if (operationsCompleted[0] == documentNumber) {
                                randomFactDownload2();
                            }
                        });
            }
        }


        private void randomFactDownload2() {
            Set<Map.Entry<String, Object>> entrySet = dataMapRandFactImages.entrySet();
            Map.Entry<String, Object>[] entryArray1 = entrySet.toArray(new Map.Entry[entrySet.size()]);

            Set<Map.Entry<String, Object>> entrySetRandFactText = dataMapRandFactText.entrySet();
            Map.Entry<String, String>[] entryArrayRandFactText = entrySetRandFactText.toArray(new Map.Entry[entrySetRandFactText.size()]);
            for (int i = 0; i < entryArrayRandFactText.length; i++) {
                Log.d("RelevanceCheckEarlyONE", entryArrayRandFactText[i].getValue() + "\n" + entryArray1[i].getValue()); //gets fucked up
                Log.d("RelevanceCheckEarlyTWO", dataMapRandFactText.get("text" + (i + 1)) + "\n" + dataMapRandFactImages.get("image" + (i + 1))); //alright
            }
            //Map.Entry<String, Object>[] entryArrayFinal = new Map.Entry[entryArray1.length + entryArray2.length];
            //System.arraycopy(entryArray1, 0, entryArrayFinal, 0, entryArray1.length);
            //System.arraycopy(entryArray2, 0, entryArrayFinal, entryArray1.length, entryArray2.length);

            final float progressStep = (float) (((1 / (float) entryArray1.length) * 100) * 0.2);                        //20 процентов загрузки занимает random fact
            //final float progressStep = (1 / (float) entryArray.length) * 100;
            for (int i = 0; i < entryArray1.length; i++) {

                URL url = null;
                try {
                    url = new URL(entryArray1[i].getValue().toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                InputStream[] input = {null};
                URL finalUrl = url;
                final int finalI = i;
                //поток будто отстает от цикла while
                new Thread(() -> {

                    try {
                        input[0] = finalUrl.openStream();
                        // perform your network operation here
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while (true) {
                        try {
                            if ((bytesRead = input[0].read(buffer)) == -1)
                                break;
                        } catch (IOException | NullPointerException e) {
                            e.printStackTrace();
                            if (!Common.isConnectedToInternet(getBaseContext())) {
                                continueAfterNetworkError[0] = false;
                            }
                        }
                        if (continueAfterNetworkError[0])
                            output.write(buffer, 0, bytesRead);
                        else
                            break;
                    }
                    if (continueAfterNetworkError[0]) {
                        byte[] imageBytes = output.toByteArray();
                        try {
                            try {
                                input[0].close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } catch (NullPointerException ex) {    //not sure
                            ex.printStackTrace();
                        }
                        try {
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (!cancelClicked[0]) {
                            SQLiteHelper newSqlHelper = new SQLiteHelper(SubjectSelect.this);

                            newSqlHelper.insertDataRandomFact(entryArrayRandFactText[finalI].getValue(), imageBytes, subject);

                            progress[0] += progressStep;
                        }
                        //progress[0] = (finalI/entryArray.length)*100;

                        runOnUiThread(() -> {
                            if (!cancelClicked[0]) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    dialogHorizontalProgressBar.setProgress(Math.round(progress[0]), true);
                                } else
                                    dialogHorizontalProgressBar.setProgress(Math.round(progress[0]));
                                dialogPercentageTV.setText(Math.round(progress[0]) + "%");
                                if (Math.round(progress[0]) >= 100 && !wasProgress100ToGoToMain[0]) { //в конце загрузки версия sqlite обновляется. Немного ненадежно. Нужен другой способ определять конец загрузки всех файлов
                                    //... Не всегда файлы Scientists скачиваются в конце, поэтому работает через жопу. Можно сделать то же самое в нескольких других функциях тоже.
                                    wasProgress100ToGoToMain[0] = true;
                                    StatisticsSave("SQLiteVersion" + subject, firestoreVersion);
                                    Log.d("SQLiteVersion", "SQLite version " + subject + " saved. It's now: " + pref.getInt("SQLiteVersion" + subject, 0));
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        dialogHorizontalProgressBar.setProgress(100, true);
                                    } else
                                        dialogHorizontalProgressBar.setProgress(100);
                                    dialogPercentageTV.setText("100%");
                                    wentToMainActivity = true;
                                    Intent intent = new Intent(SubjectSelect.this, MainActivity.class);
                                    intent.putExtra("loggedInManually", loggedInManually);
                                    intent.putExtra("subject", subject);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            if (!wasNNDialogShown[0]) {
                                Dialog noNetworkDialog = new Dialog(SubjectSelect.this);
                                noNetworkDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                noNetworkDialog.setContentView(R.layout.dialog_sqlite_upload_network_error);
                                noNetworkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                noNetworkDialog.setCancelable(true);
                                AppCompatButton noNetworkDialogOkButton = noNetworkDialog.findViewById(R.id.ok_button_dialog);
                                noNetworkDialogOkButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        noNetworkDialog.dismiss();
                                    }
                                });
                                dialogFilesWeigthTV.setVisibility(View.GONE);
                                dialogHorizontalProgressBar.setProgress(0);
                                dialog.dismiss();
                                noNetworkDialog.show();
                                wasNNDialogShown[0] = true;
                            }
                        });
                    }
                }).start();
            }
            topicTrainingQuestionsDownload1();
            topicTrainingATQuestionsDownload1();
            trueOrFalseQuestionsDownload1();

        }

        private void topicTrainingQuestionsDownload1() {
            Log.d("dataPutTT", "topicTrainingQuestionDownload1() launched");
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            dataMapQuestion = new HashMap<>();
            dataMapOption1 = new HashMap<>();
            dataMapOption2 = new HashMap<>();
            dataMapOption3 = new HashMap<>();
            dataMapOption4 = new HashMap<>();
            dataMapAnswer = new HashMap<>();
            dataMapQuestionSet = new HashMap<>();


            firestore.collection("Questions").document(subject).collection(subject).document("TopicTraining").get().addOnSuccessListener(documentSnapshot -> {
                numQueriesTopicTraining[0] = (int) (long) documentSnapshot.get("NumberOfSets");
                TTQuestionsGetThread thread = new TTQuestionsGetThread();
                thread.start();
            });
        }


        private class TTQuestionsGetThread extends Thread {
            @Override
            public void run() {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                final float progressStep = (float) (((1 / (float) numQueriesTopicTraining[0]) * 100) * 0.1);
                CountDownLatch latch = new CountDownLatch(numQueriesTopicTraining[0]);

                for (int i = 0; i < numQueriesTopicTraining[0]; i++) {
                    int finalI = i;
                    firestore.collection("Questions").document(subject).collection(subject).document("TopicTraining")
                            .collection("QuestionSet" + (i + 1)).get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!cancelClicked[0]) {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                        if (documentSnapshot.exists()) {
                                            String randKey = UUID.randomUUID().toString();
                                            dataMapQuestion.put(randKey, documentSnapshot.get("question"));
                                            dataMapOption1.put(randKey, documentSnapshot.get("option1"));
                                            dataMapOption2.put(randKey, documentSnapshot.get("option2"));
                                            dataMapOption3.put(randKey, documentSnapshot.get("option3"));
                                            dataMapOption4.put(randKey, documentSnapshot.get("option4"));
                                            dataMapAnswer.put(randKey, documentSnapshot.get("answer"));
                                            dataMapQuestionSet.put(randKey, (finalI + 1));
                                            Log.d("dataPutTT", "data was put to maps");
                                        }
                                    }
                                    progress[0] += progressStep;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        dialogHorizontalProgressBar.setProgress(Math.round(progress[0]), true);
                                    } else
                                        dialogHorizontalProgressBar.setProgress(Math.round(progress[0]));
                                    dialogPercentageTV.setText(Math.round(progress[0]) + "%");
                                    if (Math.round(progress[0]) >= 100 && !wasProgress100ToGoToMain[0]) { //в конце загрузки версия sqlite обновляется. Немного ненадежно. Нужен другой способ определять конец загрузки всех файлов
                                        //... Не всегда файлы Scientists скачиваются в конце, поэтому работает через жопу. Можно сделать то же самое в нескольких других функциях тоже.
                                        wasProgress100ToGoToMain[0] = true;
                                        StatisticsSave("SQLiteVersion" + subject, firestoreVersion);
                                        Log.d("SQLiteVersion", "SQLite version " + subject + " saved. It's now: " + pref.getInt("SQLiteVersion" + subject, 0));
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            dialogHorizontalProgressBar.setProgress(100, true);
                                        } else
                                            dialogHorizontalProgressBar.setProgress(100);
                                        dialogPercentageTV.setText("100%");
                                        wentToMainActivity = true;
                                        Intent intent = new Intent(SubjectSelect.this, MainActivity.class);
                                        intent.putExtra("loggedInManually", loggedInManually);
                                        intent.putExtra("subject", subject);
                                        startActivity(intent);
                                        finish();
                                    }

                                    latch.countDown();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("getFailed", "Failed to retrieve data: " + e.getMessage());
                                latch.countDown();
                            });
                }

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!cancelClicked[0]) {
                    topicTrainingQuestionsDownload2();
                }
            }
        }


        private void topicTrainingQuestionsDownload2() {
            Log.d("dataPutTT", "topicTrainingQuestionDownload2() launched");
            SQLiteHelper sqLiteHelper = new SQLiteHelper(SubjectSelect.this);
            Set<Map.Entry<String, Object>> entrySetQuestion = dataMapQuestion.entrySet();
            Map.Entry<String, Object>[] entryArrayQuestion = entrySetQuestion.toArray(new Map.Entry[entrySetQuestion.size()]);

            Set<Map.Entry<String, Object>> entrySetOption1 = dataMapOption1.entrySet();
            Map.Entry<String, Object>[] entryArrayOption1 = entrySetOption1.toArray(new Map.Entry[entrySetOption1.size()]);

            Set<Map.Entry<String, Object>> entrySetOption2 = dataMapOption2.entrySet();
            Map.Entry<String, Object>[] entryArrayOption2 = entrySetOption2.toArray(new Map.Entry[entrySetOption2.size()]);

            Set<Map.Entry<String, Object>> entrySetOption3 = dataMapOption3.entrySet();
            Map.Entry<String, Object>[] entryArrayOption3 = entrySetOption3.toArray(new Map.Entry[entrySetOption3.size()]);

            Set<Map.Entry<String, Object>> entrySetOption4 = dataMapOption4.entrySet();
            Map.Entry<String, Object>[] entryArrayOption4 = entrySetOption4.toArray(new Map.Entry[entrySetOption4.size()]);

            Set<Map.Entry<String, Object>> entrySetAnswer = dataMapAnswer.entrySet();
            Map.Entry<String, Object>[] entryArrayAnswer = entrySetAnswer.toArray(new Map.Entry[entrySetAnswer.size()]);

            Set<Map.Entry<String, Integer>> entrySetQuestionSet = dataMapQuestionSet.entrySet();
            Map.Entry<String, Integer>[] entryArrayQuestionSet = entrySetQuestionSet.toArray(new Map.Entry[entrySetQuestionSet.size()]);

            Log.d("dataPutTT", "came close to the insert for loop");
            for (int i = 0; i < entryArrayQuestion.length; i++) {
                sqLiteHelper.insertDataTopicTraining((String) entryArrayQuestion[i].getValue(),
                        (String) entryArrayOption1[i].getValue(),
                        (String) entryArrayOption2[i].getValue(),
                        (String) entryArrayOption3[i].getValue(),
                        (String) entryArrayOption4[i].getValue(), (String) entryArrayAnswer[i].getValue(), entryArrayQuestionSet[i].getValue(), subject);
                Log.d("dataPutTT", "Data inserted");
            }


        }


        private void topicTrainingATQuestionsDownload1() {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            dataMapQuestionAT = new HashMap<>();
            dataMapOption1AT = new HashMap<>();
            dataMapOption2AT = new HashMap<>();
            dataMapOption3AT = new HashMap<>();
            dataMapOption4AT = new HashMap<>();
            dataMapAnswerAT = new HashMap<>();
            dataMapQuestionSetAT = new HashMap<>();


            firestore.collection("Questions").document(subject).collection(subject).document("AllTopicsTraining").get().addOnSuccessListener(documentSnapshot -> {
                numQueriesAllTopicsTraining[0] = (int) (long) documentSnapshot.get("NumberOfSets");
                TTATQuestionsGetThread thread = new TTATQuestionsGetThread();
                thread.start();
            });
        }


        private class TTATQuestionsGetThread extends Thread {
            @Override
            public void run() {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                final float progressStepAT = (float) (((1 / (float) numQueriesAllTopicsTraining[0]) * 100) * 0.1);
                CountDownLatch latch = new CountDownLatch(numQueriesAllTopicsTraining[0]);

                int[] counter = {0};
                for (int i = 0; i < numQueriesAllTopicsTraining[0]; i++) {
                    int finalI = i;
                    firestore.collection("Questions").document(subject).collection(subject).document("AllTopicsTraining")
                            .collection("QuestionSet" + (i + 1)).get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if (!cancelClicked[0]) {
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                            if (documentSnapshot.exists()) {
                                                String randKey = UUID.randomUUID().toString();
                                                dataMapQuestionAT.put(randKey, documentSnapshot.get("question"));
                                                dataMapOption1AT.put(randKey, documentSnapshot.get("option1"));
                                                dataMapOption2AT.put(randKey, documentSnapshot.get("option2"));
                                                dataMapOption3AT.put(randKey, documentSnapshot.get("option3"));
                                                dataMapOption4AT.put(randKey, documentSnapshot.get("option4"));
                                                dataMapAnswerAT.put(randKey, documentSnapshot.get("answer"));
                                                dataMapQuestionSetAT.put(randKey, (finalI + 1));
                                                counter[0]++;
                                            }
                                        }

                                        progress[0] += progressStepAT;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            dialogHorizontalProgressBar.setProgress(Math.round(progress[0]), true);
                                        } else
                                            dialogHorizontalProgressBar.setProgress(Math.round(progress[0]));
                                        dialogPercentageTV.setText(Math.round(progress[0]) + "%");
                                        if (Math.round(progress[0]) >= 100 && !wasProgress100ToGoToMain[0]) { //в конце загрузки версия sqlite обновляется. Немного ненадежно. Нужен другой способ определять конец загрузки всех файлов
                                            //... Не всегда файлы Scientists скачиваются в конце, поэтому работает через жопу. Можно сделать то же самое в нескольких других функциях тоже.
                                            wasProgress100ToGoToMain[0] = true;
                                            StatisticsSave("SQLiteVersion" + subject, firestoreVersion);
                                            Log.d("SQLiteVersion", "SQLite version " + subject + " saved. It's now: " + pref.getInt("SQLiteVersion" + subject, 0));
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                dialogHorizontalProgressBar.setProgress(100, true);
                                            } else
                                                dialogHorizontalProgressBar.setProgress(100);
                                            dialogPercentageTV.setText("100%");
                                            wentToMainActivity = true;
                                            Intent intent = new Intent(SubjectSelect.this, MainActivity.class);
                                            intent.putExtra("loggedInManually", loggedInManually);
                                            intent.putExtra("subject", subject);
                                            startActivity(intent);
                                            finish();
                                        }

                                        latch.countDown();
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("getFailed", "Failed to retrieve data: " + e.getMessage());
                                latch.countDown();
                            });
                }

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!cancelClicked[0]) {
                    Log.d("counterTTTAT", valueOf(counter[0])); //size is correct, there are 200 questions in the maps. Something goes wrong in the insert part
                    topicTrainingATQuestionsDownload2();
                }
            }
        }


        private void topicTrainingATQuestionsDownload2() {
            SQLiteHelper sqLiteHelper = new SQLiteHelper(SubjectSelect.this);
            Set<Map.Entry<String, Object>> entrySetQuestion = dataMapQuestionAT.entrySet();
            Map.Entry<String, Object>[] entryArrayQuestion = entrySetQuestion.toArray(new Map.Entry[entrySetQuestion.size()]);

            Set<Map.Entry<String, Object>> entrySetOption1 = dataMapOption1AT.entrySet();
            Map.Entry<String, Object>[] entryArrayOption1 = entrySetOption1.toArray(new Map.Entry[entrySetOption1.size()]);

            Set<Map.Entry<String, Object>> entrySetOption2 = dataMapOption2AT.entrySet();
            Map.Entry<String, Object>[] entryArrayOption2 = entrySetOption2.toArray(new Map.Entry[entrySetOption2.size()]);

            Set<Map.Entry<String, Object>> entrySetOption3 = dataMapOption3AT.entrySet();
            Map.Entry<String, Object>[] entryArrayOption3 = entrySetOption3.toArray(new Map.Entry[entrySetOption3.size()]);

            Set<Map.Entry<String, Object>> entrySetOption4 = dataMapOption4AT.entrySet();
            Map.Entry<String, Object>[] entryArrayOption4 = entrySetOption4.toArray(new Map.Entry[entrySetOption4.size()]);

            Set<Map.Entry<String, Object>> entrySetAnswer = dataMapAnswerAT.entrySet();
            Map.Entry<String, Object>[] entryArrayAnswer = entrySetAnswer.toArray(new Map.Entry[entrySetAnswer.size()]);

            Set<Map.Entry<String, Integer>> entrySetQuestionSet = dataMapQuestionSetAT.entrySet();
            Map.Entry<String, Integer>[] entryArrayQuestionSet = entrySetQuestionSet.toArray(new Map.Entry[entrySetQuestionSet.size()]);

            for (int i = 0; i < entryArrayQuestion.length; i++) {
                sqLiteHelper.insertDataAllTopicsTraining((String) entryArrayQuestion[i].getValue(),
                        (String) entryArrayOption1[i].getValue(),
                        (String) entryArrayOption2[i].getValue(),
                        (String) entryArrayOption3[i].getValue(),
                        (String) entryArrayOption4[i].getValue(), (String) entryArrayAnswer[i].getValue(), entryArrayQuestionSet[i].getValue(), subject);
            }
            /*sqLiteHelper.insertDataAllTopicsTraining(entryArrayQuestion.length, entryArrayQuestion, entryArrayOption1, entryArrayOption2, entryArrayOption3,
                    entryArrayOption4, entryArrayAnswer, entryArrayQuestionSet, subject);*/
        } //в сетах 9, 18 и 19 не хватает вопросов


        private void trueOrFalseQuestionsDownload1() {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            dataMapQuestionTOF = new HashMap<>();
            dataMapAnswerTOF = new HashMap<>();
            dataMapRevealAnswerTOF = new HashMap<>();
            dataMapQuestionSetTOF = new HashMap<>();


            firestore.collection("Questions").document(subject).collection(subject).document("TrueOrFalse").get().addOnSuccessListener(documentSnapshot -> {
                numQueriesTrueOrFalse[0] = (int) (long) documentSnapshot.get("NumberOfSets");
                TrueOrFalseQuestionGetThread thread = new TrueOrFalseQuestionGetThread();
                thread.start();
            });
        }


        private class TrueOrFalseQuestionGetThread extends Thread {
            @Override
            public void run() {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance(); //нужно все firestore'ы инициализировать одной private переменной в начале onCreate()

                final float progressStepTrueOrFalse = (float) (((1 / (float) numQueriesTrueOrFalse[0]) * 100) * 0.1);
                CountDownLatch latch = new CountDownLatch(numQueriesTrueOrFalse[0]);

                int[] counter = {0};
                for (int i = 0; i < numQueriesTrueOrFalse[0]; i++) {
                    int finalI = i;
                    firestore.collection("Questions").document(subject).collection(subject).document("TrueOrFalse")
                            .collection("QuestionSet" + (i + 1)).get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!cancelClicked[0]) {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                        if (documentSnapshot.exists()) {
                                            String randKey = UUID.randomUUID().toString();
                                            dataMapQuestionTOF.put(randKey, documentSnapshot.get("question"));
                                            dataMapAnswerTOF.put(randKey, documentSnapshot.get("answer"));
                                            dataMapRevealAnswerTOF.put(randKey, documentSnapshot.get("revealAnswer"));
                                            dataMapQuestionSetTOF.put(randKey, (finalI + 1));
                                            counter[0]++;
                                        }
                                    }

                                    progress[0] += progressStepTrueOrFalse;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        dialogHorizontalProgressBar.setProgress(Math.round(progress[0]), true);
                                    } else
                                        dialogHorizontalProgressBar.setProgress(Math.round(progress[0]));
                                    dialogPercentageTV.setText(Math.round(progress[0]) + "%");
                                    if (Math.round(progress[0]) >= 100 && !wasProgress100ToGoToMain[0]) { //в конце загрузки версия sqlite обновляется. Немного ненадежно. Нужен другой способ определять конец загрузки всех файлов
                                        //... Не всегда файлы Scientists скачиваются в конце, поэтому работает через жопу. Можно сделать то же самое в нескольких других функциях тоже.
                                        wasProgress100ToGoToMain[0] = true;
                                        StatisticsSave("SQLiteVersion" + subject, firestoreVersion);
                                        Log.d("SQLiteVersion", "SQLite version " + subject + " saved. It's now: " + pref.getInt("SQLiteVersion" + subject, 0));
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            dialogHorizontalProgressBar.setProgress(100, true);
                                        } else
                                            dialogHorizontalProgressBar.setProgress(100);
                                        dialogPercentageTV.setText("100%");
                                        wentToMainActivity = true;
                                        Intent intent = new Intent(SubjectSelect.this, MainActivity.class);
                                        intent.putExtra("loggedInManually", loggedInManually);
                                        intent.putExtra("subject", subject);
                                        startActivity(intent);
                                        finish();
                                    }

                                    latch.countDown();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("getFailed", "Failed to retrieve data: " + e.getMessage());
                                    latch.countDown();
                                }
                            });
                }

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!cancelClicked[0]) {
                    trueOrFalseQuestionsDownload2();
                }
            }
        }


        private void trueOrFalseQuestionsDownload2() {
            SQLiteHelper sqLiteHelper = new SQLiteHelper(SubjectSelect.this);
            Set<Map.Entry<String, Object>> entrySetQuestion = dataMapQuestionTOF.entrySet();
            Map.Entry<String, Object>[] entryArrayQuestion = entrySetQuestion.toArray(new Map.Entry[entrySetQuestion.size()]);

            Set<Map.Entry<String, Object>> entrySetAnswer = dataMapAnswerTOF.entrySet();
            Map.Entry<String, Object>[] entryArrayAnswer = entrySetAnswer.toArray(new Map.Entry[entrySetAnswer.size()]);

            Set<Map.Entry<String, Object>> entrySetRevealAnswer = dataMapRevealAnswerTOF.entrySet();
            Map.Entry<String, Object>[] entryArrayRevealAnswer = entrySetRevealAnswer.toArray(new Map.Entry[entrySetRevealAnswer.size()]);

            Set<Map.Entry<String, Integer>> entrySetQuestionSet = dataMapQuestionSetTOF.entrySet();
            Map.Entry<String, Integer>[] entryArrayQuestionSet = entrySetQuestionSet.toArray(new Map.Entry[entrySetQuestionSet.size()]);

            for (int i = 0; i < entryArrayQuestion.length; i++) {
                if (entryArrayAnswer.length != 0 && entryArrayRevealAnswer.length != 0 && entryArrayQuestionSet.length != 0) {
                    sqLiteHelper.insertDataTrueOrFalse((String) entryArrayQuestion[i].getValue(),
                            (String) entryArrayAnswer[i].getValue(),
                            (String) entryArrayRevealAnswer[i].getValue(),
                            entryArrayQuestionSet[i].getValue(), subject);
                } else {
                    Log.e("rrrttt", "One or more elements in the array are null");
                }
            }
            GuessByImageQuestionsDownload1();
        }


        private void GuessByImageQuestionsDownload1() {
            if (subject.equals("Philosophy")) {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                dataMapQuestionGBI = new HashMap<>();
                dataMapOption1GBI = new HashMap<>();
                dataMapOption2GBI = new HashMap<>();
                dataMapOption3GBI = new HashMap<>();
                dataMapOption4GBI = new HashMap<>();
                dataMapAnswerGBI = new HashMap<>();
                dataMapQuestionSetGBI = new HashMap<>();


                firestore.collection("Questions").document(subject).collection(subject).document("GuessByImage").get().addOnSuccessListener(documentSnapshot -> {
                    numQueriesGuessByImage[0] = (int) (long) documentSnapshot.get("NumberOfSets");
                    GuessByImageQuestionsGetThread thread = new GuessByImageQuestionsGetThread();
                    thread.start();
                });
            } else {
                progress[0] += 40; //including guess by image and scientists section
                summaryDownload();
            }
        }


        private class GuessByImageQuestionsGetThread extends Thread {
            @Override
            public void run() {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                final float progressStepGuessByImage = (float) (((1 / (float) numQueriesGuessByImage[0]) * 100) * 0.15);
                CountDownLatch latch = new CountDownLatch(numQueriesGuessByImage[0]);

                int[] counter = {0};
                for (int i = 0; i < numQueriesGuessByImage[0]; i++) {
                    int finalI = i;
                    firestore.collection("Questions").document(subject).collection(subject).document("GuessByImage")
                            .collection("QuestionSet" + (i + 1)).get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if (!cancelClicked[0]) {
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                            if (documentSnapshot.exists()) {
                                                String randKey = UUID.randomUUID().toString();
                                                dataMapQuestionGBI.put(randKey, documentSnapshot.get("question"));
                                                dataMapOption1GBI.put(randKey, documentSnapshot.get("option1"));
                                                dataMapOption2GBI.put(randKey, documentSnapshot.get("option2"));
                                                dataMapOption3GBI.put(randKey, documentSnapshot.get("option3"));
                                                dataMapOption4GBI.put(randKey, documentSnapshot.get("option4"));
                                                dataMapAnswerGBI.put(randKey, documentSnapshot.get("answer"));
                                                dataMapQuestionSetGBI.put(randKey, (finalI + 1));
                                                counter[0]++;
                                            }
                                        }
                                        progress[0] += progressStepGuessByImage;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            dialogHorizontalProgressBar.setProgress(Math.round(progress[0]), true);
                                        } else
                                            dialogHorizontalProgressBar.setProgress(Math.round(progress[0]));
                                        dialogPercentageTV.setText(Math.round(progress[0]) + "%");
                                        if (Math.round(progress[0]) >= 100 && !wasProgress100ToGoToMain[0]) { //в конце загрузки версия sqlite обновляется. Немного ненадежно. Нужен другой способ определять конец загрузки всех файлов
                                            //... Не всегда файлы Scientists скачиваются в конце, поэтому работает через жопу. Можно сделать то же самое в нескольких других функциях тоже.
                                            wasProgress100ToGoToMain[0] = true;
                                            StatisticsSave("SQLiteVersion" + subject, firestoreVersion);
                                            Log.d("SQLiteVersion", "SQLite version " + subject + " saved. It's now: " + pref.getInt("SQLiteVersion" + subject, 0));
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                dialogHorizontalProgressBar.setProgress(100, true);
                                            } else
                                                dialogHorizontalProgressBar.setProgress(100);
                                            dialogPercentageTV.setText("100%");
                                            wentToMainActivity = true;
                                            Intent intent = new Intent(SubjectSelect.this, MainActivity.class);
                                            intent.putExtra("loggedInManually", loggedInManually);
                                            intent.putExtra("subject", subject);
                                            startActivity(intent);
                                            finish();
                                        }

                                        latch.countDown();
                                    }
                                }
                            })
                            .addOnFailureListener(e -> latch.countDown());
                }

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!cancelClicked[0]) {
                    GuessByImageQuestionsDownload2();
                }
            }
        }


        private void GuessByImageQuestionsDownload2() {
            SQLiteHelper sqLiteHelper = new SQLiteHelper(SubjectSelect.this);
            Set<Map.Entry<String, Object>> entrySetQuestion = dataMapQuestionGBI.entrySet();
            Map.Entry<String, Object>[] entryArrayQuestion = entrySetQuestion.toArray(new Map.Entry[entrySetQuestion.size()]);

            Set<Map.Entry<String, Object>> entrySetOption1 = dataMapOption1GBI.entrySet();
            Map.Entry<String, Object>[] entryArrayOption1 = entrySetOption1.toArray(new Map.Entry[entrySetOption1.size()]);

            Set<Map.Entry<String, Object>> entrySetOption2 = dataMapOption2GBI.entrySet();
            Map.Entry<String, Object>[] entryArrayOption2 = entrySetOption2.toArray(new Map.Entry[entrySetOption2.size()]);

            Set<Map.Entry<String, Object>> entrySetOption3 = dataMapOption3GBI.entrySet();
            Map.Entry<String, Object>[] entryArrayOption3 = entrySetOption3.toArray(new Map.Entry[entrySetOption3.size()]);

            Set<Map.Entry<String, Object>> entrySetOption4 = dataMapOption4GBI.entrySet();
            Map.Entry<String, Object>[] entryArrayOption4 = entrySetOption4.toArray(new Map.Entry[entrySetOption4.size()]);

            Set<Map.Entry<String, Object>> entrySetAnswer = dataMapAnswerGBI.entrySet();
            Map.Entry<String, Object>[] entryArrayAnswer = entrySetAnswer.toArray(new Map.Entry[entrySetAnswer.size()]);

            Set<Map.Entry<String, Integer>> entrySetQuestionSet = dataMapQuestionSetGBI.entrySet();
            Map.Entry<String, Integer>[] entryArrayQuestionSet = entrySetQuestionSet.toArray(new Map.Entry[entrySetQuestionSet.size()]);

            final float progressStepGuessByImage = (float) (((1 / (float) numQueriesGuessByImage[0]) * 100) * 0.15) / (entryArrayQuestion.length / (float) numQueriesGuessByImage[0]);

            for (int i = 0; i < entryArrayQuestion.length; i++) {
                //url to byte[]:
                URL url = null;
                try {
                    url = new URL(entryArrayQuestion[i].getValue().toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                InputStream[] input = {null};
                URL finalUrl = url;
                final int finalI = i;
                new Thread(() -> {

                    try {
                        input[0] = finalUrl.openStream();
                        // perform your network operation here
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while (true) {
                        try {
                            if ((bytesRead = input[0].read(buffer)) == -1)
                                break;
                        } catch (IOException | NullPointerException e) {        //Call no internet dialog from here
                            e.printStackTrace();
                            if (!Common.isConnectedToInternet(getBaseContext())) {
                                continueAfterNetworkError[0] = false;
                            }
                        }
                        if (continueAfterNetworkError[0])
                            output.write(buffer, 0, bytesRead);
                        else
                            break;
                    }
                    if (continueAfterNetworkError[0]) {
                        byte[] imageBytes = output.toByteArray();
                        try {
                            try {
                                input[0].close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } catch (NullPointerException ex) {    //not sure
                            ex.printStackTrace();
                        }
                        try {
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (!cancelClicked[0]) {
                            SQLiteHelper newSqlHelper = new SQLiteHelper(SubjectSelect.this);
                            newSqlHelper.insertDataGuessByImage(imageBytes, (String) entryArrayOption1[finalI].getValue(), (String) entryArrayOption2[finalI].getValue(),
                                    (String) entryArrayOption3[finalI].getValue(), (String) entryArrayOption4[finalI].getValue(), (String) entryArrayAnswer[finalI].getValue(),
                                    entryArrayQuestionSet[finalI].getValue(), subject);

                            progress[0] += progressStepGuessByImage;
                        }
                        //progress[0] = (finalI/entryArray.length)*100;

                        runOnUiThread(() -> {
                            if (!cancelClicked[0]) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    dialogHorizontalProgressBar.setProgress(Math.round(progress[0]), true);
                                } else
                                    dialogHorizontalProgressBar.setProgress(Math.round(progress[0]));
                                dialogPercentageTV.setText(Math.round(progress[0]) + "%");
                                if (Math.round(progress[0]) >= 100 && !wasProgress100ToGoToMain[0]) { //в конце загрузки версия sqlite обновляется. Немного ненадежно. Нужен другой способ определять конец загрузки всех файлов
                                    //... Не всегда файлы Scientists скачиваются в конце, поэтому работает через жопу. Можно сделать то же самое в нескольких других функциях тоже.
                                    wasProgress100ToGoToMain[0] = true;
                                    StatisticsSave("SQLiteVersion" + subject, firestoreVersion);
                                    Log.d("SQLiteVersion", "SQLite version " + subject + " saved. It's now: " + pref.getInt("SQLiteVersion" + subject, 0));
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        dialogHorizontalProgressBar.setProgress(100, true);
                                    } else
                                        dialogHorizontalProgressBar.setProgress(100);
                                    dialogPercentageTV.setText("100%");
                                    wentToMainActivity = true;
                                    Intent intent = new Intent(SubjectSelect.this, MainActivity.class);
                                    intent.putExtra("loggedInManually", loggedInManually);
                                    intent.putExtra("subject", subject);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            if (!wasNNDialogShown[0]) {
                                Dialog noNetworkDialog = new Dialog(SubjectSelect.this);
                                noNetworkDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                noNetworkDialog.setContentView(R.layout.dialog_sqlite_upload_network_error);
                                noNetworkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                noNetworkDialog.setCancelable(true);
                                AppCompatButton noNetworkDialogOkButton = noNetworkDialog.findViewById(R.id.ok_button_dialog);
                                noNetworkDialogOkButton.setOnClickListener(view -> noNetworkDialog.dismiss());
                                dialogFilesWeigthTV.setVisibility(View.GONE);
                                dialogHorizontalProgressBar.setProgress(0);
                                dialog.dismiss();
                                noNetworkDialog.show();
                                wasNNDialogShown[0] = true;
                            }
                        });
                    }
                }).start();
            }
            summaryDownload();
        }


        private void summaryDownload() {
            Map<String, Object> dataMapTitle = new HashMap<>();
            Map<String, Object> dataMapContent = new HashMap<>();

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("Summary").document(subject).collection(subject).get().addOnSuccessListener(queryDocumentSnapshots -> {
                SQLiteHelper sqLiteHelper = new SQLiteHelper(SubjectSelect.this);
                final float progressStepSummary = (float) (((1 / (float) queryDocumentSnapshots.size()) * 100) * 0.1);
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    String randKey = UUID.randomUUID().toString();
                    dataMapTitle.put(randKey, documentSnapshot.get("title"));
                    dataMapContent.put(randKey, documentSnapshot.get("content"));
                    sqLiteHelper.insertDataSummary((String) dataMapTitle.get(randKey), (String) dataMapContent.get(randKey), subject);
                    progress[0] += progressStepSummary;
                    runOnUiThread(() -> {
                        if (!cancelClicked[0]) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                dialogHorizontalProgressBar.setProgress(Math.round(progress[0]), true);
                            } else
                                dialogHorizontalProgressBar.setProgress(Math.round(progress[0]));
                            dialogPercentageTV.setText(Math.round(progress[0]) + "%");
                            if (Math.round(progress[0]) >= 100 && !wasProgress100ToGoToMain[0]) { //в конце загрузки версия sqlite обновляется. Немного ненадежно. Нужен другой способ определять конец загрузки всех файлов
                                //... Не всегда файлы Scientists скачиваются в конце, поэтому работает через жопу. Можно сделать то же самое в нескольких других функциях тоже.
                                wasProgress100ToGoToMain[0] = true;
                                StatisticsSave("SQLiteVersion" + subject, firestoreVersion);
                                Log.d("SQLiteVersion", "SQLite version " + subject + " saved. It's now: " + pref.getInt("SQLiteVersion" + subject, 0));
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    dialogHorizontalProgressBar.setProgress(100, true);
                                } else
                                    dialogHorizontalProgressBar.setProgress(100);
                                dialogPercentageTV.setText("100%");
                                wentToMainActivity = true;
                                Intent intent = new Intent(SubjectSelect.this, MainActivity.class);
                                intent.putExtra("loggedInManually", loggedInManually);
                                intent.putExtra("subject", subject);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
            });

            Map<String, Object> dataMapSpanSearchWords = new HashMap<>();
            firestore.collection("Summary").document(subject).collection("SpanSearchWords").document("SpanSearchWords").get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    SQLiteHelper sqLiteHelper = new SQLiteHelper(SubjectSelect.this);
                    for (int i = 1; i <= documentSnapshot.getData().size(); i++) {
                        sqLiteHelper.insertDataSummarySpanSearchWords((String) documentSnapshot.get("searchWord" + i), subject);
                    }
                }
            });

            if (subject.equals("Philosophy"))
                scientistsDownload();
        }


        private void scientistsDownload() {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("Scientists").document(subject).collection(subject).get().addOnSuccessListener(queryDocumentSnapshots -> {
                final float progressStepScientists = (float) (((1 / (float) queryDocumentSnapshots.size()) * 100) * 0.1);
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    String randKey = UUID.randomUUID().toString();
                    //url to byte[]
                    URL url = null;
                    try {
                        url = new URL((String) documentSnapshot.get("picture"));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    InputStream[] input = {null};
                    URL finalUrl = url;
                    //поток будто отстает от цикла while
                    new Thread(() -> {

                        try {
                            input[0] = finalUrl.openStream();
                            // perform your network operation here
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int bytesRead = 0;
                        while (true) {
                            try {
                                if ((bytesRead = input[0].read(buffer)) == -1)
                                    break;
                            } catch (IOException | NullPointerException e) {        //Call no internet dialog from here
                                e.printStackTrace();
                                if (!Common.isConnectedToInternet(getBaseContext())) {
                                    continueAfterNetworkError[0] = false;
                                }
                            }
                            if (continueAfterNetworkError[0])
                                output.write(buffer, 0, bytesRead);
                            else
                                break;
                        }
                        if (continueAfterNetworkError[0]) {
                            byte[] imageBytes = output.toByteArray();
                            try {
                                try {
                                    input[0].close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (NullPointerException ex) {    //not sure
                                ex.printStackTrace();
                            }
                            try {
                                output.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (!cancelClicked[0]) {
                                SQLiteHelper newSqlHelper = new SQLiteHelper(SubjectSelect.this);
                                newSqlHelper.insertDataScientists(imageBytes, (String) documentSnapshot.get("title"), (String) documentSnapshot.get("content"), subject);
                                progress[0] += progressStepScientists;
                            }
                            //progress[0] = (finalI/entryArray.length)*100;

                            runOnUiThread(() -> {
                                if (!cancelClicked[0]) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        dialogHorizontalProgressBar.setProgress(Math.round(progress[0]), true);
                                    } else
                                        dialogHorizontalProgressBar.setProgress(Math.round(progress[0]));
                                    dialogPercentageTV.setText(Math.round(progress[0]) + "%");
                                    Log.d("fffgggg", valueOf(Math.round(progress[0])));
                                    if (Math.round(progress[0]) >= 100 && !wasProgress100ToGoToMain[0]) { //в конце загрузки версия sqlite обновляется. Немного ненадежно. Нужен другой способ определять конец загрузки всех файлов
                                        //... Не всегда файлы Scientists скачиваются в конце, поэтому работает через жопу. Можно сделать то же самое в нескольких других функциях тоже.
                                        wasProgress100ToGoToMain[0] = true;
                                        StatisticsSave("SQLiteVersion" + subject, firestoreVersion);
                                        Log.d("SQLiteVersion", "SQLite version " + subject + " saved. It's now: " + pref.getInt("SQLiteVersion" + subject, 0));
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            dialogHorizontalProgressBar.setProgress(100, true);
                                        } else
                                            dialogHorizontalProgressBar.setProgress(100);
                                        dialogPercentageTV.setText("100%");
                                        wentToMainActivity = true;
                                        Intent intent = new Intent(SubjectSelect.this, MainActivity.class);
                                        intent.putExtra("loggedInManually", loggedInManually);
                                        intent.putExtra("subject", subject);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        } else {
                            runOnUiThread(() -> {
                                if (!wasNNDialogShown[0]) {
                                    Dialog noNetworkDialog = new Dialog(SubjectSelect.this);
                                    noNetworkDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    noNetworkDialog.setContentView(R.layout.dialog_sqlite_upload_network_error);
                                    noNetworkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    noNetworkDialog.setCancelable(true);
                                    AppCompatButton noNetworkDialogOkButton = noNetworkDialog.findViewById(R.id.ok_button_dialog);
                                    noNetworkDialogOkButton.setOnClickListener(view -> noNetworkDialog.dismiss());
                                    dialogFilesWeigthTV.setVisibility(View.GONE);
                                    dialogHorizontalProgressBar.setProgress(0);
                                    dialog.dismiss();
                                    noNetworkDialog.show();
                                    wasNNDialogShown[0] = true;
                                }
                            });
                        }
                    }).start();
                }
            });

        }

    }
}
