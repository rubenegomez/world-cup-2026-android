package com.example.worldcup2026.ui

import android.app.Activity
import android.content.Context
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // House Ads de Videos propios alojados en el Servidor
    data class HouseVideoAd(
        val appName: String,
        val appTagline: String,
        val videoUrl: String,
        val targetUrl: String,
        val accentColor: Long = 0xFF00E676
    )

    val houseVideoAds = listOf(
        HouseVideoAd(
            appName = "Bondi Maps",
            appTagline = "¡Encontrá paradas, recorridos y horarios de colectivos en tiempo real!",
            videoUrl = "https://ellocodelpedal.duckdns.org/videos/ads/bondi_ad.mp4",
            targetUrl = "https://ellocodelpedal.duckdns.org/bondi.html",
            accentColor = 0xFF00E676
        ),
        HouseVideoAd(
            appName = "TimeTracker Pro",
            appTagline = "¡Gestioná tus horas de trabajo, guardias y cobros con precisión!",
            videoUrl = "https://ellocodelpedal.duckdns.org/videos/ads/timetracker_ad.mp4",
            targetUrl = "https://ellocodelpedal.duckdns.org/timetracker.html",
            accentColor = 0xFFFFC107
        )
    )

    private var houseAdIndex = 0
    val currentHouseVideoAd = androidx.compose.runtime.mutableStateOf<HouseVideoAd?>(null)
    private var onHouseAdFinishedCallback: (() -> Unit)? = null

    fun showHouseVideoAd(onFinished: () -> Unit) {
        val ad = houseVideoAds[houseAdIndex % houseVideoAds.size]
        houseAdIndex++
        onHouseAdFinishedCallback = onFinished
        currentHouseVideoAd.value = ad
    }

    fun dismissHouseVideoAd() {
        currentHouseVideoAd.value = null
        onHouseAdFinishedCallback?.invoke()
        onHouseAdFinishedCallback = null
    }

    private var interstitialCounter = 0

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
                                // Respaldo #3: Video House Ad propio
                                showHouseVideoAd(onRewardGranted)
                            }
                        }
                        ad.show(activity) { _ ->
                            onRewardGranted()
                            Toast.makeText(context, "🎉 ¡2 horas sin publicidad activadas!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Si no cargó AdMob ni Unity, mostrar Video House Ad propio de Bondi o TimeTracker
                        showHouseVideoAd(onRewardGranted)
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
        interstitialCounter++

        // Intercalar 1 de cada 3 veces con Video House Ad propio (Bondi Maps / TimeTracker Pro)
        if (interstitialCounter % 3 == 0) {
            showHouseVideoAd(onComplete)
            return
        }

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
                                showHouseVideoAd(onComplete)
                            }
                        }
                        ad.show(activity)
                    } else {
                        showHouseVideoAd(onComplete)
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
    val showHouseBannerFallback = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    // Rotar o reintentar periódicamente
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(45000L) // cada 45 segundos intentar refrescar
        if (showHouseBannerFallback.value) {
            showHouseBannerFallback.value = false
            showAdmobFallback.value = false
        }
    }

    if (showHouseBannerFallback.value) {
        HouseBannerFallback(modifier = modifier)
    } else if (!showAdmobFallback.value) {
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
                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                            showHouseBannerFallback.value = true
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Composable
fun HouseBannerFallback(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val houseApps = remember {
        listOf(
            Triple("TimeTracker Pro", "Control de horas y guardias", "https://ellocodelpedal.duckdns.org/timetracker.html"),
            Triple("Bondi Maps", "Colectivos y mapas interactivos", "https://ellocodelpedal.duckdns.org/bondi.html"),
            Triple("Los Fondos del Loco", "Wallpapers Ultra HD exclusivos", "https://ellocodelpedal.duckdns.org/fondos.html")
        )
    }
    var appIndex by remember { mutableIntStateOf(0) }

    // Rotar banner propio cada 12 segundos
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(12000L)
            appIndex = (appIndex + 1) % houseApps.size
        }
    }

    val currentApp = houseApps[appIndex]

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(currentApp.third)).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
        color = Color(0xFF131F2E),
        border = BorderStroke(0.5.dp, Color(0xFF00E676).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⭐", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = currentApp.first,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color(0xFF00E676)
                    )
                    Text(
                        text = currentApp.second,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }

            Surface(
                color = Color(0xFF00E676),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "DESCARGAR",
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun HouseVideoAdPlayerOverlay(
    houseAd: AdManager.HouseVideoAd,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var countdown by remember { mutableIntStateOf(5) }
    var canSkip by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            kotlinx.coroutines.delay(1000L)
            countdown--
        }
        canSkip = true
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { if (canSkip) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = canSkip,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Reproductor nativo VideoView
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    android.widget.VideoView(ctx).apply {
                        setVideoURI(android.net.Uri.parse(houseAd.videoUrl))
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            start()
                        }
                        setOnErrorListener { _, _, _ ->
                            onDismiss()
                            true
                        }
                    }
                }
            )

            // Header con Cuenta Regresiva / Botón de Cerrar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "ANUNCIO DESTACADO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(houseAd.accentColor),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable {
                        if (canSkip) onDismiss()
                    }
                ) {
                    Text(
                        text = if (canSkip) "✕ Omitir" else "Omitir en ${countdown}s",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canSkip) Color.White else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }

            // Barra inferior con botón de Descargar / Instalar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                color = Color(0xFF131F2E).copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(houseAd.accentColor).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = houseAd.appName,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(houseAd.accentColor)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = houseAd.appTagline,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    androidx.compose.material3.Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(houseAd.targetUrl)).apply {
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(houseAd.accentColor)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "INSTALAR",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

