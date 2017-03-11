package com.edin.aron.newsreader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Aron on 26/01/17.
 */

public class DbHelper extends SQLiteOpenHelper {
    public DbHelper(Context context) {
        super(context, "Provider", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String comandoSQL1="CREATE TABLE providers " +
                "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "url TEXT NOT NULL " +
                ")";
        String comandoSQL2="CREATE TABLE articles " +
                "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "date TEXT, " +
                "bookmark INTEGER DEFAULT 0, " +
                "url TEXT UNIQUE, " +
                "description TEXT NOT NULL " +
                ")";
        db.execSQL(comandoSQL1);
        db.execSQL(comandoSQL2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
