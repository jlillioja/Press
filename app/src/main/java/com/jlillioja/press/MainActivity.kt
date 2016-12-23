package com.jlillioja.press

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import kotlinx.android.synthetic.main.activity_main.*
import org.robolectric.annotation.Implements
import javax.inject.Inject

@Implements
class MainActivity() : Activity() {

    @Inject
    lateinit  var databaseManager: DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        submit.setOnClickListener { saveCurrent() }
    }

    fun saveCurrent() {
        databaseManager.saveLift(
                exercise = exercise.text.toString(),
                sets = sets.text.toString().toInt(),
                reps = reps.text.toString().toInt(),
                weight = weight.text.toString().toInt())
    }
}
