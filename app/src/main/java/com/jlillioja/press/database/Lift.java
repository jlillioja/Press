package com.jlillioja.press.database;

import android.provider.ContactsContract;

import com.jlillioja.press.database.PressDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(database = PressDatabase.class)
public class Lift extends BaseModel {

    @Column @PrimaryKey int id;

    @Column String exercise;
    @Column int sets;
    @Column int reps;
    @Column int weight;
}