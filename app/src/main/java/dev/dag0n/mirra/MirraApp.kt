package dev.dag0n.mirra

import android.app.Application
import dev.dag0n.mirra.data.SettingsStore

class MirraApp : Application() {
    lateinit var settings: SettingsStore
        private set

    override fun onCreate() {
        super.onCreate()
        settings = SettingsStore(this)
    }
}
