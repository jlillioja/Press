package com.jlillioja.press.database;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by Jacob on 12/22/2016.
 */

@Database(name = PressDatabase.NAME, version = PressDatabase.VERSION)
public class PressDatabase {
    public static final String NAME = "MyDataBase";

    public static final int VERSION = 1;
}