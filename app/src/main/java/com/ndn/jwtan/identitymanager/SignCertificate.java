package com.ndn.jwtan.identitymanager;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.named_data.jndn.Data;
import net.named_data.jndn.Name;
import net.named_data.jndn.Sha256WithRsaSignature;
import net.named_data.jndn.Signature;
import net.named_data.jndn.security.certificate.IdentityCertificate;
import net.named_data.jndn.security.identity.AndroidSqlite3IdentityStorage;
import net.named_data.jndn.security.identity.FilePrivateKeyStorage;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.IdentityStorage;
import net.named_data.jndn.security.identity.PrivateKeyStorage;
import net.named_data.jndn.util.Blob;

import java.util.List;


public class SignCertificate extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the intent that started this activity
        Intent intent = getIntent();
        m_appName = intent.getStringExtra(Intent.EXTRA_TITLE);

        String encoded = intent.getStringExtra(Intent.EXTRA_TEXT);
        byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
        Blob blob = new Blob(decoded);
        Data data = new Data();
        try {
            data.wireDecode(blob);
            m_certificate = new IdentityCertificate(data);
        } catch (Exception e) {
            Log.e(getResources().getString(R.string.app_name), e.getMessage());
        }

        DataBaseHelper dbHelper = new DataBaseHelper(getApplicationContext());
        List<String> values = dbHelper.getAllIdentities();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //TODO: sign the dskCert with the selected identity
        String identity = (String) getListAdapter().getItem(position);
        String dbPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + MainActivity.DB_NAME;
        String certDirPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + MainActivity.CERT_DIR;

        IdentityStorage identityStorage = new AndroidSqlite3IdentityStorage(dbPath);
        PrivateKeyStorage privateKeyStorage = new FilePrivateKeyStorage(certDirPath);
        IdentityManager identityManager = new IdentityManager(identityStorage, privateKeyStorage);

        try {
            Name idName = new Name(identity);
            double notBefore = System.currentTimeMillis();
            double notAfter = notBefore + 31556952000.0;
            IdentityCertificate newCertificate = identityManager.prepareUnsignedIdentityCertificate(
                    m_certificate.getPublicKeyName(), m_certificate.getPublicKeyInfo(), idName, notBefore, notAfter, (List) null);
            identityManager.signByCertificate(newCertificate, identityManager.getDefaultCertificateNameForIdentity(idName));

            // Gets the data repository in write mode
            DataBaseHelper dbHelper = new DataBaseHelper(getApplicationContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(DataBaseSchema.AppEntry.COLUMN_NAME_IDENTITY, identity);
            values.put(DataBaseSchema.AppEntry.COLUMN_NAME_APP, m_appName);
            values.put(DataBaseSchema.AppEntry.COLUMN_NAME_CERTIFICATE, m_certificate.getName().toUri());

            // Insert the new row, returning the primary key value of the new row
            db.insert(
                    DataBaseSchema.AppEntry.TABLE_NAME,
                    null,
                    values);

            // Create intent to deliver some kind of result data
            Intent result = new Intent(getApplicationContext().getPackageName() + ".RESULT_ACTION");
            result.putExtra(Intent.EXTRA_TEXT, Base64.encodeToString(newCertificate.wireEncode().getImmutableArray(), Base64.DEFAULT));
            setResult(Activity.RESULT_OK, result);
        } catch (Exception e) {
            Log.e(getResources().getString(R.string.app_name), e.getMessage());
        }

        finish();
    }

    private IdentityCertificate m_certificate;
    private String m_appName;
}
