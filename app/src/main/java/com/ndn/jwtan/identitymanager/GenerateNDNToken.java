package com.ndn.jwtan.identitymanager;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.NetworkNack;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnNetworkNack;
import net.named_data.jndn.OnTimeout;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.pib.AndroidSqlite3Pib;
import net.named_data.jndn.security.pib.PibImpl;
import net.named_data.jndn.security.tpm.TpmBackEndFile;
import net.named_data.jndn.security.v2.CertificateV2;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static com.ndn.jwtan.identitymanager.Constants.STATUS_FAILURE;
import static com.ndn.jwtan.identitymanager.Constants.STATUS_SUCCESS;
import static java.lang.Thread.sleep;

class ClientCaItem {
    CertificateV2 m_anchor;
    Name m_caName;
    String m_caInfo;
    String m_probe;
    String cert;

    CertificateV2 getAnchor() {
        if (m_anchor != null) return m_anchor;
        m_anchor = new CertificateV2();
        try {
            m_anchor.wireDecode(ByteBuffer.wrap(Base64.decode(cert, 0)));
        } catch (EncodingException e) {
            Timber.e("error");
            e.printStackTrace();
        }
        return m_anchor;
    }

    CertificateV2 getAnchor(byte[] certBytes) {
        CertificateV2 m_anchor = new CertificateV2();
        try {
            m_anchor.wireDecode(ByteBuffer.wrap(Base64.decode(certBytes, 0)));
        } catch (EncodingException e) {
            Timber.e("error");
            e.printStackTrace();
        }
        return m_anchor;
    }

    @Override
    public String toString() {
        return m_caName.toString();
    }
}

public class GenerateNDNToken extends AppCompatActivity {

    private final static String mURL = MainActivity.HOST + "/tokens/request/";
    private String caption = "";
    private String picture = "";

    private UICustomViewPager viewPager;
    private int selectedImageViewId = -1;

    private TabLayout.Tab tab0;
    private TabLayout.Tab tab1;
    private TabLayout.Tab tab2;
    private TabLayout.Tab tab3;
    private TabLayout.Tab tab4;
    private TabLayout.Tab tab5;
    private TabLayout.Tab tab6;

    private Client client;
    String challengeType;
    ChallengeFactory challengeFactory;
    JSONObject requirement;

    NDNCertModel ndnCertModel;
    Face face = new Face();

    ////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_ndn_token);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tab0 = tabLayout.newTab().setIcon(R.drawable.icon_filled);
        tab1 = tabLayout.newTab().setIcon(R.drawable.icon_empty);
        tab2 = tabLayout.newTab().setIcon(R.drawable.icon_empty);
        tab3 = tabLayout.newTab().setIcon(R.drawable.icon_empty);
        tab4 = tabLayout.newTab().setIcon(R.drawable.icon_empty);
        tab5 = tabLayout.newTab().setIcon(R.drawable.icon_empty);
        tab6 = tabLayout.newTab().setIcon(R.drawable.icon_empty);
        tabLayout.addTab(tab0);
        tabLayout.addTab(tab1);
        tabLayout.addTab(tab2);
        tabLayout.addTab(tab3);
        tabLayout.addTab(tab4);
        tabLayout.addTab(tab5);
        tabLayout.addTab(tab6);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (UICustomViewPager) findViewById(R.id.pager);
        final UICreateNDNIDPageAdapter adapter = new UICreateNDNIDPageAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());

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

        String rootPath = getApplicationContext().getFilesDir().toString();
        AndroidSqlite3Pib m_pib = null;
        TpmBackEndFile m_tpm;

        try {
            m_pib = new AndroidSqlite3Pib(rootPath, "/pib.db");
        } catch (PibImpl.Error e) {
            Timber.e("error");
            e.printStackTrace();
        }

        m_tpm = new TpmBackEndFile(TpmBackEndFile.getDefaultDirecoryPath(getApplicationContext().getFilesDir()));
        try {
            m_pib.setTpmLocator("tpm-file:" + TpmBackEndFile.getDefaultDirecoryPath(getApplicationContext().getFilesDir()));
        } catch (PibImpl.Error e) {
            Timber.e("error");
            e.printStackTrace();
        }

        KeyChain keyChain = null;
        try {
            keyChain = new KeyChain(m_pib, m_tpm);
        } catch (PibImpl.Error e) {
            e.printStackTrace();
            Timber.e("error");
        }
        client = new Client(keyChain);

        Client client = new Client(keyChain);
        ndnCertModel = ViewModelProviders.of(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new NDNCertModel();
            }
        }).get(NDNCertModel.class);

        if (ndnCertModel.client == null) {
            ndnCertModel.client = client;
        }

        ClientCaItem caItem = new ClientCaItem();
        caItem.m_caName = new Name("/ndn");
        caItem.m_caInfo = "An example NDNCERT CA";
        caItem.m_probe = "email:name";
        caItem.cert = "Bv0CpwclCANuZG4IA0tFWQgIZm94XuJ/S9AIBHNlbGYICf0AAAFsURyC7hQJGAECGQQANu6AFf0BJjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMFSaakvLIBwsIM78Rzn536WDafHxXri16krBhSQ+Y01mmtXU3ibBPQZnf1wZ116A7tuJnszP1AJsKghnlZUunSt359j8FpRSp8cHXQB8SJI1EzPPHfmaQ7Ao7GHlh4oXmv7OmCcj277GOgQNCse7sNWC775uVcU/5rUa22XWwbt2FV5yeSYeAnaggPxR2aJ/B0Jz+xRhBGJgMtaus87WSkepfSzTqsgjJOAuKHCAwv/2qQuaiEqssliumSs3uWfdNErG//SbyZpi1491oebBJDTvbmkHRANpB4H6IwlUOoe+36DxbsDQ9jEwmq+XLDf2tQuJbOk/R+zfJjlB4yyEEkCAwEAARZFGwEBHBYHFAgDbmRuCANLRVkICGZveF7if0vQ/QD9Jv0A/g8xOTcwMDEwMVQwMDAwMDD9AP8PMjAzOTA3MjhUMDY1NTE4F/0BAKXfQ41lNij4t1gKKzjBlRDyOMFjRWnw9V7iR/grI6L0K4+04hujd/t3viLIke/cYAOqfyib+bfnGaeczlFLuRBZO6YQjdC5FGhkC8IcZoIGAdp2bxYRchpb4R7S7B2oKHnpiCma1LNIqrWkM47LNbUbG/ovgHduwu5GVRv0rb7hwpvFigmWxY9YRJTYUa59N30dmxvSNVZp2CF5QOQyvkI9KuBXUnyhra2ydJSSCQOHchE7hplT90SgC992um1ZYTz18ExVjZJt3BNC6+bAQTCuc0SSO8B91o9zc7BCnPUyZAlVcooSkNBYYJOBLwWIlEH265vD4DXVK7FRpTvuZ9U=";

        if (ndnCertModel.caItems == null) {
            ArrayList<ClientCaItem> caItems = new ArrayList<>();
            caItems.add(caItem);
            ndnCertModel.caItems = caItems;
        }

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        startNetworkThread();
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

//    public void curveTest() {
//        ECParameterSpec ecParameters = ECNamedCurveTable.getParameterSpec("prime256v1");
//        final ECDomainParameters domainParameters = new ECDomainParameters(ecParameters.getCurve(), ecParameters.getG(), ecParameters.getN());
//        final SecureRandom random = new SecureRandom();
//        final ECKeyPairGenerator gen = new ECKeyPairGenerator();
//        gen.init(new ECKeyGenerationParameters(domainParameters, random));
//        final AsymmetricCipherKeyPair senderPair = gen.generateKeyPair();
//        final AsymmetricCipherKeyPair receiverPair = gen.generateKeyPair();
//        final ECDHBasicAgreement senderAgreement = new ECDHBasicAgreement();
//        senderAgreement.init(senderPair.getPrivate());
//        final BigInteger senderResult = senderAgreement.calculateAgreement(
//                receiverPair.getPublic());
//        ECPublicKeyParameters p =;
//        ECDomainParameters
//        AsymmetricCipherKeyPair a = receiverPair.getPublic();
//        final ECDHBasicAgreement receiverAgreement = new ECDHBasicAgreement();
//        receiverAgreement.init(receiverPair.getPrivate());
//        final BigInteger receiverResult = receiverAgreement.calculateAgreement(
//                senderPair.getPublic());
//        Timber.i("Results : " + senderResult);
//        Timber.i("Results : " + receiverResult);
//        Timber.i("Results : " + senderResult.equals(receiverResult));
//    }

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

    private final Thread networkThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    face.processEvents();
                    sleep(1000);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    });

    private void startNetworkThread() {
        if (!networkThread.isAlive()) {
            networkThread.start();
        }
    }

    public void selectImage(View view) {
        viewPager.setCurrentItem(5);
        tab5.setIcon(R.drawable.icon_filled);
    }

    public void imageViewClick(View view) {
        CustomImageViewer v = (CustomImageViewer) view;
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
        Intent i = new Intent(GenerateNDNToken.this, MainActivity.class);
        startActivity(i);
    }

    public void tab1Click(final View view) {
        if (ndnCertModel.selectedCaItem.getValue() == null) {
            Toast.makeText(this, "Please select a CA name", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (!ndnCertModel.selectedCaItem.getValue().m_probe.equals("")) {
                List<String> items = Arrays.asList(ndnCertModel.selectedCaItem.getValue().m_probe.split(":"));
                if (items.size() == 0) {
                    viewPager.setCurrentItem(2);
                    ndnCertModel.nameEditable = true;
                } else {
                    ndnCertModel.nameEditable = false;
                    ndnCertModel.probes.postValue(items);
                    viewPager.setCurrentItem(1);
                }
                tab1.setIcon(R.drawable.icon_filled);
            }
        }
    }

    public void submitProbes() {
        final JSONObject s = ndnCertModel.probesInfo;

        if (!(s == null) && !(s.length() == 0)) {
            Thread networkThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Interest interest = client.generateProbeInterest(ndnCertModel.selectedCaItem.getValue(), s);
                    try {
                        face.expressInterest(interest, probeCb, onTimeout, onNack);
                    } catch (IOException e) {
                        Timber.e("error");
                        e.printStackTrace();
                    }
                }
            });
            networkThread.start();

        } else if (ndnCertModel.probes.getValue().size() != 0) {
            Toast.makeText(GenerateNDNToken.this, "Please fill the values", Toast.LENGTH_SHORT).show();
        } else {
            tab2Click(null);
        }
    }

    public void declineClick(View view) {
        Intent i = new Intent(GenerateNDNToken.this, MainActivity.class);
        startActivity(i);
    }

    public void tab2Click(View view) {
        final Runnable changePage = new Runnable() {
            public void run() {
                viewPager.setCurrentItem(2);
                tab2.setIcon(R.drawable.icon_filled);
            }
        };
        runOnUiThread(changePage);
    }

    public void tab0Click(View view) {
        viewPager.setCurrentItem(0);
    }

    public void sendNewInterest(View view) {
        EditText name = findViewById(R.id.name);
        EditText validity_text = findViewById(R.id.validity_period);

        String validity = validity_text.getText().toString();
        if (validity.equals("")) {
            Toast.makeText(this, "Please enter a validity period", Toast.LENGTH_SHORT).show();
            return;
        }
        final int hours = Integer.parseInt(validity);

        if (hours <= 0) {
            Toast.makeText(this, "Please enter a validity period", Toast.LENGTH_SHORT).show();
            return;
        } else {
            final String identityNameStr = name.getText().toString();
            Thread networkThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        face.expressInterest(client.generateNewInterest(System.currentTimeMillis(),
                                System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hours),
                                new Name(identityNameStr), probeToken), newCb, onTimeout, onNack);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Timber.e("error");
                    }
                }
            });
            networkThread.start();
        }

    }

    public void tab3Click(View view) {
        final Runnable changePage = new Runnable() {
            public void run() {
                viewPager.setCurrentItem(3);
                tab3.setIcon(R.drawable.icon_filled);
            }
        };
        runOnUiThread(changePage);

    }

    public void sendChallengeInterest(View view) {
        final RadioGroup challengeGroup = findViewById(R.id.challengeGroup);
        int checkedRadioButtonId = challengeGroup.getCheckedRadioButtonId();
        if (checkedRadioButtonId == -1) {
            Toast.makeText(this, "PLease select a challenge", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton radioBtn = findViewById(checkedRadioButtonId);
        String choice = radioBtn.getText().toString();

        if (!choice.equals("PIN")) {
            Toast.makeText(this, "Sorry," + choice + " is not supported yet", Toast.LENGTH_SHORT).show();
            return;
        }
        challengeFactory = new ChallengeFactory();
        challenge = challengeFactory.createChallengeModule(choice);
        if (challenge != null) {
            challengeType = choice;
        } else {
            Timber.d("Cannot recognize the specified challenge. Exit");
            return;
        }
        if (client.getApplicationStatus() == STATUS_FAILURE) {
            Timber.d("Failure");
            return;
        }
        try {
            requirement = challenge.getRequirementForChallenge(client.getApplicationStatus(),
                    client.getChallengeStatus());
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.e("error");
        }

        if (requirement.length() > 0) {
            ndnCertModel.requirement.postValue(requirement);
            tab4Click();
            return;
        } else {
            final Thread networkThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        face.expressInterest(client.generateChallengeInterest(challenge.genChallengeRequestJson(
                                client.getApplicationStatus(),
                                client.getChallengeStatus(),
                                requirement)), challengeCb, onTimeout, onNack);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Timber.e("error");
                    }
                }
            });
            networkThread.start();
        }
    }

    public void backImage(View view) {
        returnClick(view);
    }

    private void tab4Click() {
        final Runnable changePage = new Runnable() {
            public void run() {
                viewPager.setCurrentItem(4);
                tab4.setIcon(R.drawable.icon_filled);
            }
        };
        runOnUiThread(changePage);
    }

    public void submitChallenge() {
        if (ndnCertModel.inputRequirement.length() == ndnCertModel.requirement.getValue().length()) {
            final Thread networkThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        face.expressInterest(client.generateChallengeInterest(challenge.genChallengeRequestJson(
                                client.getApplicationStatus(),
                                client.getChallengeStatus(),
                                ndnCertModel.inputRequirement)), challengeCb, onTimeout, onNack);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Timber.e("error");
                    }
                }
            });
            networkThread.start();
        } else {
            Toast.makeText(this, "Please enter all challenges", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void tab5Click() {
        final Runnable changePage = new Runnable() {
            public void run() {
                viewPager.setCurrentItem(5);
                tab5.setIcon(R.drawable.icon_filled);
            }
        };
        runOnUiThread(changePage);
    }


    public void submitImage(View view) {
        final Thread networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    face.expressInterest(client.generateDownloadInterest(), downloadCb, onTimeout, onNack);
                } catch (IOException e) {
                    e.printStackTrace();
                    Timber.e("error");
                }
            }
        });
        networkThread.start();
    }

    private void tab6Click() {
        Timber.d("Cert downloaded");
        final Runnable changePage = new Runnable() {
            public void run() {
                Toast.makeText(GenerateNDNToken.this, "Certificate downloaded and stored", Toast.LENGTH_SHORT).show();
                viewPager.setCurrentItem(6);
                tab6.setIcon(R.drawable.icon_filled);
            }
        };
        runOnUiThread(changePage);

    }

    final OnData probeInfoCb = new OnData() {
        @Override
        public void onData(Interest interest, Data data) {
            Timber.i("Got probeInfoCb data");
            JSONObject contentJson = null;
            try {
                contentJson = client.getJsonFromData(data);
            } catch (JSONException e) {
                e.printStackTrace();
                Timber.e("error");
            }

            ClientCaItem caItem = null;
            try {
                caItem = client.extractCaItem(contentJson);
            } catch (Exception e) {
                e.printStackTrace();
                Timber.e("error");
            }

            if (caItem != null) {
                ndnCertModel.caItems.add(caItem);
                ndnCertModel.selectedCaItem.postValue(caItem);
            }

//            std::cerr << "Will install new trust anchor, please double check the identity info: \n"
//                    << "This trust anchor packet is signed by " << reply.getSignature().getKeyLocator() << std::endl
//                    << "The signing certificate is " << caItem.m_anchor << std::endl;
//            std::cerr << "Do you trust the information? Type in YES or NO" << std::endl;

//            String answer = "YES";
//            getline(std::cin, answer);
//            std::transform(answer.begin(), answer.end(), answer.begin(), ::toupper);
//            if (answer.equals("YES")) {
            try {
                client.onProbeInfoResponse(data);
            } catch (JSONException e) {
                e.printStackTrace();
                Timber.e("error");
            }
//                std::cerr << "You answered YES: new CA installed" << std::endl;
//                startApplication();
//            } else {
//                std::cerr << "New CA not installed" << std::endl;
//                return;
//            }
        }
    };

    Data probeToken = null;
    final OnData probeCb = new OnData() {
        @Override
        public void onData(Interest interest, Data data) {
            Timber.i("Got probeCb data");
            tab2Click(null);
            client.onProbeResponse(data);
            probeToken = data;
        }
    };

    final OnData newCb = new OnData() {
        @Override
        public void onData(Interest interest, Data data) {
            Timber.i("Got newCb data");
            List<String> challengeList = null;
            try {
                challengeList = client.onNewResponse(data);
            } catch (JSONException e) {
                e.printStackTrace();
                Timber.e("error");
            }
            ndnCertModel.challenges.postValue(challengeList);
            tab3Click(null);
        }
    };

    Challenge challenge;
    final OnData challengeCb = new OnData() {
        @Override
        public void onData(Interest interest, Data data) {
            Timber.i("Got data");
            try {
                client.onChallengeResponse(data);
            } catch (JSONException e) {
                e.printStackTrace();
                Timber.e("error");
            }
            if (client.getApplicationStatus() == STATUS_SUCCESS) {
                Timber.i("DONE! Certificate has already been issued \n");
                tab5Click();
                return;
            }

            challenge = challengeFactory.createChallengeModule(challengeType);
            JSONObject requirement = null;
            Timber.d("Cert downloaded");
            final Runnable toast = new Runnable() {
                public void run() {
                    Toast.makeText(GenerateNDNToken.this, client.getChallengeStatus(), Toast.LENGTH_SHORT).show();
                }
            };
            runOnUiThread(toast);
            try {
                requirement = challenge.getRequirementForChallenge(client.getApplicationStatus(), client.getChallengeStatus());
            } catch (JSONException e) {
                e.printStackTrace();
                Timber.e("error");
            }

            if (client.getApplicationStatus() == STATUS_FAILURE) {
                Timber.d("Failure");
                return;
            }
            if (requirement.length() > 0) {
                ndnCertModel.requirement.postValue(requirement);
                tab4Click();
                return;
            }
            try {
                face.expressInterest(client.generateChallengeInterest(challenge.genChallengeRequestJson(
                        client.getApplicationStatus(),
                        client.getChallengeStatus(),
                        requirement)), challengeCb, onTimeout, onNack);
            } catch (Exception e) {
                e.printStackTrace();
                Timber.e("error");
            }
        }
    };

    final OnData downloadCb = new OnData() {
        @Override
        public void onData(Interest interest, Data data) {
            CertificateV2 cert = client.onDownloadResponse(data);
            if (cert != null) {
                ndnCertModel.cert.postValue(cert);
                Timber.d(" DONE! Certificate has already been installed to local keychain");
                insert(cert);
                tab6Click();
            } else {
                Toast.makeText(GenerateNDNToken.this, "Error occurred while fetching Certificate", Toast.LENGTH_SHORT).show();
            }
        }
    };

    final OnTimeout onTimeout = new OnTimeout() {
        @Override
        public void onTimeout(Interest interest) {
            Timber.e("Timeout");
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(GenerateNDNToken.this, "Timeout occurred", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    final OnNetworkNack onNack = new OnNetworkNack() {
        @Override
        public void onNetworkNack(Interest interest, NetworkNack networkNack) {
            Timber.e("Got NACK");
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(GenerateNDNToken.this, "NACK occurred", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private void insert(CertificateV2 cert) {
        Timber.d("inserting into DB");
        DataBaseHelper dbHelper = new DataBaseHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        byte[] arr = new byte[cert.wireEncode().buf().remaining()];
        cert.wireEncode().buf().get(arr);
        String certString = Base64.encodeToString(arr, 0);
        // Insert the new row, returning the primary key value of the new row
        ContentValues values = new ContentValues();
        values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_IDENTITY, cert.getIdentity().toString());
        values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_APPROVED, true);
        values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_CERTIFICATE, certString);
        values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_CAPTION, caption);
        values.put(DataBaseSchema.IdentityEntry.COLUMN_NAME_PICTURE, picture);

        long rowInserted = db.insert(
                DataBaseSchema.IdentityEntry.TABLE_NAME,
                null,
                values);

        Timber.d("rowInserted? " + (rowInserted != -1));
    }

    public void search_name(View view) {
        AutoCompleteTextView acTextView = findViewById(R.id.ca_name);
        final String input = acTextView.getText().toString();
        if (!input.startsWith("/")) {
            Toast.makeText(this, "Please input valid Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!acTextView.getText().toString().equals("")) {
            for (ClientCaItem cl : ndnCertModel.caItems) {
                if (cl.m_caName.toString().equals(input)) {
                    ndnCertModel.selectedCaItem.postValue(cl);
                    return;
                }
            }
            ndnCertModel.selectedCaItem.postValue(null);
            Thread networkThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        face.expressInterest(client.generateProbeInfoInterest(new Name(input)),
                                probeInfoCb, onTimeout, onNack);
                    } catch (Exception e) {
                        Timber.e("error");
                        e.printStackTrace();
                    }
                }
            });
            networkThread.start();
        } else {
            Toast.makeText(this, "Please enter a Name", Toast.LENGTH_SHORT).show();
            ndnCertModel.selectedCaItem.postValue(null);
        }
    }

}
