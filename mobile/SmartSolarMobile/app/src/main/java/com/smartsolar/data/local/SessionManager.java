/*
 * References:
 * Android Developers – Save Data Using SQLite
 * https://developer.android.com/training/data-storage/sqlite
 *
 */
package com.smartsolar.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smartsolar.model.LoginResponse;

//Stores and retrieves the authenticated user session from SQLite.
public class SessionManager {

    private final DatabaseHelper helper;

    // Initializes the database helper using the application context.
    public SessionManager(Context context) {
        helper = new DatabaseHelper(context.getApplicationContext());
    }

    // Saves the authenticated user's details and replaces any existing session
    public void saveSession(LoginResponse user) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("user_session", null, null);

        ContentValues values = new ContentValues();
        values.put("id", 1);
        values.put("token", user.getToken());
        values.put("user_id", user.getUserId());
        values.put("name", user.getName());
        values.put("email", user.getEmail());
        values.put("role", user.getRole());
        values.put("reference_id", user.getReferenceId());

        db.insertOrThrow("user_session", null, values);
    }

    // Checks whether a valid, non-empty authentication token is stored.
    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    // Retrieves the stored authentication token.
    public String getToken() {
        return getValue("token");
    }

     // Retrieves the authenticated user's ID.
    public String getUserId() {
        return getValue("user_id");
    }

    // Retrieves the authenticated user's name.
    public String getName() {
        return getValue("name");
    }

    // Retrieves the authenticated user's email address.
    public String getEmail() {
        return getValue("email");
    }

    // Retrieves the authenticated user's role.
    public String getRole() {
        return getValue("role");
    }

    // Retrieves the authenticated user's reference ID.
    public String getReferenceId() {
        return getValue("reference_id");
    }

    // Reads a specified column value from the stored user session.
    private String getValue(String column) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_session",
                null,
                "id = ?",
                new String[]{"1"},
                null,
                null,
                null
        );

        String value = null;
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(column);
            if (index >= 0) {
                value = cursor.getString(index);
            }
        }

        cursor.close();
        return value;
    }

    // Logs out the user by deleting the stored session from SQLite.
    public void logout() {
        helper.getWritableDatabase().delete("user_session", null, null);
    }
}
