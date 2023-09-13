package com.eduscope.eduscope.login;

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
import androidx.appcompat.widget.AppCompatButton;

import com.airbnb.lottie.LottieAnimationView;
import com.eduscope.eduscope.R;
import com.eduscope.eduscope.internet_connection.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;


public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private AppCompatButton recoverPasswordBtn;
    private LottieAnimationView mailSentAnim, lockAnim;
    private ProgressBar loading;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        loading = findViewById(R.id.loading_progress_bar);
        emailEditText = findViewById(R.id.login_email);
        mailSentAnim = findViewById(R.id.mail_sent_lottie_anim);
        lockAnim = findViewById(R.id.lock_lottie_anim);
        lockAnim.setSpeed(1.5f);
        lockAnim.playAnimation();

        TextView backToLoginClickable = findViewById(R.id.back_to_login_clickable);
        Spannable backToLoginSpannable  = new SpannableString(backToLoginClickable.getText().toString());
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
        backToLoginSpannable.setSpan(clickSpan, 0, getString(R.string.to_previous_screen).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        backToLoginClickable.setText(backToLoginSpannable, TextView.BufferType.SPANNABLE);
        backToLoginClickable.setMovementMethod(LinkMovementMethod.getInstance());



        emailEditText.setOnClickListener(view -> recoverPasswordBtn.setText(getString(R.string.recover_password)));

        recoverPasswordBtn = findViewById(R.id.password_recovery_button);
        recoverPasswordBtn.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                recoverPasswordBtn.setText(getString(R.string.email_field_empty));
            } else {
                recoverPasswordBtn.setText("");
                loading.setVisibility(View.VISIBLE);
                resetPassword();
                //check the entered data?
                //send the recovery email
                //every time something's missing or going wrong the lock animation plays
                //after data is checked and mail is successfully sent we start the lottie anim:
            }
        });

    }

    private void resetPassword() {
        boolean[] emailExists = {false};
        String emailStr = emailEditText.getText().toString().trim();
        firestore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    outerloop:
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (Objects.equals(document.get("email"), emailStr)) {
                            emailExists[0] = true;
                            break outerloop;
                        }
                    }
                }
                if (emailExists[0]) {
                    auth.sendPasswordResetEmail(emailStr).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loading.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                recoverPasswordBtn.setText(getString(R.string.recovery_sent));
                                lockAnim.setVisibility(View.GONE);
                                mailSentAnim.setVisibility(View.VISIBLE);
                                mailSentAnim.playAnimation();
                            } else {
                                recoverPasswordBtn.setText(getString(R.string.error_occured));
                            }
                        }
                    });
                }
                else {
                    loading.setVisibility(View.GONE);
                    recoverPasswordBtn.setText(getString(R.string.password_reset_invalid_email));
                }
            }
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

}