package com.smartsolar.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smartsolar.model.LoginResponse;

public class SessionManager {

    private final DatabaseHelper helper;

    public SessionManager(Context context) {
        helper = new DatabaseHelper(context);
    }

    public void saveSession(LoginResponse user) {

        SQLiteDatabase db = helper.getWritableDatabase();

        db.delete(
                "user_session",
                null,
                null
        );

        ContentValues values = new ContentValues();

        values.put("id", 1);
        values.put("token", user.getToken());
        values.put("user_id", user.getUserId());
        values.put("name", user.getName());
        values.put("email", user.getEmail());
        values.put("role", user.getRole());
        values.put("reference_id", user.getReferenceId());

        db.insert(
                "user_session",
                null,
                values
        );
    }

    public String getToken() {
        return getValue("token");
    }

    public String getRole() {
        return getValue("role");
    }

    private String getValue(String column) {

        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(
                "user_session",
                null,
                "id = 1",
                null,
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
        helper.getWritableDatabase()
                .delete(
                        "user_session",
                        null,
                        null
                );
    }
}