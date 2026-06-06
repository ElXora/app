package com.example

import android.app.Application
import android.os.Build

class MainApplication : Application() {
    override fun getAttributionTag(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            "default"
        } else {
            super.getAttributionTag()
        }
    }
}
