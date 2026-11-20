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
import com.unity3d.ads.UnityAdsLoadOptions
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

object UnityAdsManager {
    const val GAME_ID = "800359844"
    const val REWARDED_PLACEMENT_ID = "Rewarded_Android"
    const val INTERSTITIAL_PLACEMENT_ID = "Intersticial_Android"
    const val BANNER_PLACEMENT_ID = "Banner_Android"
    val TEST_MODE: Boolean
        get() = com.example.worldcup2026.BuildConfig.DEBUG

    private var isInitialized = false
    private var isInitializing = false
    private var isRewardedLoaded = false
    private var isRewardedLoading = false
    private var isInterstitialLoaded = false
    private var isInterstitialLoading = false

    fun init(context: Context) {
        if (UnityAds.isInitialized) {
            isInitialized = true
            isInitializing = false
            loadRewardedAd()
            loadInterstitialAd()
            return
        }
        if (isInitializing) return
        isInitializing = true

        UnityAds.initialize(
            context.applicationContext,
            GAME_ID,
            TEST_MODE,
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    android.util.Log.d("UnityAdsManager", "✅ Unity Ads Inicializado con Éxito (GameID: $GAME_ID, TestMode: $TEST_MODE)")
                    isInitialized = true
                    isInitializing = false
                    loadRewardedAd()
                    loadInterstitialAd()
                }

                override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                    android.util.Log.e("UnityAdsManager", "❌ Error al inicializar Unity Ads: $error | $message")
                    isInitialized = false
                    isInitializing = false
                }
            }
        )
    }

    fun loadRewardedAd() {
        if (!UnityAds.isInitialized || isRewardedLoaded || isRewardedLoading) return
        isRewardedLoading = true
        android.util.Log.d("UnityAdsManager", "Intentando cargar Video Recompensado: $REWARDED_PLACEMENT_ID")
        val loadOptions = UnityAdsLoadOptions()
        UnityAds.load(REWARDED_PLACEMENT_ID, loadOptions, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                android.util.Log.d("UnityAdsManager", "✅ Video Recompensado CARGADO: $placementId")
                isRewardedLoaded = true
                isRewardedLoading = false
            }
            override fun onUnityAdsFailedToLoad(placementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {
                android.util.Log.w("UnityAdsManager", "⚠️ Falló carga Video Recompensado: $placementId | Error: $error | Msg: $message")
                isRewardedLoaded = false
                isRewardedLoading = false
            }
        })
    }

    fun loadInterstitialAd() {
        if (!UnityAds.isInitialized || isInterstitialLoaded || isInterstitialLoading) return
        isInterstitialLoading = true
        android.util.Log.d("UnityAdsManager", "Intentando cargar Intersticial: $INTERSTITIAL_PLACEMENT_ID")
        val loadOptions = UnityAdsLoadOptions()
        UnityAds.load(INTERSTITIAL_PLACEMENT_ID, loadOptions, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                android.util.Log.d("UnityAdsManager", "✅ Intersticial CARGADO: $placementId")
                isInterstitialLoaded = true
                isInterstitialLoading = false
            }
            override fun onUnityAdsFailedToLoad(placementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {
                android.util.Log.w("UnityAdsManager", "⚠️ Falló carga Intersticial: $placementId | Error: $error | Msg: $message")
                isInterstitialLoaded = false
                isInterstitialLoading = false
            }
        })
    }

    fun showRewardedAd(activity: Activity, onRewardGranted: () -> Unit, onFallback: () -> Unit) {
        if (!UnityAds.isInitialized) {
            init(activity)
            onFallback()
            return
        }
        if (!isRewardedLoaded) {
            loadRewardedAd()
            onFallback()
            return
        }

        isRewardedLoaded = false
        UnityAds.show(
            activity,
            REWARDED_PLACEMENT_ID,
            UnityAdsShowOptions(),
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowComplete(placementId: String?, state: UnityAds.UnityAdsShowCompletionState?) {
                    if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                        onRewardGranted()
                        Toast.makeText(activity, "🎉 ¡2 horas sin publicidad activadas!", Toast.LENGTH_SHORT).show()
                    }
                    loadRewardedAd()
                }

                override fun onUnityAdsShowFailure(placementId: String?, error: UnityAds.UnityAdsShowError?, message: String?) {
                    loadRewardedAd()
                    onFallback()
                }

                override fun onUnityAdsShowStart(placementId: String?) {}
                override fun onUnityAdsShowClick(placementId: String?) {}
            }
        )
    }

    fun showInterstitialAd(context: Context, onComplete: () -> Unit, onFallback: () -> Unit) {
        val activity = context as? Activity
        if (activity == null) {
            onFallback()
            return
        }
        if (!UnityAds.isInitialized) {
            init(activity)
            onFallback()
            return
        }
        if (!isInterstitialLoaded) {
            loadInterstitialAd()
            onFallback()
            return
        }

        isInterstitialLoaded = false
        UnityAds.show(
            activity,
            INTERSTITIAL_PLACEMENT_ID,
            UnityAdsShowOptions(),
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowComplete(placementId: String?, state: UnityAds.UnityAdsShowCompletionState?) {
                    onComplete()
                    loadInterstitialAd()
                }

                override fun onUnityAdsShowFailure(placementId: String?, error: UnityAds.UnityAdsShowError?, message: String?) {
                    loadInterstitialAd()
                    onFallback()
                }

                override fun onUnityAdsShowStart(placementId: String?) {}
                override fun onUnityAdsShowClick(placementId: String?) {}
            }
        )
    }
}

@Composable
fun UnityBannerView(modifier: Modifier = Modifier, onBannerFailed: () -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? Activity

    if (activity != null && UnityAds.isInitialized) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { _ ->
                BannerView(activity, UnityAdsManager.BANNER_PLACEMENT_ID, UnityBannerSize(320, 50)).apply {
                    listener = object : BannerView.Listener() {
                        override fun onBannerLoaded(bannerAdView: BannerView?) {
                            android.util.Log.d("UnityAdsManager", "✅ Banner Unity CARGADO")
                        }
                        override fun onBannerFailedToLoad(bannerAdView: BannerView?, errorInfo: com.unity3d.services.banners.BannerErrorInfo?) {
                            android.util.Log.w("UnityAdsManager", "⚠️ Falló Banner Unity: ${errorInfo?.errorCode} | ${errorInfo?.errorMessage}")
                            onBannerFailed()
                        }
                    }
                    load()
                }
            },
            onRelease = { bannerView ->
                bannerView.destroy()
            }
        )
    } else {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            onBannerFailed()
        }
    }
}
