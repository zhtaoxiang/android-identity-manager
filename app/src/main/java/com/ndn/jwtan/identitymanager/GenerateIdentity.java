package com.ndn.jwtan.identitymanager;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class GenerateIdentity extends Activity {

    private final static String mURL = MainActivity.HOST + "/tokens/request/";

    ////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_identity);
    }

    public void submitEmail(View view) {
        Button button = (Button) findViewById(R.id.submitEmail);
        button.setEnabled(false);

        // Do something in response to button
        EditText editText = (EditText) findViewById(R.id.emailText);
        String email = editText.getText().toString();

        sendHttpRequest(email);
    }

    ////////////////////////////////////////////////////////////
    private void sendHttpRequest(final String email) {
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
                            if (jsResponse.get("status") == 0)
                                newFragment = new MessageDialogFragment(R.string.token_success);
                            else
                                newFragment = new MessageDialogFragment(R.string.token_fail);

                            newFragment.show(getFragmentManager(), "message");
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
                            String toastString = "Error code: " + error.networkResponse.statusCode;
                            Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();

                            finish();
                        }
                    }
                }) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("flag", MainActivity.FLAG);

                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
