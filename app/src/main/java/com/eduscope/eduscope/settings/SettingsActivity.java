package com.eduscope.eduscope.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.FrameLayout;
import com.eduscope.eduscope.R;


public class SettingsActivity extends AppCompatActivity {

    private final String timeInAppKeySA = "TimeInApp";
    private SharedPreferences prefSA;
    private long appSessionStartSA;
    //private TextView settings_text;
    boolean isSwitch;
    //boolean onStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity_layout);

        prefSA = getSharedPreferences("Statistics", Context.MODE_PRIVATE);


        //Bundle extras = getIntent().getExtras();

        FrameLayout settings_frame = findViewById(R.id.fragment_container);

        SharedPreferences def_pref = PreferenceManager.getDefaultSharedPreferences(this);
        isSwitch = def_pref.getBoolean("theme_switch_preference", false);

            if (isSwitch) {
                setTheme(R.style.PreferenceScreenDarkTheme);
                settings_frame.setBackgroundColor(getResources().getColor(R.color.dark_theme));
            }


        if (getSupportActionBar() != null) { //!!!ПРОВЕРИТЬ, ОТОБРАЖАЕТСЯ ЛИ ACTION BAR НА ДРУГИХ УСТРОЙСТВАХ
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true); //стрелка возврата на home screen
            actionBar.setTitle(R.string.settings_toolbar_title);
        }

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit(); //замена обычного контента на фрагмент и запуск фрагмена
        //потом фрагмент (SettingsFragment) запускает экран preference_screen

        def_pref.registerOnSharedPreferenceChangeListener((sharedPreferences, s) -> {
            //anyChanges = true;
            //ActivityCompat.recreate(SettingsActivity.this); //после 5-20 раз подряд перестает работать, иногда сразу
            recreate();
            //finish();
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { //зависит от наличия action бара
        //if (item.getItemId() == android.R.id.home) //стандартный id главной страницы
        StatisticsSaveLongSA(timeInAppKeySA, prefSA.getLong(timeInAppKeySA, 0) + (System.currentTimeMillis() - appSessionStartSA)); //работает
        finish();
        return true;
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        StatisticsSaveLongSA(timeInAppKeySA, prefSA.getLong(timeInAppKeySA, 0) + (System.currentTimeMillis() - appSessionStartSA)); //работает
        finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
        StatisticsSaveLongSA(timeInAppKeySA, prefSA.getLong(timeInAppKeySA, 0) + (System.currentTimeMillis() - appSessionStartSA)); //работает
        //def_pref.unregisterOnSharedPreferenceChangeListener((sharedPreferences, s) -> {});
    }


    public void StatisticsSaveLongSA(String key, long value) {
        SharedPreferences.Editor edit = prefSA.edit();
        edit.putLong(key, value);
        edit.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        appSessionStartSA = System.currentTimeMillis();
    }


    @Override
    public void onDestroy() {
        //finish();
        super.onDestroy();
        //StatisticsSaveLongSA(timeInAppKeySA, timeInAppCountSA + (System.currentTimeMillis() - appSessionStartSA - (System.currentTimeMillis() - timeAfterOnStop)));
    }
}