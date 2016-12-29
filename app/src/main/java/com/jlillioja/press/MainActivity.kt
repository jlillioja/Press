package com.jlillioja.press

import android.app.Activity
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.jlillioja.press.database.DatabaseManager

import kotlinx.android.synthetic.main.activity_main.*
import org.robolectric.annotation.Implements
import javax.inject.Inject

@Implements
class MainActivity() : Activity(), HistoryFragment.OnFragmentInteractionListener {

    @Inject
    lateinit  var databaseManager: DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        submit.setOnClickListener { saveCurrent() }

        setUpHistoryFragment()
    }

    private fun setUpHistoryFragment() {
        fragmentManager.beginTransaction()
                .add(R.id.history_fragment, HistoryFragment.newInstance())
                .commit()

    }

    fun saveCurrent() {
        databaseManager.saveLift(
                exercise = exercise.text.toString(),
                sets = sets.text.toString().toInt(),
                reps = reps.text.toString().toInt(),
                weight = weight.text.toString().toInt())
    }

    override fun onFragmentInteraction(uri: Uri) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

