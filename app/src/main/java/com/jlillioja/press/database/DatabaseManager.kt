package com.jlillioja.press.database

import javax.inject.Inject


open class DatabaseManager {

    open fun saveLift(exercise : String, sets : Int, reps : Int, weight : Int) {
        val lift = Lift()

        lift.exercise = exercise
        lift.sets = sets
        lift.reps = reps
        lift.weight = weight

        lift.save()
    }

}