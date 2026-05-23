package com.example.worldcup2026

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.example.worldcup2026.data.util.AnalyticsManager

class WorldCupApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
        AnalyticsManager.initialize(this)
    }
}
