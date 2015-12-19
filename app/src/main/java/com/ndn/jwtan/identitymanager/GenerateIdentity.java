package com.ndn.jwtan.identitymanager;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

public class GenerateIdentity extends AppCompatActivity {

    private final static String mURL = MainActivity.HOST + "/tokens/request/";
    private String caption = "";
    private String picture = "";

    private UICustomViewPager viewPager;

    ////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_identity);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Step 1"));
        tabLayout.addTab(tabLayout.newTab().setText("Step 2"));
        tabLayout.addTab(tabLayout.newTab().setText("Step 3"));
        tabLayout.addTab(tabLayout.newTab().setText("Step 4"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (UICustomViewPager) findViewById(R.id.pager);
        final UICreateIDPageAdapter adapter = new UICreateIDPageAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());

        // Disabling clicking on tabs to switch
        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        /*
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        */
    }

    public void submitEmail(View view) {
        Button button = (Button) findViewById(R.id.submitEmail);
        button.setEnabled(false);

        // Do something in response to button
        EditText editText = (EditText) findViewById(R.id.emailText);
        String email = editText.getText().toString();

        this.caption = "caption";
        this.picture = "picture";

        sendHttpRequest(email);
    }

    public void tab1Click(View view) {
        viewPager.setCurrentItem(1);
    }

    public void declineClick(View view) {
        Intent i = new Intent(GenerateIdentity.this, MainActivity.class);
        startActivity(i);
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public void tab2Click(View view) {
        EditText editText = (EditText) findViewById(R.id.emailText);
        String email = editText.getText().toString();

        EditText editID = (EditText) findViewById(R.id.idNameText);
        String idName = editText.getText().toString();

        if (isValidEmailAddress(email)) {
            if (idName != "") {
                viewPager.setCurrentItem(2);
            } else {
                String toastString = "Please give an identity name";
                Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();
            }
        } else {
            String toastString = "Please put valid email address";
            Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();
        }
    }

    public void tab0Click(View view) {
        viewPager.setCurrentItem(0);
    }

    ////////////////////////////////////////////////////////////
    private void sendHttpRequest(final String email) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // for passing into the new Response.Listener
        final String caption = this.caption;
        final String picture = this.picture;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, mURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsResponse = new JSONObject(response);
                            if (jsResponse.getInt("status") == 200) {
                                DataBaseHelper dbHelper = new DataBaseHelper(getApplicationContext());
                                SQLiteDatabase db = dbHelper.getWritableDatabase();

                                // Insert the new row, returning the primary key value of the new row
                                ContentValues values = new ContentValues();
                                values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_IDENTITY, jsResponse.getString("assigned_namespace"));
                                values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_APPROVED, false);
                                values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_CAPTION, caption);
                                values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_PICTURE, picture);

                                db.insert(
                                        DataBaseSchema.IdentityEntry.TABLE_NAME,
                                        null,
                                        values);
                            }
                            else {
                                DialogFragment newFragment = new MessageDialogFragment(R.string.token_fail);
                                newFragment.show(getFragmentManager(), "message");
                            }
                            viewPager.setCurrentItem(3);
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

                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
