package com.ndn.jwtan.identitymanager;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import android.util.Log;

public class DisplayIdentities extends ListActivity {

    public final static String EXTRA_MESSAGE_IDENTITY = "com.ndn.jwtan.identitymanager.MESSAGE_IDENTITY";

    // Here instead of the user identities that we have locally,
    // we really want to display the list of device identities, as we have public keys for those,
    // and can use those for signing application data
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        TextView view = (TextView) v.findViewById(R.id.item);
        String identity = (String) view.getText();

        Intent intent = new Intent(this, DisplayApps.class);
        intent.putExtra(EXTRA_MESSAGE_IDENTITY, identity);
        startActivity(intent);
    }
}
