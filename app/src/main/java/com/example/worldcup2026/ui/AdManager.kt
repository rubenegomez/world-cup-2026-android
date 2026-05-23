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

object AdManager {
    private const val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val BANNER_TEST_ID = "ca-app-pub-3940256099942544/6300978111"

    private var mInterstitialAd: InterstitialAd? = null
    private var isLoading = false

    fun loadInterstitialAd(context: Context) {
        if (mInterstitialAd != null || isLoading) return
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_TEST_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    isLoading = false
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context.applicationContext,
                            "AdMob error al cargar Interstitial: ${adError.message} (Código: ${adError.code})",
                            Toast.LENGTH_LONG
                        ).show()
                    }
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
                    onComplete()
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context.applicationContext,
                            "AdMob error al mostrar Interstitial: ${adError.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            ad.show(activity)
        } else {
            loadInterstitialAd(context)
            onComplete()
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context.applicationContext,
                    "Interstitial no listo, intentando cargar de nuevo...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

@Composable
fun AdmobBanner(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context.applicationContext,
                                "AdMob error al cargar Banner: ${adError.message} (Código: ${adError.code})",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
