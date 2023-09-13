package com.eduscope.eduscope.login;

import android.content.IntentFilter;
import android.graphics.Color;
import android.icu.lang.UCharacter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.eduscope.eduscope.R;
import com.eduscope.eduscope.internet_connection.NetworkChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private AppCompatButton signUpButton;
    private EditText name, aftername, email, password, password2;
    private ProgressBar loading;
    private AutoCompleteTextView eduinstListAutoComplete;
    private ArrayAdapter<String> eduinstAdapter;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        loading = findViewById(R.id.loading_progress_bar);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        signUpButton = findViewById(R.id.signup_button);

        name = findViewById(R.id.login_username);
        aftername = findViewById(R.id.login_username2);
        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        password2 = findViewById(R.id.login_password2);

        name.setOnClickListener(view -> signUpButton.setText(getString(R.string.sign_up_button)));
        aftername.setOnClickListener(view -> signUpButton.setText(getString(R.string.sign_up_button)));
        email.setOnClickListener(view -> signUpButton.setText(getString(R.string.sign_up_button)));
        password.setOnClickListener(view -> signUpButton.setText(getString(R.string.sign_up_button)));
        password2.setOnClickListener(view -> signUpButton.setText(getString(R.string.sign_up_button)));

        TextView backToLoginClickable = findViewById(R.id.back_to_login_clickable);
        Spannable forgotPasswordSpannable = new SpannableString(backToLoginClickable.getText().toString());
        ClickableSpan clickSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(Color.WHITE);
            }

            @Override
            public void onClick(@NonNull View view) {
                onBackPressed();
            }
        };

        forgotPasswordSpannable.setSpan(clickSpan, 0, getString(R.string.to_previous_screen).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        backToLoginClickable.setText(forgotPasswordSpannable, TextView.BufferType.SPANNABLE);
        backToLoginClickable.setMovementMethod(LinkMovementMethod.getInstance());

        //Добавить проверку, соответствует ли то, что пользователь ввел, одному из реальных университетов!!!!!
        loading.setVisibility(View.VISIBLE);
        signUpButton.setText("");
        List<String>[] eduinstList = new List[]{new ArrayList<>()};
        firestore.collection("EducationalInstitutions").document("EducationalInstitutionList").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //DocumentSnapshot document = task.getResult(); //compiler says it's never used
                Map<String, Object> tempMap = task.getResult().getData();
                if (tempMap != null) {
                    for (Map.Entry<String, Object> entry : tempMap.entrySet()) {
                        eduinstList[0].add((String) entry.getValue());
                    }
                }
            }
            loading.setVisibility(View.GONE);
            signUpButton.setText(getString(R.string.sign_up_button));
            eduinstListAutoComplete = findViewById(R.id.autocomplete_eduinst);
            eduinstAdapter = new ArrayAdapter<>(SignUpActivity.this, R.layout.simple_spinner_dropdown_item);
            eduinstAdapter.addAll(eduinstList[0]);
            eduinstListAutoComplete.setAdapter(eduinstAdapter);


            signUpButton.setOnClickListener(view -> {
                String nameStr = name.getText().toString().trim(),
                        afternameStr = aftername.getText().toString().trim(),
                        eduinstStr = eduinstListAutoComplete.getText().toString().trim(),
                        emailStr = email.getText().toString().trim(),
                        passwordStr = password.getText().toString().trim(),
                        password2Str = password2.getText().toString().trim();
                if (nameStr.isEmpty() || afternameStr.isEmpty() || emailStr.isEmpty() || passwordStr.isEmpty() || password2Str.isEmpty())
                    signUpButton.setText(getString(R.string.all_fields_required));
                else {
                    if (!passwordStr.equals(password2Str))
                        signUpButton.setText(getString(R.string.passwords_not_same));
                    else {
                        //Limiting name and aftername lenghts to 32 symbols each:
                        if (nameStr.length() > 32 || afternameStr.length() > 32) {
                            signUpButton.setText(getString(R.string.name_and_aftername_length_limit));
                        } else {
                            //Finding special symbols in name and aftername:
                            boolean[] nameAfternameSpecialSymbols = {false, false};
                            for (int i = 0; i < nameStr.length(); i++) {
                                if (!Character.isLetter(nameStr.charAt(i)) && nameStr.charAt(i) != ' ')
                                    nameAfternameSpecialSymbols[0] = true;
                            }
                            for (int i = 0; i < afternameStr.length(); i++) {
                                if (!Character.isLetter(afternameStr.charAt(i)) && afternameStr.charAt(i) != ' ')
                                    nameAfternameSpecialSymbols[1] = true;
                            }
                            if (nameAfternameSpecialSymbols[0] && nameAfternameSpecialSymbols[1])
                                signUpButton.setText(getString(R.string.name_and_aftername_special_symbols_error));
                            else if (nameAfternameSpecialSymbols[0])
                                signUpButton.setText(getString(R.string.name_special_symbols_error));
                            else if (nameAfternameSpecialSymbols[1])
                                signUpButton.setText(getString(R.string.aftername_special_symbols_error));
                            else {

                                //Cuss words check:
                                signUpButton.setText("");
                                loading.setVisibility(View.VISIBLE);
                                boolean[] curseWordsFound = {false, false};
                                signUpButton.setText("");
                                loading.setVisibility(View.VISIBLE);
                                firestore.collection("CurseWords").document("CurseWordList").get().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        DocumentSnapshot document = task1.getResult();
                                        List<String> curseWordList = new ArrayList<>();
                                        Map<String, Object> map = document.getData();
                                        if (map != null) {
                                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                                curseWordList.add(entry.getValue().toString());
                                            }
                                            for (int i = 0; i < curseWordList.size(); i++) {
                                                int nameCurseWordBeginIndex = nameStr.toLowerCase().indexOf(curseWordList.get(i));
                                                int afternameCurseWordBeginIndex = afternameStr.toLowerCase().indexOf(curseWordList.get(i));
                                                int nameCurseWordEndIndex = nameCurseWordBeginIndex + curseWordList.get(i).length();
                                                int afternameCurseWordEndIndex = afternameCurseWordBeginIndex + curseWordList.get(i).length();
                                                if (nameCurseWordBeginIndex != -1) {
                                                    if (nameCurseWordBeginIndex == 0 && nameCurseWordEndIndex == nameStr.length())
                                                        curseWordsFound[0] = true;
                                                    else if (nameCurseWordBeginIndex == 0) {
                                                        if (nameStr.charAt(nameCurseWordEndIndex) == ' ')
                                                            curseWordsFound[0] = true;
                                                    } else if (nameCurseWordEndIndex == nameStr.length()) {
                                                        if (nameStr.charAt(nameCurseWordBeginIndex - 1) == ' ')
                                                            curseWordsFound[0] = true;
                                                    } else if (nameCurseWordEndIndex + 1 <= nameStr.length()) //case where both previous and next symbol after the word are spaces
                                                        if (nameStr.charAt(nameCurseWordBeginIndex - 1) == ' ' && nameStr.charAt(nameCurseWordBeginIndex + curseWordList.get(i).length()) == ' ')
                                                            curseWordsFound[0] = true;
                                                }

                                                if (afternameCurseWordBeginIndex != -1) {
                                                    if (afternameCurseWordBeginIndex == 0 && afternameCurseWordEndIndex == afternameStr.length())
                                                        curseWordsFound[1] = true;
                                                    else if (afternameCurseWordBeginIndex == 0) {
                                                        if (afternameStr.charAt(afternameCurseWordEndIndex) == ' ')
                                                            curseWordsFound[1] = true;
                                                    } else if (afternameCurseWordEndIndex == afternameStr.length()) {
                                                        if (afternameStr.charAt(afternameCurseWordBeginIndex - 1) == ' ')
                                                            curseWordsFound[1] = true;
                                                    } else if (afternameCurseWordEndIndex + 1 <= afternameStr.length()) //case where both previous and next symbol after the word are spaces
                                                        if (afternameStr.charAt(afternameCurseWordBeginIndex - 1) == ' ' && afternameStr.charAt(afternameCurseWordBeginIndex + curseWordList.get(i).length()) == ' ')
                                                            curseWordsFound[1] = true;
                                                }
                                            }
                                            if (curseWordsFound[0] && curseWordsFound[1]) {
                                                signUpButton.setText(getString(R.string.curse_words_in_name_and_aftername));
                                                loading.setVisibility(View.GONE);
                                            } else if (curseWordsFound[0]) {
                                                signUpButton.setText(getString(R.string.curse_words_in_name));
                                                loading.setVisibility(View.GONE);
                                            } else if (curseWordsFound[1]) {
                                                signUpButton.setText(getString(R.string.curse_words_in_aftername));
                                                loading.setVisibility(View.GONE);
                                            }

                                        }
                                    }

                                    if (!curseWordsFound[0] && !curseWordsFound[1]) {
                                        //Password check:
                                        boolean passwordCyrillicFound = false;
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                            passwordCyrillicFound = passwordStr.chars()
                                                    .mapToObj(UCharacter.UnicodeBlock::of)
                                                    .anyMatch(b -> b.equals(UCharacter.UnicodeBlock.CYRILLIC));
                                        }

                                        boolean passwordNoNumbersFound = true;
                                        for (int i = 0; i < passwordStr.length(); i++) {
                                            if (Character.isDigit(passwordStr.charAt(i))) {
                                                passwordNoNumbersFound = false;
                                                break;
                                            }
                                        }

                                        boolean passwordDoesntContainLetters = true;
                                        for (int i = 0; i < passwordStr.length(); i++) {
                                            if (Character.isLetter(passwordStr.charAt(i))) {
                                                passwordDoesntContainLetters = false;
                                                break;
                                            }
                                        }

                                        boolean passwordWrongLength = passwordStr.length() < 7;

                                        if (passwordCyrillicFound || passwordNoNumbersFound || passwordDoesntContainLetters || passwordWrongLength) {
                                            loading.setVisibility(View.GONE);
                                            signUpButton.setText(getString(R.string.password_restrictions));
                                        } else {
                                            signUpButton.setText("");
                                            //Works pretty slow, so that further code starts working without isLoginTaken change

                                            //Email availability check:
                                            boolean[] isEmailTaken = {false};
                                            firestore.collection("Users").get().addOnCompleteListener(task11 -> {
                                                if (task11.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task11.getResult()) {
                                                        if (Objects.equals(document.get("email"), emailStr)) {
                                                            isEmailTaken[0] = true;
                                                            loading.setVisibility(View.GONE);
                                                            signUpButton.setText(getString(R.string.email_taken));
                                                        }
                                                    }
                                                }

                                                if (!isEmailTaken[0]) {
                                                    boolean[] isEduinstRight = {false};
                                                    firestore.collection("EducationalInstitutions").document("EducationalInstitutionList")
                                                            .get().addOnCompleteListener(task111 -> {
                                                                if (task111.isSuccessful()) {
                                                                    if (!eduinstStr.isEmpty()) {
                                                                        //DocumentSnapshot document = task111.getResult(); //compiler says it's never used
                                                                        Map<String, Object> tempMap = task111.getResult().getData();
                                                                        if (tempMap != null) {
                                                                            for (Map.Entry<String, Object> entry : tempMap.entrySet()) {
                                                                                if (Objects.equals(entry.getValue(), eduinstStr))
                                                                                    isEduinstRight[0] = true;
                                                                            }
                                                                        }
                                                                    } else
                                                                        isEduinstRight[0] = true;
                                                                }
                                                                if (!isEduinstRight[0]) {
                                                                    loading.setVisibility(View.GONE);
                                                                    signUpButton.setText(getString(R.string.eduinst_entered_wrong));
                                                                } else {
                                                                    if (!isEmailTaken[0]) {
                                                                        firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(task1111 -> {
                                                                            if (task1111.isSuccessful()) {
                                                                                User user = new User(nameStr, afternameStr, emailStr, eduinstStr, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                                                                                firestore.collection("Users").document(FirebaseAuth.getInstance().getUid()).set(user).addOnSuccessListener(documentReference -> sendEmailVerification())
                                                                                        .addOnFailureListener(e -> {
                                                                                            signUpButton.setText(getString(R.string.signup_error));
                                                                                            loading.setVisibility(View.GONE);
                                                                                        });
                                                                            } else {
                                                                                signUpButton.setText(getString(R.string.error_sending_verification_mail));
                                                                                loading.setVisibility(View.GONE);
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    }

                    //Registration data uploading should begin if login isn't taken

                }
            });
        });


    }


    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }


    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }


    private void sendEmailVerification() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
                signUpButton.setText(getString(R.string.confirm_sent));
                //add the password recovery email sent animation
                firebaseAuth.signOut();
                loading.setVisibility(View.GONE);
            });
        } else {
            signUpButton.setText(getString(R.string.error_sending_verification_mail));
            loading.setVisibility(View.GONE);
        }
    }

}