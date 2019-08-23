package com.ndn.jwtan.identitymanager;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static com.ndn.jwtan.identitymanager.Constants.STATUS_FAILURE;
import static com.ndn.jwtan.identitymanager.Constants.STATUS_SUCCESS;
import static java.lang.Thread.sleep;

class ClientCaItem {
    Name m_caName = new Name("/ndn");
    String m_caInfo = "An example NDNCERT CA";
    String m_probe = "email";
    private String cert = "Bv0CpwclCANuZG4IA0tFWQgIZm94XuJ/S9AIBHNlbGYICf0AAAFsURyC7hQJGAECGQQANu6AFf0BJjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMFSaakvLIBwsIM78Rzn536WDafHxXri16krBhSQ+Y01mmtXU3ibBPQZnf1wZ116A7tuJnszP1AJsKghnlZUunSt359j8FpRSp8cHXQB8SJI1EzPPHfmaQ7Ao7GHlh4oXmv7OmCcj277GOgQNCse7sNWC775uVcU/5rUa22XWwbt2FV5yeSYeAnaggPxR2aJ/B0Jz+xRhBGJgMtaus87WSkepfSzTqsgjJOAuKHCAwv/2qQuaiEqssliumSs3uWfdNErG//SbyZpi1491oebBJDTvbmkHRANpB4H6IwlUOoe+36DxbsDQ9jEwmq+XLDf2tQuJbOk/R+zfJjlB4yyEEkCAwEAARZFGwEBHBYHFAgDbmRuCANLRVkICGZveF7if0vQ/QD9Jv0A/g8xOTcwMDEwMVQwMDAwMDD9AP8PMjAzOTA3MjhUMDY1NTE4F/0BAKXfQ41lNij4t1gKKzjBlRDyOMFjRWnw9V7iR/grI6L0K4+04hujd/t3viLIke/cYAOqfyib+bfnGaeczlFLuRBZO6YQjdC5FGhkC8IcZoIGAdp2bxYRchpb4R7S7B2oKHnpiCma1LNIqrWkM47LNbUbG/ovgHduwu5GVRv0rb7hwpvFigmWxY9YRJTYUa59N30dmxvSNVZp2CF5QOQyvkI9KuBXUnyhra2ydJSSCQOHchE7hplT90SgC992um1ZYTz18ExVjZJt3BNC6+bAQTCuc0SSO8B91o9zc7BCnPUyZAlVcooSkNBYYJOBLwWIlEH265vD4DXVK7FRpTvuZ9U=";
    //    byte[] certBytes = Base64.decode(cert, 0);
    CertificateV2 m_anchor;

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

    void setAnchor(byte[] certBytes) {
    }
}

class ServerCaItem {

    Name ca_prefix = new Name("/ndn");
    int issuing_freshness = 720;
    int validity_period = 360;
    String m_caInfo = "An example NDNCERT CA";
    String m_probe = "ritik@random.com";
    HashMap<String, String> supported_challenges = new HashMap<>();

    public ServerCaItem() {
        supported_challenges.put("type", "PIN");
        supported_challenges.put("type", "Email");
    }
}

public class GenerateNDNToken extends AppCompatActivity {

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

    private Client client;
    String challengeType;

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

    public void submitEmail(View view) {
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
        Intent i = new Intent(GenerateNDNToken.this, MainActivity.class);
        startActivity(i);
        return;
    }

    public void tab1Click(View view) {
        viewPager.setCurrentItem(1);
        tab1.setIcon(R.drawable.icon_filled);
    }

    public void declineClick(View view) {
        Intent i = new Intent(GenerateNDNToken.this, MainActivity.class);
        startActivity(i);
    }

    public boolean isValidEmailAddress(String email) {
//        String ePattern = "^[a-zA-Z0-9.!#$% '*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
//        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
//        java.util.regex.Matcher m = p.matcher(email);
//        return m.matches();

        return true;
    }

    public void tab2Click(View view) {
        EditText editText = (EditText) findViewById(R.id.emailText);
        String email = editText.getText().toString();

//        TextView editID = findViewById(R.id.idNameText);
//        String idName = editID.getText().toString();

        if (isValidEmailAddress(email)) {
//            if (idName != "") {
//            this.caption = idName;
            viewPager.setCurrentItem(2);
            tab2.setIcon(R.drawable.icon_filled);
//            } else {
//                String toastString = "Please give an identity name";
//                Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();
//            }
        } else {
            String toastString = "Please put valid email address";
            Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();
        }
    }

    public void tab0Click(View view) {
        viewPager.setCurrentItem(0);
    }

    ////////////////////////////////////////////////////////////
    Face face = new Face();
    String email = "";

    private void sendHttpRequest(final String email) {
        // Instantiate the RequestQueue.
        Timber.i(email);

        if (!email.equals("")) {
            this.email = email;
//            startApplication();
        }

        String rootPath = getApplicationContext().getFilesDir().toString();
        AndroidSqlite3Pib m_pib = null;
        TpmBackEndFile m_tpm;

        try {
            m_pib = new AndroidSqlite3Pib(rootPath, "/pib.db");
        } catch (PibImpl.Error e) {
            Timber.i("error");
            e.printStackTrace();
        }

        m_tpm = new TpmBackEndFile(TpmBackEndFile.getDefaultDirecoryPath(getApplicationContext().getFilesDir()));
        try {
            m_pib.setTpmLocator("tpm-file:" + TpmBackEndFile.getDefaultDirecoryPath(getApplicationContext().getFilesDir()));
        } catch (PibImpl.Error e) {
            Timber.i("error");
            e.printStackTrace();
        }

        KeyChain keyChain = null;
        try {
            keyChain = new KeyChain(m_pib, m_tpm);
        } catch (PibImpl.Error e) {
            e.printStackTrace();
            Timber.i("error");
        }
        client = new Client(keyChain);

        startNetworkThread();
//        try {
//            face.processEvents();
//        } catch (Exception e) {
//            Timber.i("error");
//            e.printStackTrace();
//        }
    }

    void startApplication() {
//        int nStep = 0;
//        auto caList = client.getClientConf().m_caItems;
//        int count = 0;
//        for (auto item : caList) {
//            std::cerr << "***************************************\n"
//                    << "Index: " << count++ << "\n"
//                    << "CA prefix:" << item.m_caName << "\n"
//                    << "Introduction: " << item.m_caInfo << "\n"
//                    << "***************************************\n";
//        }
//        std::vector<ClientCaItem> caVector{std::begin(caList), std::end(caList)};
//        std::cerr << "Step "
//                << nStep++ << ": Please type in the CA INDEX that you want to apply"
//                << " or type in NONE if your expected CA is not in the list\n";

        String caIndexS = "";
        String caIndexSUpper = "";
        ClientCaItem targetCaItem;
//        getline(std::cin, caIndexS);
        caIndexS = "5";
        caIndexSUpper = caIndexS;
        if (caIndexSUpper == "") {
//            std::cerr << "Step " << nStep << ": Please type in the CA Name\n";
            try {
                face.expressInterest(client.generateProbeInfoInterest(new Name(caIndexS)),
                        probeInfoCb, onTimeout, onNack);
            } catch (IOException e) {
                e.printStackTrace();
                Timber.i("error");
            }
        } else {
            int caIndex = Integer.parseInt(caIndexS);
//            targetCaItem = caVector[caIndex];
        }
        targetCaItem = new ClientCaItem();
        if (targetCaItem.m_probe != "") {
//                std::cerr << "Step " << nStep++ << ": Please provide information for name assignment" << std::endl;
//                std::vector<String> probeFields = ClientModule::parseProbeComponents(targetCaItem.m_probe);
//                String redo = "";
//                std::list<String> capturedParams;
//                do {
//                    capturedParams = captureParams(probeFields);
//                    std::cerr << "If everything is right, please type in OK; otherwise, type in REDO" << std::endl;
//                    getline(std::cin, redo);
//                    std::transform(redo.begin(), redo.end(), redo.begin(), ::toupper);
//                } while (redo == "REDO");
//                String probeInfo;
//                for (final  auto  item : capturedParams) {
//                    probeInfo += item;
//                    probeInfo += ":";
//                }
//                String probeInfo = probeInfo.substr(0, probeInfo.size() - 1);
            String probeInfo = "ritikkne@gmail.com";
            Interest interest = client.generateProbeInterest(targetCaItem, probeInfo);
            try {
                face.expressInterest(interest, probeCb, onTimeout, onNack);
            } catch (IOException e) {
                Timber.i("error");
                e.printStackTrace();
            }
        } else {
//            std::cerr << "Step " << nStep++ << ": Please type in the identity name you want to get (with CA prefix)\n";
            String identityNameStr = "";
//            getline(std::cin, identityNameStr);
//            std::cerr << "Step "
//                    << nStep++ << ": Please type in your expected validity period of your certificate."
//                    << "Type in a number in unit of hour."
//                    << " The CA may change the validity period if your expected period is too long.\n";
//            String periodStr;
//            getline(std::cin, periodStr);
            int hours = 5;
            try {
                face.expressInterest(client.generateNewInterest(System.currentTimeMillis(),
                        System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hours),
                        new Name(identityNameStr), null), newCb, onTimeout, onNack);
            } catch (IOException e) {
                e.printStackTrace();
                Timber.i("error");
            }
        }
    }

    private final Thread networkThread = new Thread(new Runnable() {
        @Override
        public void run() {
            startApplication();
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

    ChallengeFactory challengeFactory;

    static List<String> captureParams(final JSONObject requirement) {
        List<String> results = new ArrayList<>();
        for (Iterator<String> it = requirement.keys(); it.hasNext(); ) {
            String a = it.next();
//            std::cerr << item.second.get <  String > ("") << std::endl;
//            std::cerr << "Please provide the argument: " << item.first << " : " << std::endl;
            String tempParam;
//            getline(std::cin, tempParam);
            results.add("1234");
        }
//        std::cerr << "Got it. This is what you've provided:" << std::endl;
//        auto it1 = results.begin();
//        auto it2 = requirement.begin();
//        for (; it1 != results.end() && it2 != requirement.end(); it1++, it2++) {
//            std::cerr << it2 -> first << " : " << * it1 << std::endl;
//        }
        return results;
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
                Timber.i("error");
            }
            try {
                ClientCaItem caItem = client.extractCaItem(contentJson);
            } catch (Exception e) {
                e.printStackTrace();
                Timber.i("error");
            }

//            std::cerr << "Will install new trust anchor, please double check the identity info: \n"
//                    << "This trust anchor packet is signed by " << reply.getSignature().getKeyLocator() << std::endl
//                    << "The signing certificate is " << caItem.m_anchor << std::endl;
//            std::cerr << "Do you trust the information? Type in YES or NO" << std::endl;

            String answer = "YES";
//            getline(std::cin, answer);
//            std::transform(answer.begin(), answer.end(), answer.begin(), ::toupper);
            if (answer.equals("YES")) {
                try {
                    client.onProbeInfoResponse(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Timber.i("error");
                }
//                std::cerr << "You answered YES: new CA installed" << std::endl;
                startApplication();
            } else {
//                std::cerr << "New CA not installed" << std::endl;
                return;
            }
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
                Timber.i("error");
            }
//            std::cerr << "Step " << nStep++ << ": Please type in the challenge ID from the following challenges\n";
            for (String item : challengeList) {
//                std::cerr << "\t" << item << std::endl;
                Timber.i(item);
            }
            String choice = challengeList.get(0);
            Timber.i(choice);

            challengeFactory = new ChallengeFactory();
            Challenge challenge = challengeFactory.createChallengeModule(choice);
            if (challenge != null) {
                challengeType = choice;
            } else {
                Timber.i("Cannot recognize the specified challenge. Exit");
                return;
            }

            JSONObject requirement = null;
            try {
                requirement = challenge.getRequirementForChallenge(client.getApplicationStatus(),
                        client.getChallengeStatus());
            } catch (JSONException e) {
                e.printStackTrace();
                Timber.i("error");
            }
            if (client.getApplicationStatus() == STATUS_FAILURE) {
                Timber.i("Failure");
                return;
            }
            if (requirement.length() > 0) {
//                std::cerr << "Step " << nStep++ << ": Please satisfy following instruction(s)\n";
//                String redo = "";
                List<String> capturedParams;
//                do {
                capturedParams = captureParams(requirement);
//                Timber.i("If everything is right, please type in OK; otherwise, type in REDO");
//                getline(std::cin, redo);
//                std::transform (redo.begin(), redo.end(), redo.begin(), ::toupper);
//                } while (redo == "REDO");
//                auto it1 = capturedParams.begin();
//                auto it2 = requirement.begin();
                Timber.i(capturedParams.toString());
                int i = 0;
                for (Iterator<String> it = requirement.keys(); it.hasNext(); ) {
                    String keyStr = it.next();
                    try {
                        requirement.put(keyStr, capturedParams.get(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Timber.i("error");
                    }
                    i++;
                }
//                for (; it1 != capturedParams.end() && it2 != requirement.end(); it1++, it2++) {
//                    it2 -> second.put("", * it1);
//                }
            }
            try {
                face.expressInterest(client.generateChallengeInterest(challenge.genChallengeRequestJson(
                        client.getApplicationStatus(),
                        client.getChallengeStatus(),
                        requirement))
                        , challengeCb, onTimeout, onNack);
            } catch (Exception e) {
                e.printStackTrace();
                Timber.i("error");
            }
        }
    };

    final OnData probeCb = new OnData() {
        @Override
        public void onData(Interest interest, Data data) {
            Timber.i("Got probeCb data");
            client.onProbeResponse(data);
//            std::cerr << "Step " << nStep++
//                    << ": Please type in your expected validity period of your certificate."
//                    << " Type in a number in unit of hour. The CA may change the validity"
//                    << " period if your expected period is too long." << std::endl;
            String periodStr;
//            getline(std::cin, periodStr);
//            int hours = std::stoi(periodStr);
            int hours = 5;
            try {
                face.expressInterest(client.generateNewInterest(System.currentTimeMillis(),
                        System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hours),
                        new Name(), data), newCb, onTimeout, onNack);
            } catch (IOException e) {
                e.printStackTrace();
                Timber.i("error");
            }
        }
    };

    final OnData challengeCb = new OnData() {
        @Override
        public void onData(Interest interest, Data data) {
            try {
                client.onChallengeResponse(data);
            } catch (JSONException e) {
                e.printStackTrace();
                Timber.i("error");
            }
            if (client.getApplicationStatus() == STATUS_SUCCESS) {
                Timber.i("DONE! Certificate has already been issued \n");
                try {
                    face.expressInterest(client.generateDownloadInterest(), downloadCb, onTimeout, onNack);
                } catch (IOException e) {
                    e.printStackTrace();
                    Timber.i("error");
                }
                return;
            }

            Challenge challenge = challengeFactory.createChallengeModule(challengeType);
            JSONObject requirement = null;
            try {
                requirement = challenge.getRequirementForChallenge(client.getApplicationStatus(), client.getChallengeStatus());
            } catch (JSONException e) {
                e.printStackTrace();
                Timber.i("error");
            }
            Timber.i(client.getApplicationStatus() + "");
            if (client.getApplicationStatus() == STATUS_FAILURE) {
                Timber.i("Failure");
                return;
            }
            if (requirement.length() > 0) {
//                std::cerr << "Step " << nStep++ << ": Please satisfy following instruction(s)\n";
//                String redo = "";
                List<String> capturedParams;
//                do {
                capturedParams = captureParams(requirement);
                Timber.i(capturedParams.toString());
//                    std::cerr << "If everything is right, please type in OK; otherwise, type in REDO" << std::endl;
//                    getline(std::cin, redo);
//                    std::transform(redo.begin(), redo.end(), redo.begin(), ::toupper);
//                } while (redo == "REDO");
//                auto it1 = capturedParams.begin();
//                auto it2 = requirement.begin();
                int i = 0;
                for (Iterator<String> it = requirement.keys(); it.hasNext(); ) {
                    String keyStr = it.next();
                    try {
                        requirement.put(keyStr, capturedParams.get(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Timber.i("error");
                    }
                    i++;
                }
            }

//                for (; it1 != capturedParams.end() && it2 != requirement.end(); it1++, it2++) {
//                    it2->second.put("", *it1);
//                }
//            }

            try {
                face.expressInterest(client.generateChallengeInterest(challenge.genChallengeRequestJson(
                        client.getApplicationStatus(),
                        client.getChallengeStatus(),
                        requirement)), challengeCb, onTimeout, onNack);
            } catch (Exception e) {
                e.printStackTrace();
                Timber.i("error");
            }

        }
    };

    final OnData downloadCb = new OnData() {
        @Override
        public void onData(Interest interest, Data data) {
            CertificateV2 cert = client.onDownloadResponse(data);
            if (cert != null) {
                Timber.i(" DONE! Certificate has already been installed to local keychain\\n!!");
                insert(cert);
            }
        }
    };

    final OnTimeout onTimeout = new OnTimeout() {
        @Override
        public void onTimeout(Interest interest) {
            Timber.i("Timeout");
        }
    };

    final OnNetworkNack onNack = new OnNetworkNack() {
        @Override
        public void onNetworkNack(Interest interest, NetworkNack networkNack) {
            Timber.d("Got Nack");
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

        Timber.i("rowInserted? " + (rowInserted !=-1));
    }
}
