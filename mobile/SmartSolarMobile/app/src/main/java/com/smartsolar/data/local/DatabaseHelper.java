package com.smartsolar.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper
        extends SQLiteOpenHelper {

    private static final String DB_NAME =
            "smartsolar.db";

    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(
                context,
                DB_NAME,
                null,
                DB_VERSION
        );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE user_session (" +
                        "id INTEGER PRIMARY KEY," +
                        "token TEXT," +
                        "user_id TEXT," +
                        "name TEXT," +
                        "email TEXT," +
                        "role TEXT," +
                        "reference_id TEXT" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        db.execSQL(
                "DROP TABLE IF EXISTS user_session"
        );

        onCreate(db);
    }
}