package com.ndn.jwtan.identitymanager;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// For getting IMEI
import android.telephony.TelephonyManager;
import java.util.UUID;
import android.content.Context;

// For getting Android ID: This ID will change upon factory reset, and can be changed in rooted phones
//import android.provider.Settings.Secure;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.named_data.jndn.Data;
import net.named_data.jndn.Name;
import net.named_data.jndn.security.*;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.certificate.IdentityCertificate;
import net.named_data.jndn.security.identity.AndroidSqlite3IdentityStorage;
import net.named_data.jndn.security.identity.FilePrivateKeyStorage;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.IdentityStorage;
import net.named_data.jndn.security.identity.PrivateKeyStorage;
import net.named_data.jndn.util.Blob;
import net.named_data.jndn.security.certificate.PublicKey;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;

public class InstallCertificate extends AppCompatActivity {

    private static final String mURL = MainActivity.HOST + "/cert/get/";
    private UICustomViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_token_and_identity);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icon_filled));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icon_filled));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icon_filled));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icon_filled));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (UICustomViewPager) findViewById(R.id.pager);
        final UICreateIDPageAdapter adapter = new UICreateIDPageAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), getResources().getString(R.string.please_wait));

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

        viewPager.setCurrentItem(3);

        // Get the message from the intent
        Intent intent = getIntent();
        String name = intent.getStringExtra(UriHandler.EXTRA_MESSAGE_NAME);
        mName = name;

        sendHttpGetRequest();
    }

    public void returnClick(View view) {
        Intent i = new Intent(InstallCertificate.this, MainActivity.class);
        startActivity(i);
        return;
    }

    ////////////////////////////////////////////////////////////
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
                            Name identityName = keyName.getPrefix(keyName.size() - 1);
                            values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_CERTIFICATE, certificate.getName().toUri());
                            values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_APPROVED, true);

                            // Insert the new row, returning the primary key value of the new row
                            int res = db.update(
                                    DataBaseSchema.IdentityEntry.TABLE_NAME,
                                    values,
                                    DataBaseSchema.IdentityEntry.COLUMN_NAME_IDENTITY + " = '" + identityName.toUri() + "'",
                                    null);

                            if (res != 1) {
                                Log.e(getResources().getString(R.string.app_name),
                                        "Unexpected number of updates: " + Integer.toString(res));
                            }

                            // Upon successful user identity request, try generating device identity
                            try {
                                generateAndInstallDeviceCertificate(identityName, certificate.getName());
                            } catch (SecurityException e) {
                                Log.e(getResources().getString(R.string.app_name), e.getMessage());
                            }

                            String hint = "Certificate installed: " + certificate.getName().toUri();
                            TextView hintText = (TextView) findViewById(R.id.step4Hint);
                            hintText.setText(hint);
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

    /**
     * Device identity generation function;
     *   A device identity is generated when one does not already exist, and a user namespace is granted successfully,
     *   either for the first time by the remote website, or by some device that has the user identity and has chosen to authorize this device
     */

    private Boolean generateAndInstallDeviceCertificate(Name userIdentityName, Name userCertificateName) throws net.named_data.jndn.security.SecurityException {
        // Device UUID generation taken from http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        Log.d("device_key", deviceId);

        String dbPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + MainActivity.DB_NAME;
        String certDirPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + MainActivity.CERT_DIR;

        IdentityStorage identityStorage = new AndroidSqlite3IdentityStorage(dbPath);
        PrivateKeyStorage privateKeyStorage = new FilePrivateKeyStorage(certDirPath);
        IdentityManager identityManager = new IdentityManager(identityStorage, privateKeyStorage);

        Name identityName = new Name(userIdentityName).append(deviceId);

        // For now, we always generate a new device certificate upon successful user identity request
        // TODO: behavior when a device identity already exists?
        if (true) {
            Name keyName = identityManager.generateRSAKeyPairAsDefault(identityName, true);
            PublicKey publicKey = identityManager.getPublicKey(keyName);

            // We create cert that is good for 2 years by default
            Calendar calendar = Calendar.getInstance();
            double notBefore = (double)calendar.getTimeInMillis();
            calendar.add(Calendar.YEAR, 2);
            double notAfter = (double)calendar.getTimeInMillis();
            try {
                // This method throws Error: AndroidSqlite3IdentityStorage.getCertificate for !allowAny is not implemented from library
                Name certificatePrefix = new Name(keyName).getPrefix(-1).append("KEY").append(keyName.get(-1));
                IdentityCertificate certificate = identityManager.createIdentityCertificate(certificatePrefix, publicKey, userCertificateName, notBefore, notAfter);

                DataBaseHelper dbHelper = new DataBaseHelper(getApplicationContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                // Insert the new row, returning the primary key value of the new row
                ContentValues values = new ContentValues();
                values.put(DataBaseSchema.DeviceEntry.COLUMN_NAME_IDENTITY, identityName.toUri());
                values.put(DataBaseSchema.DeviceEntry.COLUMN_NAME_DEVICE, deviceId);
                values.put(DataBaseSchema.DeviceEntry.COLUMN_NAME_CERTIFICATE, certificate.getName().toUri());

                db.insert(
                        DataBaseSchema.DeviceEntry.TABLE_NAME,
                        null,
                        values);
                return true;
            } catch (SecurityException e) {
                Log.e(getResources().getString(R.string.app_name), e.getMessage());
                return false;
            }

        } else {
            Log.d("device_key", "Device key for this user identity already exists.");
            return false;
        }
    }
}