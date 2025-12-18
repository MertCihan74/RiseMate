package com.parthenios.risemate

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics

class RiseMateApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Crashlytics'i başlat
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }
}

