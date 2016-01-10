package com.ndn.jwtan.identitymanager;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import net.named_data.jndn.Data;
import net.named_data.jndn.Name;
import net.named_data.jndn.security.certificate.IdentityCertificate;
import net.named_data.jndn.security.identity.AndroidSqlite3IdentityStorage;
import net.named_data.jndn.security.identity.FilePrivateKeyStorage;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.IdentityStorage;
import net.named_data.jndn.security.identity.PrivateKeyStorage;
import net.named_data.jndn.util.Blob;

import java.util.List;

/**
 * Created by zhehaowang on 1/9/16.
 */
public class SignAppCertificate extends Activity {

    ////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("zhehao", "sign request received");
        Intent intent = getIntent();

        String encoded = intent.getStringExtra("cert");
        String signerID = intent.getStringExtra("signer_id");
        String appID = intent.getStringExtra("app_id");

        byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
        Blob blob = new Blob(decoded);
        Data data = new Data();
        try {
            data.wireDecode(blob);
            IdentityCertificate certificate = new IdentityCertificate(data);
            String signedCert = signAndRecordCertificate(signerID, certificate, appID);

            Intent result = new Intent();
            result.putExtra("signed_cert", signedCert);

            if (signedCert != "") {
                setResult(Activity.RESULT_OK, result);
            } else {
                setResult(Activity.RESULT_CANCELED, result);
            }
            finish();
        } catch (Exception e) {
            Log.e(getResources().getString(R.string.app_name), e.getMessage());
        } finally {
            finish();
        }
    }

    private String signAndRecordCertificate(String signerID, IdentityCertificate idCert, String appID) {
        String dbPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + MainActivity.DB_NAME;
        String certDirPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + MainActivity.CERT_DIR;

        IdentityStorage identityStorage = new AndroidSqlite3IdentityStorage(dbPath);
        PrivateKeyStorage privateKeyStorage = new FilePrivateKeyStorage(certDirPath);
        IdentityManager identityManager = new IdentityManager(identityStorage, privateKeyStorage);

        try {
            Name idName = new Name(signerID);
            double notBefore = System.currentTimeMillis();
            double notAfter = notBefore + 31556952000.0;
            IdentityCertificate newCertificate = identityManager.prepareUnsignedIdentityCertificate(
                    idCert.getPublicKeyName(), idCert.getPublicKeyInfo(), idName, notBefore, notAfter, (List) null);
            identityManager.signByCertificate(newCertificate, identityManager.getDefaultCertificateNameForIdentity(idName));

            // Gets the data repository in write mode
            DataBaseHelper dbHelper = new DataBaseHelper(getApplicationContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DataBaseSchema.AppEntry.COLUMN_NAME_IDENTITY, signerID);
            values.put(DataBaseSchema.AppEntry.COLUMN_NAME_APP, appID);
            String encoded = Base64.encodeToString(newCertificate.wireEncode().getImmutableArray(), Base64.DEFAULT);
            values.put(DataBaseSchema.AppEntry.COLUMN_NAME_CERTIFICATE, encoded);

            // Insert the new row, returning the primary key value of the new row
            db.insert(
                    DataBaseSchema.AppEntry.TABLE_NAME,
                    null,
                    values);
            return encoded;
        } catch (Exception e) {
            Log.e(getResources().getString(R.string.app_name), e.getMessage());
        }
        return "";
    }
}
