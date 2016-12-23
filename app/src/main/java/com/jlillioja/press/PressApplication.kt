package com.jlillioja.press

import android.app.Application
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager

class PressApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FlowManager.init(FlowConfig.Builder(this).build())
    }

    override fun onTerminate() {
        super.onTerminate()
        FlowManager.destroy()
    }
}