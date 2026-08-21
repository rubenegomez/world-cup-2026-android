package com.example.worldcup2026.ui

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

object UnityAdsManager {
    const val GAME_ID = "800359844"
    const val REWARDED_PLACEMENT_ID = "Rewarded_Android"
    const val INTERSTITIAL_PLACEMENT_ID = "Interstitial_Android"
    const val BANNER_PLACEMENT_ID = "Banner_Android"
    const val TEST_MODE = false

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized || UnityAds.isInitialized) {
            isInitialized = true
            return
        }

        UnityAds.initialize(
            context.applicationContext,
            GAME_ID,
            TEST_MODE,
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    isInitialized = true
                    loadRewardedAd()
                    loadInterstitialAd()
                }

                override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                    isInitialized = false
                }
            }
        )
    }

    fun loadRewardedAd() {
        if (!isInitialized) return
        UnityAds.load(REWARDED_PLACEMENT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {}
            override fun onUnityAdsFailedToLoad(placementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {}
        })
    }

    fun loadInterstitialAd() {
        if (!isInitialized) return
        UnityAds.load(INTERSTITIAL_PLACEMENT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {}
            override fun onUnityAdsFailedToLoad(placementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {}
        })
    }

    fun showRewardedAd(activity: Activity, onRewardGranted: () -> Unit) {
        if (!isInitialized) {
            init(activity)
            Toast.makeText(activity, "Cargando anuncio de Unity Ads, intente de nuevo en un instante...", Toast.LENGTH_SHORT).show()
            return
        }

        UnityAds.show(
            activity,
            REWARDED_PLACEMENT_ID,
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowComplete(placementId: String?, state: UnityAds.UnityAdsShowCompletionState?) {
                    if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                        onRewardGranted()
                        Toast.makeText(activity, "🎉 ¡Recompensa obtenida!", Toast.LENGTH_SHORT).show()
                    }
                    loadRewardedAd()
                }

                override fun onUnityAdsShowFailure(placementId: String?, error: UnityAds.UnityAdsShowError?, message: String?) {
                    Toast.makeText(activity, "No se pudo mostrar el anuncio de Unity Ads.", Toast.LENGTH_SHORT).show()
                    loadRewardedAd()
                }

                override fun onUnityAdsShowStart(placementId: String?) {}
                override fun onUnityAdsShowClick(placementId: String?) {}
            }
        )
    }

    fun showInterstitialAd(context: Context, onComplete: () -> Unit) {
        val activity = context as? Activity
        if (activity == null || !isInitialized) {
            if (activity != null) init(activity)
            onComplete()
            return
        }

        UnityAds.show(
            activity,
            INTERSTITIAL_PLACEMENT_ID,
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowComplete(placementId: String?, state: UnityAds.UnityAdsShowCompletionState?) {
                    onComplete()
                    loadInterstitialAd()
                }

                override fun onUnityAdsShowFailure(placementId: String?, error: UnityAds.UnityAdsShowError?, message: String?) {
                    onComplete()
                    loadInterstitialAd()
                }

                override fun onUnityAdsShowStart(placementId: String?) {}
                override fun onUnityAdsShowClick(placementId: String?) {}
            }
        )
    }
}

@Composable
fun UnityBannerView(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? Activity
    
    if (activity != null) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { ctx ->
                val bannerView = BannerView(activity, UnityAdsManager.BANNER_PLACEMENT_ID, UnityBannerSize(320, 50))
                bannerView.load()
                bannerView
            }
        )
    }
}
