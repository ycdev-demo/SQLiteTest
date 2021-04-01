package me.ycdev.android.demo.sqlite

import android.app.Application
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree());
        Timber.tag("SQLiteTest").i("app starting...")
    }
}