package com.jlillioja.press

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(MockAndroidModule::class))
interface TestComponent : ApplicationComponent {
    fun inject(test : MainActivityTest)
}