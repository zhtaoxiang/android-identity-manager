package com.ndn.jwtan.identitymanager;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.named_data.jndn.Data;
import net.named_data.jndn.Name;
import net.named_data.jndn.security.certificate.IdentityCertificate;
import net.named_data.jndn.security.identity.AndroidSqlite3IdentityStorage;
import net.named_data.jndn.security.identity.IdentityStorage;
import net.named_data.jndn.util.Blob;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class InstallCertificate extends Activity {

    private static final String mURL = MainActivity.HOST + "/cert/get/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_certificate);

        // Get the message from the intent
        Intent intent = getIntent();
        String name = intent.getStringExtra(UriHandler.EXTRA_MESSAGE_NAME);
        mName = name;

        sendHttpGetRequest();
    }

    ////////////////////////////////////////////////////////////
    public void okay(View view) {
        finish();
    }

    private void sendHttpGetRequest() {
        final String name = mName;

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = null;
        try {
            url = mURL + "?name=" + URLEncoder.encode(name, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(getResources().getString(R.string.app_name), e.getMessage());
        }

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        byte[] decoded = Base64.decode(response, Base64.DEFAULT);
                        Blob blob = new Blob(decoded);
                        Data data = new Data();
                        try {
                            data.wireDecode(blob);
                            IdentityCertificate certificate = new IdentityCertificate(data);
                            TextView organizationText = (TextView) findViewById(R.id.installComplete);
                            organizationText.setText("Certificate installed: " + certificate.getName().toUri());

                            String dbPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + MainActivity.DB_NAME;
                            IdentityStorage identityStorage = new AndroidSqlite3IdentityStorage(dbPath);
                            identityStorage.addCertificate(certificate);
                            identityStorage.setDefaultCertificateNameForKey(certificate.getPublicKeyName(), certificate.getName());
                            identityStorage.setDefaultKeyNameForIdentity(certificate.getPublicKeyName());

                            // Gets the data repository in write mode
                            DataBaseHelper dbHelper = new DataBaseHelper(getApplicationContext());
                            SQLiteDatabase db = dbHelper.getWritableDatabase();

                            // Create a new map of values, where column names are the keys
                            ContentValues values = new ContentValues();
                            Name keyName = certificate.getPublicKeyName();
                            values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_IDENTITY, keyName.getPrefix(keyName.size() - 1).toUri());
                            values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_CERTIFICATE, certificate.getName().toUri());

                            // Insert the new row, returning the primary key value of the new row
                            db.insert(
                                    DataBaseSchema.IdentityEntry.TABLE_NAME,
                                    null,
                                    values);
                        } catch (Exception e) {
                            Log.e(getResources().getString(R.string.app_name), e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            String toastString = "Error code: " + error.networkResponse.statusCode;
                            Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();

                            finish();
                        }
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private String mName;
}
