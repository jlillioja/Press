package com.jlillioja.press

import com.jlillioja.press.database.DatabaseManager
import com.jlillioja.press.database.Lift
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import kotlinx.android.synthetic.main.activity_main.*
import org.hamcrest.CoreMatchers.equalTo


import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.arrayContainingInAnyOrder
import org.hamcrest.Matchers.contains
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import rx.Observable
import javax.inject.Inject
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = MockPressApplication::class)
open class MainActivityTest {

    lateinit var subject: MainActivity
    lateinit var fragment: HistoryFragment
    @Inject open lateinit var mockDatabaseManager: DatabaseManager

    @Before
    fun setup() {
        val app = RuntimeEnvironment.application as MockPressApplication
        MockPressApplication.graph.inject(this)

        val mockLiftsObservable : Observable<List<Lift>> = Observable.just(listOf(Lift("Bench Press", 5, 5, 225)))
        whenever(mockDatabaseManager.getLiftsObservable()).thenReturn(mockLiftsObservable)

        subject = Robolectric.setupActivity(MainActivity::class.java)
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
    fun submit_handlesNullInput() {
        subject.submit.performClick()

        verify(mockDatabaseManager, never()).saveLift(anyString(), anyInt(), anyInt(), anyInt())
    }

    @Test
    fun historyfragment_loadsLifts() {
        fragment.databaseManager = mockDatabaseManager
        fragment.onCreate(null)

        assert(fragment.isVisible)
        assertThat(fragment.displayedLifts, contains(any<Lift>()))
    }
}

