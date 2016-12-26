package com.jlillioja.press

import com.jlillioja.press.database.DatabaseManager
import kotlinx.android.synthetic.main.activity_main.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class MainActivityTest {

    @Test
    fun test() {
        val mockDatabaseManager = mock(DatabaseManager::class.java)
        val activity = Robolectric.setupActivity(MainActivity::class.java)
        activity.databaseManager = mockDatabaseManager


        activity.exercise.setText("Bench Press")
        activity.sets.setText("5")
        activity.reps.setText("5")
        activity.weight.setText("225")

        activity.submit.performClick()

        verify(mockDatabaseManager).saveLift(anyString(), anyInt(), anyInt(), anyInt())

        assert(activity.fragmentManager.findFragmentById(R.id.history_fragment).isVisible)
    }
}

