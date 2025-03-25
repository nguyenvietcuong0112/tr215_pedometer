package com.stepcounter.pedometer.walking.steptracker.calorieburner.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.R;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.base.BaseActivityAds;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.databinding.FragmentIntro2Binding;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.nativefull.ActivityLoadNativeIntro2Full;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.utils.SharePreferenceUtils;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.utils.SystemConfiguration;


public class Intro2Activity extends BaseActivityAds {
    FragmentIntro2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentIntro2Binding.inflate(getLayoutInflater());
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_LIGHT);
        setContentView(binding.getRoot());
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        loadNativeIntro2();
        if (!SharePreferenceUtils.isOrganic(this)) {
            checkNextButtonStatus(false);
            new Handler().postDelayed(() -> checkNextButtonStatus(true), 1500);
        }
        if (!SharePreferenceUtils.isOrganic(this)) {
            Admob.getInstance().loadCollapsibleBanner(Intro2Activity.this, getString(R.string.banner_collapse_intro), "top");
        } else {
            binding.llBanner.setVisibility(View.GONE);
            binding.animLoading.setVisibility(View.GONE);
        }
        binding.btnNext.setOnClickListener(v->{
            nextActivity();
        });
    }

    private void loadNativeIntro2() {
        if (!SharePreferenceUtils.isOrganic(this)) {
            loadNative2();
        } else {
            binding.frAds.removeAllViews();
            binding.frAds.setVisibility(View.INVISIBLE);
        }
    }

    private void loadNative2() {
        checkNextButtonStatus(false);
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_onboarding2), new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                runOnUiThread(() -> {
                    binding.frAds.setVisibility(View.INVISIBLE);
                    checkNextButtonStatus(true);
                });

            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                runOnUiThread(() -> {
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(Intro2Activity.this).inflate(R.layout.layout_native_language_non_organic, null);
                    binding.frAds.removeAllViews();
                    binding.frAds.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                    checkNextButtonStatus(true);
                });

            }
        });
    }


    private void checkNextButtonStatus(boolean isReady) {
        if (isReady) {
            binding.btnNext.setVisibility(View.VISIBLE);
            binding.btnNextLoading.setVisibility(View.GONE);
        } else {
            binding.btnNext.setVisibility(View.GONE);
            binding.btnNextLoading.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void nextActivity() {
        if (SharePreferenceUtils.isOrganic(this)) {
            Intent intent = new Intent(this, Intro3Activity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        } else {
            Intent intent = new Intent(this, ActivityLoadNativeIntro2Full.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        }
    }
}
