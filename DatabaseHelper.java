
package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "QueueDB";
    private static final int DATABASE_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE queue (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "service TEXT NOT NULL, " +
                        "status TEXT NOT NULL DEFAULT 'Waiting')"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS queue");
        onCreate(db);
    }

    public long addCustomer(String name, String service) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("service", service);
        values.put("status", "Waiting");

        return db.insert("queue", null, values);
    }

    public Cursor getWaitingCustomers() {
        return getReadableDatabase().rawQuery(
                "SELECT id, name, service FROM queue " +
                        "WHERE status = 'Waiting' ORDER BY id ASC",
                null
        );
    }

    public boolean markServed(int tokenId) {
        ContentValues values = new ContentValues();
        values.put("status", "Served");

        int rows = getWritableDatabase().update(
                "queue",
                values,
                "id = ? AND status = ?",
                new String[]{
                        String.valueOf(tokenId), "Waiting"
                }
        );

        return rows > 0;
    }
}
