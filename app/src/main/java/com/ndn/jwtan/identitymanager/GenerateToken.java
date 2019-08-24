package com.ndn.jwtan.identitymanager;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class GenerateToken extends AppCompatActivity {

    private final static String mURL = MainActivity.HOST + "/tokens/request/";
    private String caption = "";
    private String picture = "";

    private UICustomViewPager viewPager;
    private int selectedImageViewId = -1;
    /*
    private static int RESULT_LOAD_IMAGE = 1;
    public static final int KITKAT_VALUE = 1002;
    */
    private TabLayout.Tab tab0;
    private TabLayout.Tab tab1;
    private TabLayout.Tab tab2;
    private TabLayout.Tab tab3;

    ////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_token_and_identity);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tab0 = tabLayout.newTab().setIcon(R.drawable.icon_filled);
        tab1 = tabLayout.newTab().setIcon(R.drawable.icon_empty);
        tab2 = tabLayout.newTab().setIcon(R.drawable.icon_empty);
        tab3 = tabLayout.newTab().setIcon(R.drawable.icon_empty);
        tabLayout.addTab(tab0);
        tabLayout.addTab(tab1);
        tabLayout.addTab(tab2);
        tabLayout.addTab(tab3);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (UICustomViewPager) findViewById(R.id.pager);
        final UICreateIDPageAdapter adapter = new UICreateIDPageAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), getResources().getString(R.string.token_success), false);

        // Disabling clicking on tabs to switch
        LinearLayout tabStrip = ((LinearLayout) tabLayout.getChildAt(0));
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
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

        // Disabled picking from gallery for now
        /*
        Intent intent;

        if (Build.VERSION.SDK_INT < 19){
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, KITKAT_VALUE);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, KITKAT_VALUE);
        }
        */

    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            ImageView imageView = (ImageView) findViewById(R.id.imageView1);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }
    */

    public void submitImage(View view) {
        Button button = (Button) findViewById(R.id.submitEmail);
        button.setEnabled(false);

        // Do something in response to button
        EditText editText = (EditText) findViewById(R.id.emailText);
        String email = editText.getText().toString();

        sendHttpRequest(email);
    }

    public void imageViewClick(View view) {
        CustomImageViewer v = (CustomImageViewer) view;
        // test: getDrawable() gives null with "background" instead of "src"
        //overlay is black with transparency of 0x77 (119)
        if (!v.selected) {
            v.getDrawable().setColorFilter(0x33000000, PorterDuff.Mode.MULTIPLY);
            v.selected = true;
            if (this.selectedImageViewId != -1) {
                CustomImageViewer oriV = (CustomImageViewer) findViewById(this.selectedImageViewId);
                imageViewClick(oriV);
            }
            this.picture = (String) v.getTag();
            this.selectedImageViewId = v.getId();
        } else {
            v.getDrawable().setColorFilter(0xFF000000, PorterDuff.Mode.MULTIPLY);
            v.selected = false;
            this.picture = "";
            this.selectedImageViewId = -1;
        }
        v.invalidate();
    }

    public void returnClick(View view) {
        Intent i = new Intent(GenerateToken.this, MainActivity.class);
        startActivity(i);
        return;
    }

    public void tab1Click(View view) {
        viewPager.setCurrentItem(1);
        tab1.setIcon(R.drawable.icon_filled);
    }

    public void declineClick(View view) {
        Intent i = new Intent(GenerateToken.this, MainActivity.class);
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
        String idName = editID.getText().toString();

        if (isValidEmailAddress(email)) {
            if (idName != "") {
                this.caption = idName;
                viewPager.setCurrentItem(2);
                tab2.setIcon(R.drawable.icon_filled);
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

        final CustomImageViewer oriV;
        // resets the selected image
        if (this.selectedImageViewId != -1) {
            oriV = (CustomImageViewer) findViewById(this.selectedImageViewId);
        } else {
            oriV = null;
        }

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

                            TextView emailView = (TextView) findViewById(R.id.step4Email);
                            emailView.setText(email);
                            emailView.setVisibility(View.VISIBLE);

                            TextView idNameView = (TextView) findViewById(R.id.step4IdName);
                            idNameView.setText(caption);
                            idNameView.setVisibility(View.VISIBLE);

                            ImageView profileImageView = (ImageView) findViewById(R.id.step4ImageView);
                            // Could need a better approach to clear
                            profileImageView.setImageResource(0);
                            profileImageView.setImageResource(getResources().getIdentifier(picture, "drawable", getPackageName()));
                            profileImageView.setVisibility(View.VISIBLE);

                            Button returnBtn = (Button) findViewById(R.id.returnBtn);
                            returnBtn.setEnabled(true);

                            viewPager.setCurrentItem(3);
                            tab3.setIcon(R.drawable.icon_filled);
                            if (oriV != null) {
                                imageViewClick(oriV);
                            }
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
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);

                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void backImage(View view) {
        tab1Click(view);
    }
}
