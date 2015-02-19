package com.runze.yourheroes.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.runze.yourheroes.db.YourHeroesContract.PersonEntry;

/**
 * Created by Eloi Jr on 31/01/2015.
 */
public class YourHeroesDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "yourheroes.db";

    public YourHeroesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_PERSON = "CREATE TABLE " + PersonEntry.TABLE_NAME + "(" +
                PersonEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                PersonEntry.COLUMN_NAME + " TEXT, " +
                PersonEntry.COLUMN_DESCRIPTION + " TEXT, " +
                PersonEntry.COLUMN_URLDETAIL + " TEXT, " +
                PersonEntry.COLUMN_LANDSCAPESMALL + " TEXT, " +
                PersonEntry.COLUMN_STANDARDXLARGE + " TEXT );";

        db.execSQL(SQL_CREATE_PERSON);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PersonEntry.TABLE_NAME);
        onCreate(db);
    }
}
