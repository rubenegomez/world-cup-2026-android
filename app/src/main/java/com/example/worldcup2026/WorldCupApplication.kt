package com.example.worldcup2026

import android.app.Application
import com.google.android.gms.ads.MobileAds

class WorldCupApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
    }
}
