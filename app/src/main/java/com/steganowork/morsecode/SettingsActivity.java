package com.steganowork.morsecode;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

public class SettingsActivity extends AppCompatActivity {
    String TAG = "SettingsActivity";
    static String versionName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        versionName = getVersionName(getApplicationContext());

        setupWindowAnimations();  // 트랜지션 함수
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            PreferenceScreen preferenceScreen = getPreferenceScreen();
            Preference pVersion = preferenceScreen.findPreference("app_version");
            pVersion.setSummary(getResources().getString(R.string.dev_v_summary) + " : " + versionName);
        }
    }

    public String getVersionName(Context context){
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private void setupWindowAnimations() {  // 트랜지션 함수
        //Slide slide = new Slide();
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setExitTransition(fade);

        Slide slide = new Slide();
        slide.setDuration(1000);
        getWindow().setReenterTransition(slide);
    }

    // 액션 바 =====================================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:  // 뒤로가기 아이콘
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}