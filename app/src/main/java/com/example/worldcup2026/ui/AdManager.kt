package com.example.worldcup2026.ui

import android.app.Activity
import android.content.Context
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    const val INTERSTITIAL_REAL_ID = "ca-app-pub-7768012635304880/8383774129"
    const val BANNER_REAL_ID = "ca-app-pub-7768012635304880/7721429148"
    const val REWARDED_TEST_ID = "ca-app-pub-3940256099942544/5224354917"

    private var mInterstitialAd: InterstitialAd? = null
    private var isLoading = false

    private var mRewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    fun loadRewardedAd(context: Context) {
        UnityAdsManager.loadRewardedAd()
        if (mRewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_TEST_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mRewardedAd = null
                    isRewardedLoading = false
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    mRewardedAd = rewardedAd
                    isRewardedLoading = false
                }
            }
        )
    }

    fun showRewardedAd(context: Context, onRewardGranted: () -> Unit) {
        val activity = context as? Activity
        val ad = mRewardedAd

        if (activity != null && ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mRewardedAd = null
                    loadRewardedAd(context)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mRewardedAd = null
                    UnityAdsManager.showRewardedAd(activity, onRewardGranted)
                }
            }
            ad.show(activity) { _ ->
                onRewardGranted()
                Toast.makeText(context, "🎉 ¡2 horas sin publicidad activadas!", Toast.LENGTH_SHORT).show()
            }
        } else if (activity != null) {
            UnityAdsManager.showRewardedAd(activity, onRewardGranted)
        } else {
            onRewardGranted()
        }
    }

    fun loadInterstitialAd(context: Context) {
        // Carga primaria en AdMob y respaldo en Unity Ads
        UnityAdsManager.loadInterstitialAd()
        if (mInterstitialAd != null || isLoading) return
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_REAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    isLoading = false
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    isLoading = false
                }
            }
        )
    }

    fun showInterstitialAd(context: Context, onComplete: () -> Unit) {
        val activity = context as? Activity
        val ad = mInterstitialAd

        if (activity != null && ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    onComplete()
                    loadInterstitialAd(context)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mInterstitialAd = null
                    // Si AdMob falla, mediación automática hacia Unity Ads
                    UnityAdsManager.showInterstitialAd(context, onComplete)
                }
            }
            ad.show(activity)
        } else {
            // Mediación en cascada: AdMob no disponible -> mostrar Unity Ads
            UnityAdsManager.showInterstitialAd(context, onComplete)
        }
    }
}

@Composable
fun AdmobBanner(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showUnityFallback = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    if (showUnityFallback.value) {
        UnityBannerView(modifier = modifier)
    } else {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdManager.BANNER_REAL_ID
                    adListener = object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            super.onAdFailedToLoad(adError)
                            // Fallback inmediato hacia Unity Ads si AdMob rechaza el APK fuera de tienda
                            showUnityFallback.value = true
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
