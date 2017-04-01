package com.jlillioja.press

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.jlillioja.press.database.DatabaseManager
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import org.robolectric.annotation.Implements
import javax.inject.Inject

@Implements
open class MainActivity : Activity() {

    @Inject
    open lateinit var databaseManager: DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PressApplication.graph.inject(this)

        verticalLayout {
            button {
                onClick {
                    saveCurrent()
                }
            }
        }
    }

    fun saveCurrent() {
        val exercise = exercise.text.toString()
        val sets = sets.text.toString()
        val reps = reps.text.toString()
        val weight = weight.text.toString()

        if (sets == "" || reps == "" || weight == "") {
            Toast.makeText(this, "Please enter numbers to do something", LENGTH_SHORT).show()
        } else {
            databaseManager.saveLift(
                    exercise = exercise,
                    sets = sets.toInt(),
                    reps = reps.toInt(),
                    weight = weight.toInt())
        }
    }
}

