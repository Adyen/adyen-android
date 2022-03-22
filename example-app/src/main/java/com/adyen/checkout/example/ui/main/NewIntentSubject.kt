package com.adyen.checkout.example.ui.main

import android.content.Intent

interface NewIntentSubject {

    fun registerObserver(observer: Observer)
    fun unregisterObserver(observer: Observer)

    interface Observer {
        fun onNewIntent(intent: Intent)
    }
}
