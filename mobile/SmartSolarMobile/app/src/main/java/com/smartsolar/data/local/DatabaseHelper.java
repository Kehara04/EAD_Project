/*
 * References:
 *
 * Android Developers – SQLiteDatabase
 * https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase
 */
package com.smartsolar.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//Creates the local SQLite database used for Android session persistence.
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smartsolar.db";
    private static final int DB_VERSION = 1;

    // Initializes the local SQLite database with its name and version.
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Creates the user_session table to store the logged-in user's session details.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE user_session (" +
                        "id INTEGER PRIMARY KEY," +
                        "token TEXT NOT NULL," +
                        "user_id TEXT," +
                        "name TEXT," +
                        "email TEXT," +
                        "role TEXT," +
                        "reference_id TEXT" +
                        ")"
        );
    }

    // Recreates the session table when the database version is upgraded.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user_session");
        onCreate(db);
    }
}
