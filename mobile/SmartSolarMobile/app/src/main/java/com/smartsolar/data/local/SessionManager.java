package com.smartsolar.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smartsolar.model.LoginResponse;

/**
 * Stores and retrieves the authenticated user session from SQLite.
 */
public class SessionManager {

    private final DatabaseHelper helper;

    public SessionManager(Context context) {
        helper = new DatabaseHelper(context.getApplicationContext());
    }

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

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public String getToken() {
        return getValue("token");
    }

    public String getUserId() {
        return getValue("user_id");
    }

    public String getName() {
        return getValue("name");
    }

    public String getEmail() {
        return getValue("email");
    }

    public String getRole() {
        return getValue("role");
    }

    public String getReferenceId() {
        return getValue("reference_id");
    }

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

    public void logout() {
        helper.getWritableDatabase().delete("user_session", null, null);
    }
}
