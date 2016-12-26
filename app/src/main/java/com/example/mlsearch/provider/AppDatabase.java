package com.example.mlsearch.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Database which use SQLite and support the contract defined in {@link
 * AppContract} It support incremental upgrades.
 */
public class AppDatabase extends SQLiteOpenHelper {
    /** Database Name */
    private static final String DATABASE_NAME = "application.db";
    /** Database version */
    public static final int DATABASE_VERSION = 1;

    /** DB table names. */
    interface Tables {
        String PRODUCTS = "products";
    }

    /** Constructor. */
    public AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.PRODUCTS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + AppContract.ProductsColumns.ID + " TEXT,"
                + AppContract.ProductsColumns.NAME + " TEXT,"
                + AppContract.ProductsColumns.PRICE + " TEXT,"
                + "UNIQUE (" + AppContract.ProductsColumns.ID + ") ON CONFLICT " +
                "REPLACE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Implement incremental upgrade.
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}
