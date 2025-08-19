package org.example.project

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.cloudinary.android.MediaManager


class MyApp : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var ctx: Context
            private set
    }
    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext

        val config = hashMapOf(
            "cloud_name" to "detpngf0i",
            "api_key"    to "859679673437186",
            "api_secret" to "mupstOb71Ci2Yg3C3_kI8tRD-CA"
        )
        MediaManager.init(this, config)
    }
}

