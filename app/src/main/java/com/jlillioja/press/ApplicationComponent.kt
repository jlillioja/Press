package com.jlillioja.press

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AndroidModule::class))
interface ApplicationComponent {
    fun inject(application: PressApplication)
    fun inject(mainActivity: MainActivity)
}