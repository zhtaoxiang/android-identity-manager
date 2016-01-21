package com.ndn.jwtan.identitymanager;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    protected static final String DB_NAME = "certDb.db";
    protected static final String CERT_DIR = "certDir";
    // TODO: Stuck at submit email address without timeout if HOST misconfigured.
    // On memoria, my ICN chat cert runs on 5000, while the openmhealth cert runs on 5001
    protected static final String HOST = "http://memoria.ndn.ucla.edu:5001";
    private static final String slotTaken = "used";

    private String usage = "main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();

        String appID = intent.getStringExtra("app_id");
        if (appID != null) {
            usage = "authorize";
        }

        Log.e("zhehao", usage);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getIdentities();
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Log.e("zhehao", "on resume");
        getIdentities();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Called when the user clicks the generate a new identity button */
    public void generateOrViewIdentity(View view) {
        FloatingActionButton fab = (FloatingActionButton) view;
        if (fab.getTag(R.string.tags_if_taken) == slotTaken) {
            if (usage == "authorize") {
                //authorizeApp((String) fab.getTag(R.string.tags_id_name));
                Intent resultIntent = new Intent();
                // Although we have a device ID, the user ID is used to sign app ID
                resultIntent.putExtra("prefix", (String) fab.getTag(R.string.tags_id_name));
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                traceApplications((String)fab.getTag(R.string.tags_id_name));
            }
        } else {
            Intent intent = new Intent(this, GenerateToken.class);
            startActivity(intent);
        }
    }

    // Always exit the application when back is pressed for Main activity,
    // instead of potentially returning to "action finished", or "create identity"
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // Although we have a device ID, the user ID is used to sign app ID
    public void getIdentities() {
        // Establish Database connection
        DataBaseHelper dbHelper = new DataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DataBaseSchema.IdentityEntry.COLUMN_NAME_IDENTITY + " DESC";

        String[] projection = {
                DataBaseSchema.IdentityEntry._ID,
                DataBaseSchema.IdentityEntry.COLUMN_NAME_CAPTION,
                DataBaseSchema.IdentityEntry.COLUMN_NAME_PICTURE,
                DataBaseSchema.IdentityEntry.COLUMN_NAME_IDENTITY
        };

        String[] whereClause = {
                "1"
        };

        Cursor c = db.query(
                DataBaseSchema.IdentityEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                DataBaseSchema.IdentityEntry.COLUMN_NAME_APPROVED + "=?",                                     // The columns for the WHERE clause
                whereClause,                              // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        Integer idx = 0;
        // temporary maxIdx for static layout
        int maxIdx = 6;
        while (c.moveToNext() && idx < maxIdx) {
            String tvId = "tv" + idx.toString();
            TextView tv = (TextView) this.findViewById(getResources().getIdentifier(tvId, "id", getPackageName()));
            tv.setText(c.getString(1));
            String fabId = "fab" + idx.toString();
            FloatingActionButton fab = (FloatingActionButton) this.findViewById(getResources().getIdentifier(fabId, "id", getPackageName()));
            if (c.getString(2) != "") {
                fab.setImageResource(getResources().getIdentifier(c.getString(2), "drawable", getPackageName()));
            }
            fab.setTag(R.string.tags_if_taken, slotTaken);
            fab.setTag(R.string.tags_id_name, c.getString(3));
            idx += 1;
        }

        c.close();
        db.close();
    }

    public void traceApplications(String idName) {
        Intent intent = new Intent(this, DisplayApps.class);
        intent.putExtra(DisplayApps.EXTRA_MESSAGE_IDENTITY, idName);
        startActivity(intent);
    }

    /** Called when the user clicks the trace all identities button */
    public void traceIdentities(View view) {
        Intent intent = new Intent(this, DisplayIdentities.class);
        startActivity(intent);
    }
}
