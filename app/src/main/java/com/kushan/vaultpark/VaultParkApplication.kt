package com.kushan.vaultpark

import android.app.Application
import android.content.Context

class VaultParkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: VaultParkApplication
            private set
    }
}
