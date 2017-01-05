package com.jlillioja.press.database

import dagger.Provides
import rx.Observable
import javax.inject.Inject

open class DatabaseManager {

    open fun saveLift(exercise : String, sets : Int, reps : Int, weight : Int) {
        Lift(exercise, sets, reps, weight).save()
    }

    open fun saveLift(lift: Lift) {
        lift.save()
    }

    open fun getLiftsObservable() : Observable<List<Lift>> {
        return Observable.empty()
    }
}