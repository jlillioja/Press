package com.jlillioja.press

import android.app.Application
import android.content.Context
import com.jlillioja.press.database.DatabaseManager
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
open class AndroidModule(private val application: Application) {

    @Provides
    @Singleton
    open fun provideApplicationContext(): Context {
        return application
    }

    @Provides
    @Singleton
    open fun provideDatabaseManager() : DatabaseManager {
        return DatabaseManager()
    }
}