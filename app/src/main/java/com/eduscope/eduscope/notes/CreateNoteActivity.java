package com.eduscope.eduscope.notes;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.eduscope.eduscope.R;
import com.eduscope.eduscope.notifications.ReminderBroadcastReceiver;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText noteTitle, noteContent;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    private ImageView moreBtn;
    private String noteColorCode = "#cdc9c3";
    private final String[] colorCodes = {"#E7E7E7", "#BBFFBE", "#D0FFA6", "#efbbcf", "#FFB6B6", "#D7E9FF", "#ADFFDB", "#ffe0ac", "#FFC9B8"};
    private boolean willBeSaved = true;
    private FrameLayout colorIndicator;
    private SharedPreferences pref, defPref;
    private long appSessionStart;
    private String timeInAppKey = "TimeInApp";
    private CalendarDialog dialogCalendar;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private String keeper = "";
    private FloatingActionButton editFloatingBtn;
    private LottieAnimationView floatingBtnPulseLottie;
    private boolean earlyEndOfVoiceRecognitionAvailable = false;
    private final String voiceRecognitionBtnKey = "voiceRecognitionButtonShown";
    private PopupMenu[] popupMenu = new PopupMenu[1];
    private boolean wasSaved = false;
    private DocumentReference savedDocumentReference;
    private SearchView searchView;
    private RelativeLayout searchLayout;
    private ImageView searchCancelCrossImage;
    private String docIdStr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        searchView = findViewById(R.id.search_view);
        searchLayout = findViewById(R.id.search_layout);
        searchCancelCrossImage = findViewById(R.id.search_cancel_cross_image);
        noteTitle = findViewById(R.id.create_note_title);
        noteContent = findViewById(R.id.create_note_content);

        editFloatingBtn = findViewById(R.id.edit_note_floating_button);

        pref = getSharedPreferences("Statistics", Context.MODE_PRIVATE);
        floatingBtnPulseLottie = findViewById(R.id.floating_button_pulse);
        defPref = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences def_pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (def_pref.getBoolean("theme_switch_preference", false)) {
            noteTitle.setTextColor(getResources().getColor(R.color.light_text));
            noteTitle.setBackground(getResources().getDrawable(R.drawable.round_back_dark_10));
            noteTitle.setHintTextColor(getResources().getColor(R.color.dark_grey));
            noteContent.setHintTextColor(getResources().getColor(R.color.dark_grey));
            noteContent.setTextColor(getResources().getColor(R.color.light_text));
            noteContent.setBackgroundColor(getResources().getColor(R.color.dark_theme));
        }

        if (!pref.getBoolean(voiceRecognitionBtnKey, true))
            editFloatingBtn.setVisibility(View.GONE);

        colorIndicator = findViewById(R.id.color_indicator);

        Random random = new Random();
        noteColorCode = colorCodes[random.nextInt(colorCodes.length)]; //add simultaneous change of indicator's color
        ColorCodeFindHex(noteColorCode);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        moreBtn = findViewById(R.id.more_button_editnote);

        Dialog colorSelectDialog = new Dialog(CreateNoteActivity.this);
        colorSelectDialog.setCanceledOnTouchOutside(true);
        colorSelectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        colorSelectDialog.setContentView(R.layout.dialog_note_color_select);
        colorSelectDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        colorSelectDialog.setCancelable(true);

        FrameLayout grey = colorSelectDialog.findViewById(R.id.grey);
        FrameLayout green = colorSelectDialog.findViewById(R.id.green);
        FrameLayout lightgreen = colorSelectDialog.findViewById(R.id.lightgreen);
        FrameLayout pink = colorSelectDialog.findViewById(R.id.pink);
        FrameLayout red = colorSelectDialog.findViewById(R.id.red);
        FrameLayout skyblue = colorSelectDialog.findViewById(R.id.skyblue);
        FrameLayout turquoise = colorSelectDialog.findViewById(R.id.turquoise);
        FrameLayout yellow = colorSelectDialog.findViewById(R.id.yellow);
        FrameLayout orange = colorSelectDialog.findViewById(R.id.orange);

        grey.setOnClickListener(view1 -> {
            ColorCodeFind("grey");
            colorSelectDialog.dismiss();
        });
        green.setOnClickListener(view1 -> {
            ColorCodeFind("green");
            colorSelectDialog.dismiss();
        });
        lightgreen.setOnClickListener(view1 -> {
            ColorCodeFind("lightgreen");
            colorSelectDialog.dismiss();
        });
        pink.setOnClickListener(view1 -> {
            ColorCodeFind("pink");
            colorSelectDialog.dismiss();
        });
        red.setOnClickListener(view1 -> {
            ColorCodeFind("red");
            colorSelectDialog.dismiss();
        });
        skyblue.setOnClickListener(view1 -> {
            ColorCodeFind("skyblue");
            colorSelectDialog.dismiss();
        });
        turquoise.setOnClickListener(view1 -> {
            ColorCodeFind("turquoise");
            colorSelectDialog.dismiss();
        });
        yellow.setOnClickListener(view1 -> {
            ColorCodeFind("yellow");
            colorSelectDialog.dismiss();
        });
        orange.setOnClickListener(view1 -> {
            ColorCodeFind("orange");
            colorSelectDialog.dismiss();
        });

        colorIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorSelectDialog.show();
            }
        });


        initializeSpeechRecognizer();

        editFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if (!earlyEndOfVoiceRecognitionAvailable) {
                        if (ContextCompat.checkSelfPermission(CreateNoteActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(CreateNoteActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                        }
                        if (speechRecognizer != null) {
                            speechRecognizer.startListening(speechRecognizerIntent);
                            floatingBtnPulseLottie.playAnimation();
                            floatingBtnPulseLottie.setVisibility(View.VISIBLE);
                        }
                        keeper = "";
                        earlyEndOfVoiceRecognitionAvailable = true;
                    } else {
                        floatingBtnPulseLottie.setVisibility(View.GONE);
                        floatingBtnPulseLottie.cancelAnimation();
                        if (speechRecognizer != null) {
                            speechRecognizer.stopListening();
                            speechRecognizer.destroy();
                            earlyEndOfVoiceRecognitionAvailable = false;
                            initializeSpeechRecognizer();
                        }
                    }
                }
        });


        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenuInit(view);
                popupMenu[0].show();
            }
        });

        TextView navigationTV = findViewById(R.id.navigation_tv);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Not needed in this case
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Remove all existing spans
                SpannableString spannableString = new SpannableString(noteContent.getText());
                ForegroundColorSpan[] existingSpans = spannableString.getSpans(0, spannableString.length(), ForegroundColorSpan.class);
                for (ForegroundColorSpan span : existingSpans) {
                    spannableString.removeSpan(span);
                }

                if (!newText.isEmpty()) {
                    // Highlight the snippets matching the search query
                    String escapedQuery = Pattern.quote(newText);
                    Pattern pattern = Pattern.compile(escapedQuery, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(noteContent.getText());
                    int currentSnippet = 0;
                    while (matcher.find()) {
                        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE),
                                matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        currentSnippet++;
                    }
                    navigationTV.setText(getString(R.string.found) + " " + String.valueOf(currentSnippet));
                } else {
                    navigationTV.setText(getString(R.string.found) + " " + "0");
                }

                noteContent.setText(spannableString);
                return true;
            }
        });

        searchCancelCrossImage = findViewById(R.id.search_cancel_cross_image);
        searchCancelCrossImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setQuery("", false);
                searchLayout.setVisibility(View.GONE);
            }
        });

        if (defPref.getBoolean("theme_switch_preference", false)) {
            noteTitle.setBackgroundResource(R.drawable.round_back_dark_10);
            noteTitle.setTextColor(getResources().getColor(R.color.light_text));
            //noteContent.setBackgroundColor(getResources().getColor(R.color.dark_theme));
            noteContent.setTextColor(getResources().getColor(R.color.light_text));
            RelativeLayout mainRL = findViewById(R.id.mainRelativeLayout);
            mainRL.setBackgroundColor(getResources().getColor(R.color.dark_theme));
        }
        noteContent.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
    }


    private void popupMenuInit(View view) {
        popupMenu[0] = new PopupMenu(view.getContext(), view);
        popupMenu[0].getMenu().add(getString(R.string.search)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                searchLayout.setVisibility(View.VISIBLE);
                searchView.setIconified(false);
                searchView.requestFocus();
                return false;
            }
        });
        popupMenu[0].getMenu().add(getString(R.string.set_reminder)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (noteContent.getText().toString().trim().isEmpty()) {
                    Toast.makeText(CreateNoteActivity.this, getString(R.string.reminder_content_cannot_be_empty), Toast.LENGTH_LONG).show();
                    return false;
                }
                Intent intent = new Intent(CreateNoteActivity.this, ReminderBroadcastReceiver.class);
                intent.putExtra("title", noteTitle.getText().toString()); // Pass the note title as an extra
                intent.putExtra("content", noteContent.getText().toString()); // Pass the note content as an extra
                dialogCalendar = new CalendarDialog(noteTitle.getText().toString(), noteContent.getText().toString());
                dialogCalendar.show(getSupportFragmentManager(), "CalendarDialog");
                dialogCalendar.setCancelable(true);
                return true;
            }
        });
        popupMenu[0].getMenu().add(getString(R.string.share)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                String noteTitleStr = noteTitle.getText().toString();
                String noteContentStr = noteContent.getText().toString();

                if (noteTitleStr.trim().isEmpty() && noteContentStr.trim().isEmpty()) {
                    Toast.makeText(CreateNoteActivity.this, getString(R.string.note_cannot_be_empty), Toast.LENGTH_SHORT).show();
                    return false;
                }
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                if (noteTitleStr.trim().isEmpty())
                    sendIntent.putExtra(Intent.EXTRA_TEXT, noteContentStr);
                else
                    sendIntent.putExtra(Intent.EXTRA_TEXT, noteTitleStr + "\n\n" + noteContentStr);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share note"));
                return true;
            }
        });
        String[] hideVoiceRecognitionBtnPopup = new String[1];
        if (pref.getBoolean(voiceRecognitionBtnKey, true)) {
            hideVoiceRecognitionBtnPopup[0] = getString(R.string.hide_voice_recognition_button);
        } else {
            hideVoiceRecognitionBtnPopup[0] = getString(R.string.show_voice_recognition_button);
        }
        popupMenu[0].getMenu().add(hideVoiceRecognitionBtnPopup[0]).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (pref.getBoolean(voiceRecognitionBtnKey, true)) {
                    StatisticsSaveBoolean(voiceRecognitionBtnKey, false);
                    hideVoiceRecognitionBtnPopup[0] = getString(R.string.show_voice_recognition_button);
                    editFloatingBtn.setVisibility(View.GONE);
                    popupMenuInit(view);
                    return true;
                }
                hideVoiceRecognitionBtnPopup[0] = getString(R.string.hide_voice_recognition_button);
                editFloatingBtn.setVisibility(View.VISIBLE);
                StatisticsSaveBoolean(voiceRecognitionBtnKey, true);
                popupMenuInit(view);
                return true;
            }
        });
        popupMenu[0].getMenu().add(getString(R.string.delete_edit_note)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
                builder.setMessage(getString(R.string.are_you_sure_delete_note))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                            willBeSaved = false;
                            if (wasSaved) {
                                savedDocumentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(CreateNoteActivity.this, getString(R.string.note_deleted), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            onBackPressed();
                        })
                        .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel()).show();
                return true;
            }
        });
    }


    public void StatisticsSaveBoolean(String key, boolean value) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean(key, value);
        edit.apply();
    }



    @Override
    protected void onStop() {
        if (willBeSaved) {
            NoteSaveDBThread thread = new NoteSaveDBThread();
            thread.start();
            wasSaved = true;
        }
        super.onStop();
    }


    class NoteSaveDBThread extends Thread {
        @Override
        public void run() {
            String title = noteTitle.getText().toString().trim();
            String content = noteContent.getText().toString().trim();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd  HH:mm.ss", Locale.getDefault());
            String date = sdf.format(new Date());
            if (!title.isEmpty() || !content.isEmpty()) {
                if (title.isEmpty()) {
                    SimpleDateFormat sdfTitle = new SimpleDateFormat("yyyy.MM.dd / HH:mm", Locale.getDefault());
                    title = sdfTitle.format(new Date());
                }
                DocumentReference documentReference;
                if (docIdStr == null) {
                    documentReference = firestore.collection("Notes")
                            .document(FirebaseAuth.getInstance().getUid()).collection("UserNotes").document();
                } else {
                    documentReference = firestore.collection("Notes")
                            .document(FirebaseAuth.getInstance().getUid()).collection("UserNotes").document(docIdStr);
                }

                Map<String, Object> note = new HashMap<>();
                note.put("title", title);
                note.put("content", content);
                note.put("color", noteColorCode);
                note.put("date", date);
                documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(CreateNoteActivity.this, getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
                        savedDocumentReference = documentReference;
                        docIdStr = documentReference.getId();
                    }
                });
            }
        }
    }

    private void ColorCodeFind(String color) { //попробовать по hex коду устанавливать цвет, если это возможно, так хотя бы можно нормально сохранять в базу данных
        //стринговое значение, а не int
        String[] colors = {"grey", "green", "lightgreen", "pink", "red", "skyblue", "turquoise", "yellow", "orange"};
        Drawable[] colorCodesDrawables = {getDrawable(R.drawable.note_roundback_grey_10), getDrawable(R.drawable.note_roundback_green_10), getDrawable(R.drawable.note_roundback_lightgreen_10),
                getDrawable(R.drawable.note_roundback_pink_10), getDrawable(R.drawable.note_roundback_red_10), getDrawable(R.drawable.note_roundback_skyblue_10),
                getDrawable(R.drawable.note_roundback_turquoise_10), getDrawable(R.drawable.note_roundback_yellow_10), getDrawable(R.drawable.notes_orange)};
        Drawable noteDrawable = getDrawable(R.drawable.note_roundback_grey_10);
        for (int i = 0; i < 9; i++) {
            if (color.equals(colors[i])) {
                noteColorCode = colorCodes[i];
                noteDrawable = colorCodesDrawables[i];
            }
        }
        colorIndicator.setBackground(noteDrawable);
    }

    public void ColorCodeFindHex(String hex) {
        String[] colorCodes = {"#E7E7E7", "#BBFFBE", "#D0FFA6", "#efbbcf", "#FFB6B6", "#D7E9FF", "#ADFFDB", "#ffe0ac", "#FFC9B8"};
        Drawable[] colorCodesDrawables = {getDrawable(R.drawable.note_roundback_grey_10), getDrawable(R.drawable.note_roundback_green_10), getDrawable(R.drawable.note_roundback_lightgreen_10),
                getDrawable(R.drawable.note_roundback_pink_10), getDrawable(R.drawable.note_roundback_red_10), getDrawable(R.drawable.note_roundback_skyblue_10),
                getDrawable(R.drawable.note_roundback_turquoise_10), getDrawable(R.drawable.note_roundback_yellow_10), getDrawable(R.drawable.notes_orange)};
        Drawable noteDrawable = getDrawable(R.drawable.note_roundback_grey_10);
        for (int i = 0; i < 9; i++) {
            if (hex.equals(colorCodes[i])) {
                noteColorCode = colorCodes[i];
                noteDrawable = colorCodesDrawables[i];
            }
        }
        colorIndicator.setBackground(noteDrawable);
    }


    public void StatisticsSaveLong(String key, long value) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putLong(key, value);
        edit.apply();
    }


    @Override
    protected void onResume() {
        appSessionStart = System.currentTimeMillis();
        super.onResume();
    }

    @Override
    protected void onPause() {
        StatisticsSaveLong(timeInAppKey, pref.getLong(timeInAppKey, 0) + (System.currentTimeMillis() - appSessionStart));
        super.onPause();
    }


    private void initializeSpeechRecognizer() {
        Log.d("isRecognitionAvailable", String.valueOf(SpeechRecognizer.isRecognitionAvailable(this)));
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            String languagePreference = defPref.getString("voice_recognition_language_preference", getString(R.string.default_language));
            if (languagePreference.equals(getString(R.string.default_language)))
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            else if (languagePreference.equals(getString(R.string.russian)))
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
            else if (languagePreference.equals(getString(R.string.english)))
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle bundle) {
                }

                @Override
                public void onBeginningOfSpeech() {
                }

                @Override
                public void onRmsChanged(float v) {
                }

                @Override
                public void onBufferReceived(byte[] bytes) {
                }

                @Override
                public void onEndOfSpeech() {
                    floatingBtnPulseLottie.setVisibility(View.GONE);
                    earlyEndOfVoiceRecognitionAvailable = false;
                }

                @Override
                public void onError(int i) {
                    floatingBtnPulseLottie.setVisibility(View.GONE);
                    earlyEndOfVoiceRecognitionAvailable = false;
                }

                @Override
                public void onResults(Bundle bundle) {
                    ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (data != null) {
                        keeper = data.get(0);
                        if (noteContent.getText().toString().isEmpty()) {
                            noteContent.setText(keeper);
                            noteContent.setSelection(keeper.length());
                        } else {
                            Log.d("selectionEnd1", "getSelectionEnd returned " + noteContent.getSelectionEnd());
                            if (noteContent.getSelectionEnd() == noteContent.getText().length()) {
                                if (noteContent.getText().toString().charAt(noteContent.getText().toString().length() - 1) != ' ' &&
                                        noteContent.getText().toString().charAt(noteContent.getText().toString().length() - 1) != '\n' && !keeper.substring(0, 1).matches("[^a-zA-Z0-9]"))
                                    noteContent.setText(noteContent.getText().toString() + " " + keeper);
                                else
                                    noteContent.setText(noteContent.getText().toString() + keeper);
                                noteContent.setSelection(noteContent.getText().toString().length());
                            } else if (noteContent.getSelectionEnd() == 0) {
                                if (noteContent.getText().toString().charAt(0) != ' ' &&
                                        noteContent.getText().toString().charAt(0) != '\n' && !noteContent.toString().substring(0, 1).matches("[^a-zA-Z0-9]"))
                                    noteContent.setText(keeper + " " + noteContent.getText().toString());
                                else
                                    noteContent.setText(keeper + noteContent.getText().toString());
                                noteContent.setSelection(keeper.length());
                            } else {
                                String subContent1 = noteContent.getText().toString().substring(0, noteContent.getSelectionEnd());
                                String subContent2 = noteContent.getText().toString().substring(noteContent.getSelectionEnd());
                                if ((subContent1.charAt(subContent1.length() - 1) == ' ' || subContent1.charAt(subContent1.length() - 1) == '\n')
                                        && (subContent2.charAt(0) == ' ' || subContent2.charAt(0) == '\n' || subContent2.substring(0, 1).matches("[^a-zA-Z0-9]"))) {
                                    noteContent.setText(subContent1 + keeper + subContent2);
                                    noteContent.setSelection(subContent1.length() + keeper.length());
                                } else if (subContent1.charAt(subContent1.length() - 1) == ' ' || subContent1.charAt(subContent1.length() - 1) == '\n') {
                                    noteContent.setText(subContent1 + keeper + " " + subContent2);
                                    noteContent.setSelection(subContent1.length() + keeper.length());
                                } else if (subContent2.charAt(0) == ' ' || subContent2.charAt(0) == '\n') {
                                    noteContent.setText(subContent1 + " " + keeper + subContent2);
                                    noteContent.setSelection(subContent1.length() + 1 + keeper.length());
                                } else {
                                    noteContent.setText(subContent1 + " " + keeper + " " + subContent2);
                                    noteContent.setSelection(subContent1.length() + 1 + keeper.length());
                                }
                            }
                        }
                    }
                }

                @Override
                public void onPartialResults(Bundle bundle) {
                }

                @Override
                public void onEvent(int i, Bundle bundle) {
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result != null) {
                        if (noteContent.getText().toString().isEmpty()) {
                            noteContent.setText(keeper);
                            noteContent.setSelection(keeper.length());
                        } else {
                            Log.d("selectionEnd1", "getSelectionEnd returned " + noteContent.getSelectionEnd());
                            if (noteContent.getSelectionEnd() == noteContent.getText().length()) {
                                if (noteContent.getText().toString().charAt(noteContent.getText().toString().length() - 1) != ' ' &&
                                        noteContent.getText().toString().charAt(noteContent.getText().toString().length() - 1) != '\n' && !keeper.substring(0, 1).matches("[^a-zA-Z0-9]"))
                                    noteContent.setText(noteContent.getText().toString() + " " + keeper);
                                else
                                    noteContent.setText(noteContent.getText().toString() + keeper);
                                noteContent.setSelection(noteContent.getText().toString().length());
                            } else if (noteContent.getSelectionEnd() == 0) {
                                if (noteContent.getText().toString().charAt(0) != ' ' &&
                                        noteContent.getText().toString().charAt(0) != '\n' && !noteContent.toString().substring(0, 1).matches("[^a-zA-Z0-9]"))
                                    noteContent.setText(keeper + " " + noteContent.getText().toString());
                                else
                                    noteContent.setText(keeper + noteContent.getText().toString());
                                noteContent.setSelection(keeper.length());
                            } else {
                                String subContent1 = noteContent.getText().toString().substring(0, noteContent.getSelectionEnd());
                                String subContent2 = noteContent.getText().toString().substring(noteContent.getSelectionEnd());
                                if ((subContent1.charAt(subContent1.length() - 1) == ' ' || subContent1.charAt(subContent1.length() - 1) == '\n')
                                        && (subContent2.charAt(0) == ' ' || subContent2.charAt(0) == '\n' || subContent2.substring(0, 1).matches("[^a-zA-Z0-9]"))) {
                                    noteContent.setText(subContent1 + keeper + subContent2);
                                    noteContent.setSelection(subContent1.length() + keeper.length());
                                } else if (subContent1.charAt(subContent1.length() - 1) == ' ' || subContent1.charAt(subContent1.length() - 1) == '\n') {
                                    noteContent.setText(subContent1 + keeper + " " + subContent2);
                                    noteContent.setSelection(subContent1.length() + keeper.length());
                                } else if (subContent2.charAt(0) == ' ' || subContent2.charAt(0) == '\n') {
                                    noteContent.setText(subContent1 + " " + keeper + subContent2);
                                    noteContent.setSelection(subContent1.length() + 1 + keeper.length());
                                } else {
                                    noteContent.setText(subContent1 + " " + keeper + " " + subContent2);
                                    noteContent.setSelection(subContent1.length() + 1 + keeper.length());
                                }
                            }
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize the speech recognizer
                initializeSpeechRecognizer();
            } else {
                // Permission denied, inform the user and disable the voice input button
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                editFloatingBtn.setEnabled(false);
            }
        }
    }
}