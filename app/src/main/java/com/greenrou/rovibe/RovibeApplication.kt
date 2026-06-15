package com.greenrou.rovibe

import android.app.Application
import com.greenrou.rovibe.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RovibeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RovibeApplication)
            modules(appModule)
        }
    }
}
