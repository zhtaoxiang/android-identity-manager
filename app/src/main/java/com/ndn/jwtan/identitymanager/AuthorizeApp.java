package com.ndn.jwtan.identitymanager;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class AuthorizeApp extends ListActivity {
    String mAppCategory = "";
    String mAppCertName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        mAppCategory = intent.getStringExtra("app_category");
        mAppCertName = intent.getStringExtra("cert_name");

        super.onCreate(savedInstanceState);

        // Establish Database connection
        DataBaseHelper dbHelper = new DataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DataBaseSchema.DeviceEntry.COLUMN_NAME_IDENTITY + " DESC";

        String[] projection = {
                DataBaseSchema.DeviceEntry._ID,
                DataBaseSchema.DeviceEntry.COLUMN_NAME_IDENTITY,
                DataBaseSchema.DeviceEntry.COLUMN_NAME_CERTIFICATE
        };

        Cursor c = db.query(
                DataBaseSchema.DeviceEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );


        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {DataBaseSchema.DeviceEntry.COLUMN_NAME_IDENTITY, DataBaseSchema.DeviceEntry.COLUMN_NAME_CERTIFICATE};
        int[] toViews = {R.id.item, R.id.description}; // The TextView in simple_list_item_1

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.list_item,
                c,
                fromColumns,
                toViews,
                0);

        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent resultIntent = new Intent();
        TextView view = (TextView) v.findViewById(R.id.item);
        String identity = (String) view.getText();

        // TODO: namespace check, and application identity implementation
        resultIntent.putExtra("signer_id", identity);
        setResult(0, resultIntent);
        finish();
    }
}
