package com.stepcounter.pedometer.walking.steptracker.calorieburner.nativefull;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.R;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.activity.Intro2Activity;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.base.BaseActivityAds;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.databinding.ActivityNativeFullBinding;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.utils.SystemConfiguration;

public class ActivityLoadNativeIntro1Full extends BaseActivityAds {
    ActivityNativeFullBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNativeFullBinding.inflate(getLayoutInflater());
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_LIGHT);
        setContentView(binding.getRoot());
        loadNativeFull();

    }

    private void loadNativeFull() {
//        Admob.getInstance().loadNativeAds(this, getString(R.string.native_tutorial_full), 1, new NativeCallback() {
//            @Override
//            public void onAdFailedToLoad() {
//                super.onAdFailedToLoad();
//                binding.frAdsFull.setVisibility(View.GONE);
//
//            }
//
//            @Override
//            public void onNativeAdLoaded(NativeAd nativeAd) {
//                super.onNativeAdLoaded(nativeAd);
//                NativeAdView adView = (NativeAdView) LayoutInflater.from(ActivityLoadNativeIntro1Full.this)
//                        .inflate(R.layout.native_full_language, null);
//                ImageView closeButton = adView.findViewById(R.id.close);
//                closeButton.setOnClickListener(v -> {
//                    Intent intent = new Intent(ActivityLoadNativeIntro1Full.this, Intro2Activity.class);
//                    startActivity(intent);
//                    finish();
//                });
//                new Handler().postDelayed(() -> closeButton.setVisibility(View.VISIBLE), 2000);
//                binding.frAdsFull.removeAllViews();
//                binding.frAdsFull.addView(adView);
//                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
//            }
//        });
        Intent intent = new Intent(ActivityLoadNativeIntro1Full.this, Intro2Activity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void nextActivity() {
        Intent intent = new Intent(this, Intro2Activity.class);
        startActivity(intent);
        finish();
    }
}
