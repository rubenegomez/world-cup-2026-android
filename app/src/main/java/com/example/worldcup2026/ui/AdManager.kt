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
    const val BANNER_TEST_ID = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712"

    val BANNER_AD_UNIT_ID: String
        get() = if (com.example.worldcup2026.BuildConfig.DEBUG) BANNER_TEST_ID else BANNER_REAL_ID

    val INTERSTITIAL_AD_UNIT_ID: String
        get() = if (com.example.worldcup2026.BuildConfig.DEBUG) INTERSTITIAL_TEST_ID else INTERSTITIAL_REAL_ID

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
        if (activity != null) {
            // Prioridad #1: Unity Ads
            UnityAdsManager.showRewardedAd(
                activity = activity,
                onRewardGranted = onRewardGranted,
                onFallback = {
                    // Respaldo #2: AdMob Rewarded
                    val ad = mRewardedAd
                    if (ad != null) {
                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                mRewardedAd = null
                                loadRewardedAd(context)
                            }
                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                mRewardedAd = null
                                onRewardGranted()
                            }
                        }
                        ad.show(activity) { _ ->
                            onRewardGranted()
                            Toast.makeText(context, "🎉 ¡2 horas sin publicidad activadas!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Cargando video anuncio...", Toast.LENGTH_SHORT).show()
                        loadRewardedAd(context)
                        onRewardGranted()
                    }
                }
            )
        } else {
            onRewardGranted()
        }
    }

    fun loadInterstitialAd(context: Context) {
        // Carga primaria en Unity Ads y respaldo en AdMob
        UnityAdsManager.loadInterstitialAd()
        if (mInterstitialAd != null || isLoading) return
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
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
        if (activity != null) {
            // Prioridad #1: Unity Ads
            UnityAdsManager.showInterstitialAd(
                context = context,
                onComplete = onComplete,
                onFallback = {
                    // Respaldo #2: AdMob Interstitial
                    val ad = mInterstitialAd
                    if (ad != null) {
                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                mInterstitialAd = null
                                onComplete()
                                loadInterstitialAd(context)
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                mInterstitialAd = null
                                onComplete()
                            }
                        }
                        ad.show(activity)
                    } else {
                        onComplete()
                    }
                }
            )
        } else {
            onComplete()
        }
    }
}

@Composable
fun AdmobBanner(modifier: Modifier = Modifier) {
    val showAdmobFallback = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    if (!showAdmobFallback.value) {
        UnityBannerView(
            modifier = modifier,
            onBannerFailed = {
                showAdmobFallback.value = true
            }
        )
    } else {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdManager.BANNER_AD_UNIT_ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
