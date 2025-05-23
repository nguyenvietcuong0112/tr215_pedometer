package com.stepcounter.pedometer.walking.steptracker.calorieburner.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Process;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.mallegan.ads.callback.InterCallback;
import com.mallegan.ads.util.Admob;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.R;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.base.BaseActivity;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.databinding.ActivitySplashBinding;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.utils.SharePreferenceUtils;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.utils.SharedClass;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.utils.SystemUtil;


import java.io.File;
import java.io.IOException;
import java.util.Map;


public class SplashActivity extends BaseActivity {
    private InterCallback interCallback;
    SharedPreferences.Editor editor;
    SharedPreferences spref;

    private SharePreferenceUtils sharePreferenceUtils;

    @Override
    public void bind() {
        SystemUtil.setLocale(this);
        ActivitySplashBinding activitySplashBinding = ActivitySplashBinding.inflate(getLayoutInflater());
        getWindow().setFlags(1024, 1024);

        setContentView(activitySplashBinding.getRoot());
        loadAds();


        SharedPreferences sharedPreferences = getSharedPreferences("pref_ads", 0);
        this.spref = sharedPreferences;
        this.editor = sharedPreferences.edit();
    }

    private void loadAds() {
        sharePreferenceUtils = new SharePreferenceUtils(this);
        int counterValue = sharePreferenceUtils.getCurrentValue();
        Uri uri = getIntent().getData();
        interCallback = new InterCallback() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                if (uri != null) {
                    File file = null;
                    try {
                        file = SystemUtil.fileFromContentUri(getBaseContext(), uri);
                        SharedClass.filePath = file.getPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
//                if (SharePreferenceUtils.getInstance(SplashActivity.this).isLanguageSetting()) {
//                    startActivity(new Intent(SplashActivity.this, IntroActivity.class));
//                } else {
                if (counterValue == 1) {
                    startActivity(new Intent(SplashActivity.this, InterestActivity.class));
                } else if (counterValue == 0){
                    startActivity(new Intent(SplashActivity.this, LanguageActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));

                }
//
                finish();
            }
        };
        Admob.getInstance().loadSplashInterAds2(this, getString(R.string.inter_splash), 3000, interCallback);

        if (SharePreferenceUtils.isOrganic(this)) {
            AppsFlyerLib.getInstance().registerConversionListener(this, new AppsFlyerConversionListener() {

                @Override
                public void onConversionDataSuccess(Map<String, Object> conversionData) {
                    String mediaSource = (String) conversionData.get("media_source");
                    SharePreferenceUtils.setOrganicValue(getApplicationContext(), mediaSource == null || mediaSource.isEmpty() || mediaSource.equals("organic"));
                }

                @Override
                public void onConversionDataFail(String s) {
                    // Handle conversion data failure
                }

                @Override
                public void onAppOpenAttribution(Map<String, String> map) {
                    // Handle app open attribution
                }

                @Override
                public void onAttributionFailure(String s) {
                    // Handle attribution failure
                }
            });
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ExitApp();
    }

    public void ExitApp() {
        moveTaskToBack(true);
        finish();
        Process.killProcess(Process.myPid());
        System.exit(0);
    }
}
