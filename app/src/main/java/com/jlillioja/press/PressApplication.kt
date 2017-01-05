package com.jlillioja.press

import android.app.Application
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import javax.inject.Inject

open class PressApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        graph = DaggerApplicationComponent.builder().androidModule(AndroidModule(this)).build()
        graph.inject(this)

        // This instantiates DBFlow
        FlowManager.init(FlowConfig.Builder(this).build())
        // add for verbose logging
        // FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);
    }

    companion object {
        //platformStatic allow access it from java code
        @JvmStatic lateinit var graph: ApplicationComponent
    }
}