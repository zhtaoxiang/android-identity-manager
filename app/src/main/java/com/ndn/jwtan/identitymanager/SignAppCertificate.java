package com.ndn.jwtan.identitymanager;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import net.named_data.jndn.ControlParameters;
import net.named_data.jndn.ControlResponse;
import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnTimeout;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.certificate.IdentityCertificate;
import net.named_data.jndn.security.identity.AndroidSqlite3IdentityStorage;
import net.named_data.jndn.security.identity.FilePrivateKeyStorage;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.IdentityStorage;
import net.named_data.jndn.security.identity.PrivateKeyStorage;
import net.named_data.jndn.security.policy.SelfVerifyPolicyManager;
import net.named_data.jndn.util.Blob;
import net.named_data.jndn.util.CommandInterestGenerator;

import static android.content.ContentValues.TAG;

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

    private String signAndRecordCertificate(final String signerID, IdentityCertificate idCert, String appID) {
        String dbPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + MainActivity.DB_NAME;
        String certDirPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + MainActivity.CERT_DIR;

        IdentityStorage identityStorage = new AndroidSqlite3IdentityStorage(dbPath);
        PrivateKeyStorage privateKeyStorage = new FilePrivateKeyStorage(certDirPath);
        final IdentityManager identityManager = new IdentityManager(identityStorage, privateKeyStorage);

        final KeyChain keyChain = new KeyChain(identityManager, new SelfVerifyPolicyManager(identityStorage));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    identityManager.setDefaultIdentity(new Name(signerID));
                    Log.d(TAG, "the default certificate is set to be " + keyChain.getDefaultCertificateName().toString());
                    final Face face = new Face();
                    face.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());
                    registerRemotePrefix(signerID, face, keyChain);
                    int counter = 0;
                    // run it for 10 seconds
                    while (counter < 2000) {
                        face.processEvents();
                        Thread.sleep(5);
                        counter ++;
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        ).start();

        try {
            Name signerName = new Name(signerID);
            double notBefore = System.currentTimeMillis();
            double notAfter = notBefore + 31556952000.0;
            IdentityCertificate newCertificate = identityManager.prepareUnsignedIdentityCertificate(
                    idCert.getPublicKeyName(), idCert.getPublicKeyInfo(), signerName, notBefore, notAfter, null);
            identityManager.signByCertificate(newCertificate, identityManager.getDefaultCertificateNameForIdentity(signerName));

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
            db.close();
            return encoded;
        } catch (Exception e) {
            Log.e(getResources().getString(R.string.app_name), e.getMessage());
        }
        return "";
    }

    /**
     * register a back prefix at the remote NFD
     */
    private static void registerRemotePrefix(String prefix, Face face, KeyChain keyChain) {
        Name remotePrefixRegisterPrefix = new Name("/localhop/nfd/rib/register");
        ControlParameters params = new ControlParameters();
        params.setName(new Name(prefix));
        remotePrefixRegisterPrefix.append(params.wireEncode());
        Interest remotePrefixRegisterInterest = new Interest(remotePrefixRegisterPrefix);
        Log.d("registerRemotePrefix", "try to register " + prefix);
        try {
            CommandInterestGenerator cmg = new CommandInterestGenerator();
            cmg.generate(remotePrefixRegisterInterest, keyChain, keyChain.getDefaultCertificateName());
            face.expressInterest(remotePrefixRegisterInterest, new OnData() {
                @Override
                public void onData(Interest interest, Data data) {
                    ControlResponse resp = new ControlResponse();
                    try {
                        resp.wireDecode(data.getContent());
                        if (resp.getStatusCode() == 200) {
                            Log.d("registerRemotePrefix", "succeeded");
                        } else {
                            Log.d("registerRemotePrefix", "failed");
                        }
                    } catch (EncodingException e) {
                        e.printStackTrace();
                    }
                }
            }, new OnTimeout() {
                @Override
                public void onTimeout(Interest interest) {
                    System.out.println("Time out for interest " + interest.getName().toUri());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
