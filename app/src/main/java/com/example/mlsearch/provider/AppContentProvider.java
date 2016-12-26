package com.example.mlsearch.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.BuildConfig;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Application {@link ContentProvider}.
 */
public class AppContentProvider extends ContentProvider {
    /** Log TAG. */
    private static final String TAG = AppContentProvider.class.getSimpleName();
    /**
     * {@link UriMatcher} to determine what is requested to this
     * {@link ContentProvider}.
     */
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    /** URI ID to get all payments. */
    private static final int PAYMENTS = 100;
    /** URI ID to get a payment. */
    private static final int PAYMENTS_ID = 101;

    /** Local DB Helper */
    private AppDatabase mOpenHelper;

    /**
     * Build and return a {@link UriMatcher} that catches all
     * {@link Uri} variations supported by this {@link
     * ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AppContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, AppContract.Products.PATH, PAYMENTS);
        matcher.addURI(authority, AppContract.Products.PATH + "/*", PAYMENTS_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new AppDatabase(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PAYMENTS:
                return AppContract.Products.CONTENT_TYPE;
            case PAYMENTS_ID:
                return AppContract.Products.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        // Find matching path.
        final int match = sUriMatcher.match(uri);

        // Avoid the expensive string concatenation below if not loggable
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "uri=" + uri + " match=" + match + " proj="
                    + Arrays.toString(projection) + " selection=" + selection
                    + " args=" + Arrays.toString(selectionArgs) + ")");
        }

        // Create a selection builder from Uri.
        final SelectionBuilder builder = buildExpandedSelection(uri, match);

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = builder
                .where(selection, selectionArgs)
                .query(db, projection, sortOrder);
        // Tell the cursor what uri to watch, so it knows when its source
        // data changes
        Context context = getContext();
        if (null != context) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Find matching path.
        final int match = sUriMatcher.match(uri);

        // Avoid the expensive string concatenation below if not loggable
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "insert(uri=" + uri + ", values="
                    + values.toString() + ")");
        }

        // Get the database and run the insert
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (match) {
            case PAYMENTS: {
                db.insertOrThrow(AppDatabase.Tables.PRODUCTS, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                // Generate Uri with remote id.
                return AppContract.Products.buildUri(values.getAsString(
                        AppContract.Products.ID));
            }
            default: {
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
            }
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Find matching path.
        final int match = sUriMatcher.match(uri);

        // Avoid the expensive string concatenation below if not loggable
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "delete(uri=" + uri + ")");
        }

        // Create a selection builder from Uri.
        final SelectionBuilder builder = buildSimpleSelection(uri, match);

        // Get the database and run the delete
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = builder.where(selection, selectionArgs).delete(db);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // Find matching path.
        final int match = sUriMatcher.match(uri);

        // Avoid the expensive string concatenation below if not loggable
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "update(uri=" + uri + ", values="
                    + values.toString() + ")");
        }

        // Create a selection builder from Uri.
        final SelectionBuilder builder = buildSimpleSelection(uri, match);

        // Get the database and run the update
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = builder.where(selection, selectionArgs).update(db, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /** Transactional implementation of applyBatch. */
    @Override
    public ContentProviderResult[] applyBatch(
            ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        ContentProviderResult[] result = new ContentProviderResult[operations
                .size()];
        // Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        // Begin a transaction
        db.beginTransaction();
        try {
            int i = 0;
            for (ContentProviderOperation operation : operations) {
                // Chain the result for back references
                result[i++] = operation.apply(this, result, i);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return result;
    }

    /**
     * Build a simple {@link SelectionBuilder} to match the requested {@link
     * Uri}. This is usually enough to support {@link #insert}, {@link
     * #update}, and {@link #delete} operations.
     */
    private SelectionBuilder buildSimpleSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        switch (match) {
            case PAYMENTS: {
                return builder.table(AppDatabase.Tables.PRODUCTS);
            }
            case PAYMENTS_ID: {
                final String id = AppContract.Products.getId(uri);
                return builder.table(AppDatabase.Tables.PRODUCTS)
                        .where(AppContract.Products.ID + "=?", id);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri for "
                        + match + ": " + uri);
            }
        }
    }

    /**
     * Build an advanced {@link SelectionBuilder} to match the requested {@link
     * Uri}. This is usually only used by {@link #query}, since it
     * performs table joins useful for {@link Cursor} data.
     */
    private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
        switch (match) {
            case PAYMENTS:
            case PAYMENTS_ID:
                return buildSimpleSelection(uri, match);
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }
}
