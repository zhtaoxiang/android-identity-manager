package com.ndn.jwtan.identitymanager;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.List;


public class DisplayApps extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the message from the intent
        Intent intent = getIntent();
        String identity = intent.getStringExtra(DisplayIdentities.EXTRA_MESSAGE_IDENTITY);

        // Establish Database connection
        DataBaseHelper dbHelper = new DataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DataBaseSchema.AppEntry.COLUMN_NAME_APP + " DESC";

        String[] returnProjection = {
                DataBaseSchema.AppEntry._ID,
                DataBaseSchema.AppEntry.COLUMN_NAME_IDENTITY,
                DataBaseSchema.AppEntry.COLUMN_NAME_APP,
                DataBaseSchema.AppEntry.COLUMN_NAME_CERTIFICATE
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


        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {DataBaseSchema.AppEntry.COLUMN_NAME_APP, DataBaseSchema.AppEntry.COLUMN_NAME_CERTIFICATE};
        int[] toViews = {R.id.item, R.id.description}; // The TextView in simple_list_item_1

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.list_item,
                cursor,
                fromColumns,
                toViews,
                0);

        setListAdapter(adapter);
    }
}
