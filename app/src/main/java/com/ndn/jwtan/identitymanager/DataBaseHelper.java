package com.ndn.jwtan.identitymanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "identityManager.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_IDENTITES =
            "CREATE TABLE " + DataBaseSchema.IdentityEntry.TABLE_NAME + " (" +
                    DataBaseSchema.IdentityEntry._ID + " INTEGER PRIMARY KEY," +
                    DataBaseSchema.IdentityEntry.COLUMN_NAME_IDENTITY + TEXT_TYPE + " UNIQUE," +
                    DataBaseSchema.IdentityEntry.COLUMN_NAME_CERTIFICATE + TEXT_TYPE + ")";

    private static final String SQL_DELETE_IDENTITIES =
            "DROP TABLE IF EXISTS " + DataBaseSchema.IdentityEntry.TABLE_NAME;

    private static final String SQL_CREATE_APPS =
            "CREATE TABLE " + DataBaseSchema.AppEntry.TABLE_NAME + " (" +
                    DataBaseSchema.AppEntry._ID + " INTEGER PRIMARY KEY," +
                    DataBaseSchema.AppEntry.COLUMN_NAME_IDENTITY + TEXT_TYPE + COMMA_SEP +
                    DataBaseSchema.AppEntry.COLUMN_NAME_APP + TEXT_TYPE + COMMA_SEP +
                    DataBaseSchema.AppEntry.COLUMN_NAME_CERTIFICATE + TEXT_TYPE + ")";

    private static final String SQL_DELETE_APPS =
            "DROP TABLE IF EXISTS " + DataBaseSchema.AppEntry.TABLE_NAME;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_IDENTITES);
        db.execSQL(SQL_CREATE_APPS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_IDENTITIES);
        db.execSQL(SQL_DELETE_APPS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // Getting All Identities
    public List<String> getAllIdentities() {
        List<String> identities = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DataBaseSchema.IdentityEntry.COLUMN_NAME_IDENTITY + " DESC";

        String[] projection = {
                DataBaseSchema.IdentityEntry.COLUMN_NAME_IDENTITY
        };

        Cursor cursor = db.query(
                DataBaseSchema.IdentityEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding contact to list
                identities.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        // return contact list
        return identities;
    }

    // Getting All Apps Signed by Given Identity
    public List<String> getAllApps(String identity) {
        List<String> apps = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DataBaseSchema.AppEntry.COLUMN_NAME_APP + " DESC";

        String[] returnProjection = {
                DataBaseSchema.AppEntry.COLUMN_NAME_APP
        };

        String[] whereProjection = {
                identity
        };

        Cursor cursor = db.query(
                DataBaseSchema.AppEntry.TABLE_NAME,                      // The table to query
                returnProjection,                                        // The columns to return
                DataBaseSchema.AppEntry.COLUMN_NAME_IDENTITY + " = ?",   // The columns for the WHERE clause
                whereProjection,                                         // The values for the WHERE clause
                null,                                                    // don't group the rows
                null,                                                    // don't filter by row groups
                sortOrder                                                // The sort order
        );

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding contact to list
                apps.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        // return contact list
        return apps;
    }
}
