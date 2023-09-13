package com.eduscope.eduscope.login;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;

import com.eduscope.eduscope.R;
import com.eduscope.eduscope.SubjectSelect;
import com.eduscope.eduscope.internet_connection.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailET, passwordET;
    private AppCompatButton loginBtn;
    private FirebaseAuth firebaseAuth;
    private ProgressBar loading;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            Intent autoIntent = new Intent(LoginActivity.this, SubjectSelect.class);
            autoIntent.putExtra("loggedInManually", false);
            startActivity(autoIntent);
            finish();
        }

        firebaseAuth = FirebaseAuth.getInstance();
        loading = findViewById(R.id.loading_progress_bar);

        emailET = findViewById(R.id.login_email);
        passwordET = findViewById(R.id.login_password);
        loginBtn = findViewById(R.id.login_button);

        emailET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loading.getVisibility() != View.VISIBLE)
                    loginBtn.setText(getString(R.string.login_button));
            }
        });
        passwordET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loading.getVisibility() != View.VISIBLE)
                    loginBtn.setText(getString(R.string.login_button));
            }
        });


        loginBtn.setOnClickListener(view -> {
            loginBtn.setText("");
            loading.setVisibility(View.VISIBLE);
            String emailStr = emailET.getText().toString().trim();
            String passwordStr = passwordET.getText().toString().trim();
            if (emailStr.isEmpty() || passwordStr.isEmpty()) {
                loading.setVisibility(View.GONE);
                loginBtn.setText(getString(R.string.all_fields_required));
            }
            else {
                //Signing the user in:
                firebaseAuth.signInWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                Intent intent = new Intent(LoginActivity.this, SubjectSelect.class);
                                intent.putExtra("loggedInManually", true);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        loading.setVisibility(View.GONE);
                                        loginBtn.setText(getString(R.string.login_confirm_email));
                                    }
                                });
                            }
                        }
                    } else {
                        loading.setVisibility(View.GONE);
                        loginBtn.setText(getString(R.string.login_error));
                    }
                });

            }
        });


        TextView forgotPasswordClickable = findViewById(R.id.forgot_password_clickable);
        Spannable forgotPasswordSpannable = new SpannableString(forgotPasswordClickable.getText().toString());
        ClickableSpan clickSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(Color.WHITE);
            }

            @Override
            public void onClick(@NonNull View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        };
        forgotPasswordSpannable.setSpan(clickSpan, 0, getString(R.string.forgot_password).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        forgotPasswordClickable.setText(forgotPasswordSpannable, TextView.BufferType.SPANNABLE);
        forgotPasswordClickable.setMovementMethod(LinkMovementMethod.getInstance());


        TextView signUpClickable = findViewById(R.id.sign_up_clickable);
        Spannable signUpSpannable = new SpannableString(signUpClickable.getText().toString());
        ClickableSpan clickSpanSignUp = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(Color.WHITE);
            }

            @Override
            public void onClick(@NonNull View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        };
        int beginIndex = signUpClickable.getText().toString().indexOf(getString(R.string.sign_up));
        signUpSpannable.setSpan(clickSpanSignUp, beginIndex, beginIndex + getString(R.string.sign_up).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        signUpClickable.setText(signUpSpannable, TextView.BufferType.SPANNABLE);
        signUpClickable.setMovementMethod(LinkMovementMethod.getInstance());

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
}
