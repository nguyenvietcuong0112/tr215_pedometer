package com.stepcounter.pedometer.walking.steptracker.calorieburner.nativefull;

import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.R;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.activity.Intro3Activity;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.base.BaseActivity;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.databinding.ActivityNativeFullBinding;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.utils.SystemConfiguration;


public class ActivityLoadNativeIntro2Full extends BaseActivity {
    ActivityNativeFullBinding binding;

    @Override
    public void bind() {
        binding = ActivityNativeFullBinding.inflate(getLayoutInflater());
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_LIGHT);
        setContentView(binding.getRoot());
        loadNativeFull();
    }

    private void loadNativeFull() {
        binding.frAdsFull.setVisibility(View.GONE);
        startActivity(new Intent(ActivityLoadNativeIntro2Full.this, Intro3Activity.class));
        finish();
//        Admob.getInstance().loadNativeAds(this, getString(R.string.native_tutorial2_full), 1, new NativeCallback() {
//            @Override
//            public void onAdFailedToLoad() {
//                super.onAdFailedToLoad();
//
//
//            }
//
//            @Override
//            public void onNativeAdLoaded(NativeAd nativeAd) {
//                super.onNativeAdLoaded(nativeAd);
//                NativeAdView adView = (NativeAdView) LayoutInflater.from(ActivityLoadNativeIntro2Full.this)
//                        .inflate(R.layout.native_full_language, null);
//                ImageView closeButton = adView.findViewById(R.id.close);
//                closeButton.setOnClickListener(v -> {
//                    startActivity(new Intent(ActivityLoadNativeIntro2Full.this, Intro3Activity.class));
//                });
//                new Handler().postDelayed(() -> closeButton.setVisibility(View.VISIBLE), 2000);
//                binding.frAdsFull.removeAllViews();
//                binding.frAdsFull.addView(adView);
//                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
//            }
//        });
    }
}
