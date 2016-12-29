package com.jlillioja.press.database;

import android.provider.ContactsContract;

import com.jlillioja.press.database.PressDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import lombok.Data;

@Data(staticConstructor="of")
@Table(database = PressDatabase.class)
public class Lift extends BaseModel {

    public Lift() {
        super();
    }

    public Lift(String exercise, int sets, int reps, int weight) {
        this();
        this.exercise = exercise;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
    }

    @Column @PrimaryKey int id;

    @Column String exercise;
    @Column int sets;
    @Column int reps;
    @Column int weight;

}
