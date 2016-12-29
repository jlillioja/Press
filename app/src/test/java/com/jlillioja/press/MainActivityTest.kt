package com.jlillioja.press

import com.jlillioja.press.database.DatabaseManager
import com.jlillioja.press.database.Lift
import kotlinx.android.synthetic.main.activity_main.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import rx.Observable

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class MainActivityTest {

    lateinit var subject: MainActivity
    val mockDatabaseManager: DatabaseManager = mock(DatabaseManager::class.java)
    lateinit var fragment: HistoryFragment

    @Before
    fun setup() {
        subject = Robolectric.setupActivity(MainActivity::class.java)
        subject.databaseManager = mockDatabaseManager
        fragment = (subject.fragmentManager.findFragmentById(R.id.history_fragment) as HistoryFragment)
    }

    @Test
    fun submit_savesToDatabase() {
        subject.exercise.setText("Bench Press")
        subject.sets.setText("5")
        subject.reps.setText("5")
        subject.weight.setText("225")

        subject.submit.performClick()

        verify(mockDatabaseManager).saveLift("Bench Press", 5, 5, 225)
    }

    @Test
    fun historyfragment_LoadsLifts() {
        val mockLiftsObservable : Observable<List<Lift>> = Observable.just(listOf(Lift("Bench Press", 5, 5, 225)))
        `when`(mockDatabaseManager.getLiftsObservable()).thenReturn(mockLiftsObservable)

        setup()
        fragment.databaseManager = mockDatabaseManager
        fragment.onCreate(null)

        assert(fragment.isVisible)
        assertThat(fragment.displayedLifts, equalTo(listOf(Lift("Bench Press", 5, 5, 225))))
    }
}

