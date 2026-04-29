package com.example.androidfinalproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class ReceiptDatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "ScanExpense.db";

    // ⚠️ Increment version to trigger onUpgrade
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_NAME = "receipt_table";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_VENDOR = "vendor";
    private static final String COLUMN_DATE = "receipt_date";
    private static final String COLUMN_TOTAL = "total";
    private static final String COLUMN_CATEGORY = "category";  // 👈 New column

    public ReceiptDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_VENDOR + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_TOTAL + " REAL, " +
                COLUMN_CATEGORY + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple drop and recreate strategy — data loss if upgrading in production
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addReceipt(String vendor, String date, double total, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_VENDOR, vendor);
        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_TOTAL, total);
        cv.put(COLUMN_CATEGORY, category);

        long result = db.insert(TABLE_NAME, null, cv);
        if (result == -1) {
            Toast.makeText(context, "Failed to save receipt", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Receipt saved successfully", Toast.LENGTH_SHORT).show();
        }
    }

    public Cursor readAllReceipts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public void updateReceipt(int id, String vendor, String date, double total, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_VENDOR, vendor);
        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_TOTAL, total);
        cv.put(COLUMN_CATEGORY, category);

        db.update(TABLE_NAME, cv, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void deleteReceipt(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Optional alias for clarity (you can remove one if redundant)
    public void insertReceipt(String vendor, String date, double total, String category) {
        addReceipt(vendor, date, total, category);
    }
}
