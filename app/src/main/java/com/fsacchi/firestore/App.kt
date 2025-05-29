package com.fsacchi.firestore

import android.app.Application
import com.fsacchi.firestore.core.di.presentationModules
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

open class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initiateKoin()
        FirebaseApp.initializeApp(this)
    }

    private fun initiateKoin() {
        startKoin {
            androidContext(this@App)
            modules(provideDependency())
        }
    }

    internal open fun provideDependency() = presentationModules
}
