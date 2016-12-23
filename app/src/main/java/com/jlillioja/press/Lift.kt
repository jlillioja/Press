package com.jlillioja.press

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Created by Jacob on 12/22/2016.
 */

@Table(database = PressDatabase::class)
class Lift : BaseModel() {

    @Column @PrimaryKey var id = null
    @Column var exercise: String? = null
    @Column var sets: Int = 0
    @Column var reps: Int = 0
    @Column var weight: Int = 0
}