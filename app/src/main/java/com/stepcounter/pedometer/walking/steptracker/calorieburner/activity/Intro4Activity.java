package com.stepcounter.pedometer.walking.steptracker.calorieburner.activity;

import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.InterCallback;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.R;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.base.BaseActivity;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.databinding.FragmentIntro4Binding;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.utils.ActivityLoadNativeFull;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.utils.SharePreferenceUtils;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.utils.SystemConfiguration;


public class Intro4Activity extends BaseActivity {
    FragmentIntro4Binding binding;


    @Override
    public void bind() {
        binding = FragmentIntro4Binding.inflate(getLayoutInflater());
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_LIGHT);
        setContentView(binding.getRoot());
        loadNativeIntro4();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        if (!SharePreferenceUtils.isOrganic(this)) {
            checkNextButtonStatus(false);
            new Handler().postDelayed(() -> checkNextButtonStatus(true), 1500);
        }
        if (!SharePreferenceUtils.isOrganic(this)) {
            Admob.getInstance().loadCollapsibleBanner(Intro4Activity.this, getString(R.string.banner_collapse_intro), "top");
        } else {
            binding.llBanner.setVisibility(View.GONE);
        }
        binding.btnNext.setOnClickListener(v -> {
            if (SharePreferenceUtils.isOrganic(this)) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                checkNextButtonStatus(false);
                Admob.getInstance().loadSplashInterAds2(Intro4Activity.this, getString(R.string.inter_intro), 0, new InterCallback() {

                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        Intent intent = new Intent(Intro4Activity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    @Override
                    public void onAdClosedByUser() {
                        super.onAdClosedByUser();
                        if (!SharePreferenceUtils.isOrganic(getApplicationContext())){
                            Intent intent = new Intent(Intro4Activity.this, ActivityLoadNativeFull.class);
                            intent.putExtra(ActivityLoadNativeFull.EXTRA_NATIVE_AD_ID, getString(R.string.native_full_intro));
                            startActivity(intent);
                        }

                    }
                });

            }
        });
    }

    private void loadNativeIntro4() {
        checkNextButtonStatus(false);
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_onboarding4), new NativeCallback() {
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
                NativeAdView adView;
                if (SharePreferenceUtils.isOrganic(Intro4Activity.this)) {
                    adView = (NativeAdView) LayoutInflater.from(Intro4Activity.this)
                            .inflate(R.layout.layout_native_intro_third, null);
                } else {
                    adView = (NativeAdView) LayoutInflater.from(Intro4Activity.this)
                            .inflate(R.layout.layout_native_language_non_organic, null);
                }
                runOnUiThread(() -> {

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

}
