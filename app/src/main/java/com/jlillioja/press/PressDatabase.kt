package com.jlillioja.press

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Created by Jacob on 12/22/2016.
 */

@Database(name = PressDatabase.NAME, version = PressDatabase.VERSION)
object PressDatabase {

    const val NAME = "PressDatabase" // we will add the .db extension
    const val VERSION = 1

}