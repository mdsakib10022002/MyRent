package com.techmania.myrent

import android.app.Application
import com.google.firebase.FirebaseApp

class MyRentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
