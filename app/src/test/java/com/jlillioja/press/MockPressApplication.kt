package com.jlillioja.press

open class MockPressApplication : PressApplication() {

    override fun onCreate() {
        super.onCreate()

        graph = DaggerApplicationComponent.builder().androidModule(MockAndroidModule(this)).build()
        graph.inject(this)
    }

    companion object {
        @JvmStatic lateinit var graph: ApplicationComponent
    }
}