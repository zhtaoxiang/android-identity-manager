package com.ndn.jwtan.identitymanager;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.named_data.jndn.Name;
import net.named_data.jndn.security.*;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.certificate.IdentityCertificate;
import net.named_data.jndn.security.identity.AndroidSqlite3IdentityStorage;
import net.named_data.jndn.security.identity.FilePrivateKeyStorage;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.IdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.security.identity.PrivateKeyStorage;

import org.json.JSONException;
import org.json.JSONObject;

public class SubmitIdentityRequest extends Activity {

    private final static String mURL = MainActivity.HOST + "/cert-requests/submit/";

    ////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_identity_request);

        // Get the message from the intent
        Intent intent = getIntent();
        mEmail = intent.getStringExtra(UriHandler.EXTRA_MESSAGE_EMAIL);
        mToken = intent.getStringExtra(UriHandler.EXTRA_MESSAGE_TOKEN);
        assignedNamespace = intent.getStringExtra(UriHandler.EXTRA_MESSAGE_NAMESPACE);
        try {
            Log.e("zhehao", mEmail);
            Log.e("zhehao", mToken);
            Log.e("zhehao", assignedNamespace);
        } catch (Exception e) {
            Log.e("zhehao", e.getMessage());
        }

        sendHttpGetRequest();
    }

    public void submitRequest(View view) {
        Button button = (Button) findViewById(R.id.submitRequest);
        button.setEnabled(false);

        // Do something in response to button
        EditText nameText = (EditText) findViewById(R.id.nameText);
        String name = nameText.getText().toString();

        try {
            String certification = generateKey();
            sendHttpPostRequest(name, certification);
        } catch (Exception e) {
            Log.e(getResources().getString(R.string.app_name), e.getMessage());
            finish();
        }
    }

    ////////////////////////////////////////////////////////////
    private String generateKey() throws net.named_data.jndn.security.SecurityException {
        String identity = assignedNamespace;

        String dbPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + MainActivity.DB_NAME;
        String certDirPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + MainActivity.CERT_DIR;

        IdentityStorage identityStorage = new AndroidSqlite3IdentityStorage(dbPath);
        PrivateKeyStorage privateKeyStorage = new FilePrivateKeyStorage(certDirPath);
        IdentityManager identityManager = new IdentityManager(identityStorage, privateKeyStorage);

        Name identityName = new Name(identity);
        Log.e("zhehao", identity);
        Name keyName = identityManager.generateRSAKeyPairAsDefault(identityName, true);
        IdentityCertificate certificate = identityManager.selfSign(keyName);

        String encodedString = Base64.encodeToString(certificate.wireEncode().getImmutableArray(), Base64.DEFAULT);
        return encodedString;
    }

    private void sendHttpGetRequest() {
        final String email = mEmail;
        final String token = mToken;

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = mURL + "?email=" + email + "&token=" + token;
        // Request a string response from the provided URL.
        JsonObjectRequest jsObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // We don't care about organization here
                            //EditText organizationText = (EditText) findViewById(R.id.organizationText);
                            //organizationText.setText(response.getString("organization"));
                        } catch (Exception e) {
                            Log.e(getResources().getString(R.string.app_name), e.getMessage());
                            finish();
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
        queue.add(jsObjectRequest);
    }

    private void sendHttpPostRequest(final String name, final String certification) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, mURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsResponse = new JSONObject(response);

                            DialogFragment newFragment;
                            if (jsResponse.getInt("status") == 200) {
                                newFragment = new MessageDialogFragment(R.string.submit_success);
                                newFragment.show(getFragmentManager(), "message");
                            }
                            else if (jsResponse.getInt("status") == 2) {
                                newFragment = new MessageDialogFragment(R.string.submit_fail);
                                newFragment.show(getFragmentManager(), "message");
                            }
                            else {
                                String toastString = "Error: Full Name field cannot be empty";
                                Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();

                                Button button = (Button) findViewById(R.id.submitRequest);
                                button.setEnabled(true);
                            }
                        }
                        catch (Exception e) {
                            Log.e(getResources().getString(R.string.app_name), e.getMessage());
                            finish();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            String toastString = "Error code: " + error.networkResponse.statusCode + "; msg: " + new String(error.networkResponse.data);
                            Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();

                            finish();
                        }
                    }
                }) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("full_name", name);
                params.put("email", mEmail);
                params.put("token", mToken);
                params.put("cert_request", certification);

                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    ////////////////////////////////////////////////////////////
    private String mEmail;
    private String mToken;
    private String assignedNamespace;
}
