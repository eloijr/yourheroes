package com.runze.yourheroes.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * Created by Eloi Jr on 18/02/2015.
 */
public class YourHeroesProvider extends ContentProvider {

    private static final UriMatcher fUriMatcher = buildUriMatcher();
    private YourHeroesDbHelper fDbHelper;

    private static final int PERSON = 100;
    private static final int PERSON_ID = 101;
    private static final int PERSON_STARTNAME = 102;

    @Override
    public boolean onCreate() {
        fDbHelper = new YourHeroesDbHelper(getContext());
        return true;
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = YourHeroesContract.CONTENT_AUTHORITY;

        // Each type of uri with you corresponding code!
        matcher.addURI(authority, YourHeroesContract.PATH_PERSON, PERSON_ID);
        matcher.addURI(authority, YourHeroesContract.PATH_PERSON, PERSON_STARTNAME);

        return matcher;
    }

    @Override
    public String getType(Uri uri) {
        // Verifying whats kind o uri we have...
        final int match = fUriMatcher.match(uri);
        switch (match) {
            case PERSON_ID:
                return YourHeroesContract.PersonEntry.CONTENT_ITEM_TYPE;
            case PERSON_STARTNAME:
                return YourHeroesContract.PersonEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor rCursor;

        switch (fUriMatcher.match(uri)) {
            case PERSON_ID:
                rCursor = fDbHelper.getReadableDatabase().query(
                        YourHeroesContract.PersonEntry.TABLE_NAME,
                        projection,
                        YourHeroesContract.PersonEntry._ID + " = " + ContentUris.parseId(uri),
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;

            case PERSON_STARTNAME:
                rCursor = fDbHelper.getReadableDatabase().query(
                        YourHeroesContract.PersonEntry.TABLE_NAME,
                        projection,
                        YourHeroesContract.PersonEntry.COLUMN_NAME + " starting with '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        rCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return rCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = fDbHelper.getWritableDatabase();
        final int match = fUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case PERSON: {
                long _id = db.insert(YourHeroesContract.PersonEntry.TABLE_NAME, null, values);
                if (_id  > 0)
                    returnUri = YourHeroesContract.PersonEntry.buildPersonUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = fDbHelper.getWritableDatabase();
        final int match = fUriMatcher.match(uri);
        switch (match) {
            case PERSON:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(YourHeroesContract.PersonEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = fDbHelper.getWritableDatabase();
        final int match = fUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case PERSON:
                rowsDeleted = db.delete(YourHeroesContract.PersonEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (selection == null || rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = fDbHelper.getWritableDatabase();
        final int match = fUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case PERSON:
                rowsUpdated = db.update(YourHeroesContract.PersonEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
