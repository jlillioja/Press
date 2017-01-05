package com.jlillioja.press

import android.app.Application
import android.content.Context
import com.jlillioja.press.database.DatabaseManager
import com.nhaarman.mockito_kotlin.mock
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class MockAndroidModule (protected val application: Application) : AndroidModule(application) {

    @Provides
    override fun provideApplicationContext(): Application {
        return application
    }

    @Provides
    @Singleton
    override fun provideDatabaseManager() : DatabaseManager {
        return mock()
    }

}