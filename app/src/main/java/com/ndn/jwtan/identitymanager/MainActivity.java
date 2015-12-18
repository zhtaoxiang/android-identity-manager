package com.ndn.jwtan.identitymanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;


public class MainActivity extends Activity {

    protected static final String DB_NAME = "certDb.db";
    protected static final String CERT_DIR = "certDir";
    // TODO: Stuck at submit email address without timeout if HOST misconfigured.
    // On memoria, my ICN chat cert runs on 5000, while the openmhealth cert runs on 5001
    protected static final String HOST = "http://memoria.ndn.ucla.edu:5001";
    protected static final String FLAG = "mobileApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
    public void generateIdentity(View view) {
        Intent intent = new Intent(this, GenerateIdentity.class);
        startActivity(intent);
    }

    /** Called when the user clicks the trace all identities button */
    public void traceIdentities(View view) {
        Intent intent = new Intent(this, DisplayIdentities.class);
        startActivity(intent);
    }
}
