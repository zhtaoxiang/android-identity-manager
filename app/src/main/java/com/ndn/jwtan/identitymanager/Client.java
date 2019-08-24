package com.ndn.jwtan.identitymanager;

import android.util.Base64;

import net.named_data.jndn.ContentType;
import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;
import net.named_data.jndn.MetaInfo;
import net.named_data.jndn.Name;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.SigningInfo;
import net.named_data.jndn.security.ValidityPeriod;
import net.named_data.jndn.security.VerificationHelpers;
import net.named_data.jndn.security.pib.Pib;
import net.named_data.jndn.security.pib.PibIdentity;
import net.named_data.jndn.security.pib.PibImpl;
import net.named_data.jndn.security.pib.PibKey;
import net.named_data.jndn.security.tpm.Tpm;
import net.named_data.jndn.security.tpm.TpmBackEnd;
import net.named_data.jndn.security.v2.CertificateV2;
import net.named_data.jndn.util.Blob;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static com.ndn.jwtan.identitymanager.Constants.JSON_CA_CHALLENGES;
import static com.ndn.jwtan.identitymanager.Constants.JSON_CA_CHALLENGE_ID;
import static com.ndn.jwtan.identitymanager.Constants.JSON_CA_ECDH;
import static com.ndn.jwtan.identitymanager.Constants.JSON_CA_EQUEST_ID;
import static com.ndn.jwtan.identitymanager.Constants.JSON_CA_NAME;
import static com.ndn.jwtan.identitymanager.Constants.JSON_CA_SALT;
import static com.ndn.jwtan.identitymanager.Constants.JSON_CA_STATUS;
import static com.ndn.jwtan.identitymanager.Constants.JSON_CHALLENGE_REMAINING_TIME;
import static com.ndn.jwtan.identitymanager.Constants.JSON_CHALLENGE_REMAINING_TRIES;
import static com.ndn.jwtan.identitymanager.Constants.JSON_CHALLENGE_STATUS;
import static com.ndn.jwtan.identitymanager.Constants.JSON_CLIENT_CERT_REQ;
import static com.ndn.jwtan.identitymanager.Constants.JSON_CLIENT_ECDH;
import static com.ndn.jwtan.identitymanager.Constants.JSON_CLIENT_SELECTED_CHALLENGE;
import static com.ndn.jwtan.identitymanager.Constants.STATUS_NOT_STARTED;

public class Client {

    private KeyChain keyChain;
    Name m_identityName;
    ClientCaItem m_ca;
    PibKey m_key;

    String m_requestId = "";
    int m_status = STATUS_NOT_STARTED;

    String m_challengeStatus = "";
    String m_challengeType = "";

    List<String> m_challengeList = new ArrayList<>();
    boolean m_isCertInstalled = false;

    int m_remainingTries = 0;
//    private ECDHState ecdhState;

    Client(KeyChain keyChain) {
        this.keyChain = keyChain;
    }

    void onProbeResponse(Data reply) {
        if (!VerificationHelpers.verifyDataSignature(reply, m_ca.getAnchor())) {
            Timber.d("Cannot verify data signature from " + m_ca.m_caName.toUri());
            return;
        }

        JSONObject contentJson = null;
        try {
            contentJson = getJsonFromData(reply);
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.e("error");
        }

        // read the available name and put it into the state
        String nameUri = null;
        try {
            nameUri = contentJson.getString(JSON_CA_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.e("error");
        }
        if (!nameUri.equals("")) {
            m_identityName = new Name(nameUri);
        } else {
            Timber.d("The JSON_CA_NAME is empty.");
        }

    }

    Interest generateProbeInterest(final ClientCaItem ca, final JSONObject paramJson) {
        Name interestName = new Name(ca.m_caName);
        interestName.append("CA").append("_PROBE");
        Interest interest = new Interest(interestName);
        interest.setMustBeFresh(true);
        interest.setCanBePrefix(false);
        Blob blob = new Blob(paramJson.toString().getBytes());

        interest.setApplicationParameters(blob);

        interest.appendParametersDigestToName();
        // update local state
        m_ca = ca;
        return interest;
    }

    public JSONObject getJsonFromData(Data data) throws JSONException {
        JSONObject json;
        String interestData = new String(data.getContent().getImmutableArray(), 0);
        json = new JSONObject(interestData);
        Timber.d(json.toString());
        return json;
    }

    Interest generateNewInterest(final double notBefore, final double notAfter,
                                 final Name identityName, final Data probeToken) {
        if (!(identityName.toUri().equals("/"))) { // if identityName is not empty, find the corresponding CA
//            boolean findCa = false;
//            for (final auto &caItem:
//            m_config.m_caItems){
//                if (caItem.m_caName.isPrefixOf(identityName)) {
//                    m_ca = caItem;
//                    findCa = true;
//                }
//            }
//            if (!findCa) { // if cannot find, cannot proceed
//                return null;
//            }
            m_identityName = identityName;
        } else { // if identityName is empty, check m_identityName or generate a random name
            if (!m_identityName.toUri().equals("/")) {
                // do nothing
            } else {
                Timber.i("Randomly create a new name because m_identityName is empty and the param is empty.");
                SecureRandom secureRandom = new SecureRandom();
                String id = Long.toString(secureRandom.nextLong());
                m_identityName = m_ca.m_caName;
                m_identityName.append(id);
            }
        }


        // generate a newly key pair or use an existing key
        final Pib pib = keyChain.getPib();
        PibIdentity identity = null;

        try {
            // see if the identity exists; if it doesn't, this will throw an error
            identity = pib.getIdentity(m_identityName);
            m_key = keyChain.createKey(identity);
            keyChain.setDefaultIdentity(identity);
            keyChain.setDefaultKey(identity, m_key);
            keyChain.getPib().setDefaultIdentity_(m_identityName);
        } catch (PibImpl.Error | Pib.Error | TpmBackEnd.Error | Tpm.Error | KeyChain.Error ex) {
            try {
                identity = keyChain.createIdentityV2(m_identityName);
                m_key = identity.getDefaultKey();
                keyChain.setDefaultIdentity(identity);
                keyChain.setDefaultKey(identity, m_key);
                keyChain.getPib().setDefaultIdentity_(m_identityName);
            } catch (PibImpl.Error | Pib.Error | TpmBackEnd.Error | Tpm.Error | KeyChain.Error er) {
                er.printStackTrace();
            }
        }

        // generate certificate request
        Data data = new Data(new Name(m_key.getName()).append("cert-request").appendVersion(System.currentTimeMillis()));
        data.setContent(m_key.getPublicKey());
        MetaInfo metaInfo = new MetaInfo();
        metaInfo.setType(ContentType.KEY);
        metaInfo.setFreshnessPeriod(TimeUnit.HOURS.toMillis(24));
        data.setMetaInfo(metaInfo);
        CertificateV2 certRequest = null;
        try {
            certRequest = new CertificateV2(data);
        } catch (CertificateV2.Error error) {
            Timber.e("error");
            error.printStackTrace();
        }
        SigningInfo signingInfo = new SigningInfo(SigningInfo.SignerType.KEY, m_key.getName());
        signingInfo.setValidityPeriod(new ValidityPeriod(notBefore, notAfter));
        try {
            keyChain.sign(certRequest, signingInfo);
        } catch (Exception er) {
            er.printStackTrace();
            Timber.e("error");
        }

        // generate Interest packet
        Name interestName = new Name(m_ca.m_caName);
        interestName.append("CA").append("_NEW");
        Interest interest = new Interest(interestName);
        interest.setMustBeFresh(true);
        interest.setCanBePrefix(false);
        try {
//            ecdhState = new ECDHState();

//            byte[] comp = ecdhState.loadPublicECKeyByte();
//            Timber.i(x + " : " + y);
//            String pubKey = Base64.encodeToString(ecdhState.publicKey.getQ().getEncoded(), 0);
//            Timber.i(pubKey);

            interest.setApplicationParameters(paramFromJson(genNewRequestJson("AjZd215DZTpJ25ZI9UTx/FljN2dKTcKMlZqALc9mCxu7", certRequest, probeToken)));
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e("error");
        }

        // sign the Interest packet
        try {
            keyChain.sign(interest, new SigningInfo(SigningInfo.SignerType.KEY, m_key.getName()));
        } catch (Exception er) {
            er.printStackTrace();
            Timber.e("error");
        }
        return interest;
    }


    private final JSONObject genNewRequestJson(final String ecdhPub, final CertificateV2 certRequest,
                                               final Data probeToken) throws JSONException {
        JSONObject root = new JSONObject();
        byte[] arr = new byte[certRequest.wireEncode().buf().remaining()];
        certRequest.wireEncode().buf().get(arr);
        String certString = Base64.encodeToString(arr, 0);

        root.put(JSON_CLIENT_ECDH, ecdhPub);
        root.put(JSON_CLIENT_CERT_REQ, certString);
        if (probeToken != null) {
            // clear the Stringstream
            // transform the probe data into a base64 String
            arr = new byte[probeToken.wireEncode().buf().remaining()];
            probeToken.wireEncode().buf().get(arr);
            String token = Base64.encodeToString(arr, 0);
            root.put("probe-token", token);
        }
        return root;
    }

    List<String>
    onNewResponse(final Data reply) throws JSONException {
        if (!VerificationHelpers.verifyDataSignature(reply, m_ca.getAnchor())) {
            Timber.d("Cannot verify data signature from " + m_ca.m_caName.toUri());
            return new ArrayList<>();
        }

        JSONObject contentJson = null;
        try {
            contentJson = getJsonFromData(reply);
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.e("error");
        }

        // ECDH
        final String peerKeyBase64Str = contentJson.getString(JSON_CA_ECDH);
        final long saltStr = contentJson.getLong(JSON_CA_SALT);
//        long saltInt = std::stoull (saltStr);
//        int salt[ sizeof(saltInt)];
//        std::memcpy (salt, &saltInt, sizeof(saltInt));
//        byte[] result = Base64.decode(peerKeyBase64Str, 0);

//        ecdhState.deriveSecret(result);
//        m_ecdh.deriveSecret(peerKeyBase64Str);

//        HKDF
//        hkdf(m_ecdh.context.sharedSecret, m_ecdh.context.sharedSecretLen, salt, sizeof(saltInt), m_aesKey, 32);

        // update state
        m_status = contentJson.getInt(JSON_CA_STATUS);
        m_requestId = contentJson.getString(JSON_CA_EQUEST_ID);

        JSONArray challengesJson = contentJson.getJSONArray(JSON_CA_CHALLENGES);
        m_challengeList.clear();

        for (int i = 0; i < challengesJson.length(); i++) {
            JSONObject obj = challengesJson.getJSONObject(i);
            m_challengeList.add(obj.getString(JSON_CA_CHALLENGE_ID));
        }
        return m_challengeList;
    }

    Interest generateChallengeInterest(final JSONObject paramJson) throws JSONException {
        m_challengeType = paramJson.getString(JSON_CLIENT_SELECTED_CHALLENGE);

        Name interestName = new Name(m_ca.m_caName);
        interestName.append("CA").append("_CHALLENGE").append(m_requestId);
        Interest interest = new Interest(interestName);
        interest.setMustBeFresh(true);
        interest.setCanBePrefix(false);

        // encrypt the Interest parameters
//        String payload = paramJson.toString();

//        Blob paramBlock = genEncBlock(tlv::ApplicationParameters, m_ecdh.context.sharedSecret, m_ecdh.context.sharedSecretLen,
//                ( final long*)payload.c_str(), payload.size());
        interest.setApplicationParameters(paramFromJson(paramJson));

        SigningInfo signingInfo = new SigningInfo(SigningInfo.SignerType.KEY, m_key.getName());
        try {
            keyChain.sign(interest, signingInfo);
        } catch (Exception er) {
            er.printStackTrace();
            Timber.e("error");
        }
        return interest;
    }

    private Blob paramFromJson(final JSONObject json) {
        return new Blob(json.toString().getBytes());
    }

    String
    getChallengeStatus() {
        return m_challengeStatus;
    }


    public int getApplicationStatus() {
        return m_status;
    }

    public void onChallengeResponse(Data data) throws JSONException {
        if (!VerificationHelpers.verifyDataSignature(data, m_ca.getAnchor())) {
            Timber.d("Cannot verify data signature from " + m_ca.m_caName.toUri());
            return;
        }
//        byte[] result = parseEncBlock(m_ecdh.context.sharedSecret, m_ecdh.context.sharedSecretLen, reply.getContent());

        String interestData = new String(data.getContent().getImmutableArray(), 0);
        JSONObject contentJson = new JSONObject(interestData);
//        String payload = new String(result, 0);
//        JSONObject contentJson = new JSONObject(payload);
//
        // update state
        m_status = contentJson.getInt(JSON_CA_STATUS);
        m_challengeStatus = contentJson.getString(JSON_CHALLENGE_STATUS);
        m_remainingTries = contentJson.getInt(JSON_CHALLENGE_REMAINING_TRIES);
        int m_freshBefore = (int) (System.currentTimeMillis() / 1000) + contentJson.getInt(JSON_CHALLENGE_REMAINING_TIME);
    }

    public Interest generateDownloadInterest() {
        Name interestName = new Name(m_ca.m_caName);
        interestName.append("CA").append("_DOWNLOAD").append(m_requestId);
        Interest interest = new Interest(interestName);
        interest.setMustBeFresh(true);
        interest.setCanBePrefix(false);
        return interest;
    }

    public Interest generateProbeInfoInterest(Name name) {
        Name interestName = new Name(name);
        if (!interestName.getPrefix(-1).toString().equals("CA"))
            interestName.append("CA");
        interestName.append("_PROBE").append("INFO");
        Interest interest = new Interest(interestName);
        interest.setMustBeFresh(true);
        interest.setCanBePrefix(false);
        return interest;
    }

    public void onProbeInfoResponse(Data data) throws JSONException {
        JSONObject contentJson = getJsonFromData(data);
        ClientCaItem caItem = null;
        try {
            caItem = extractCaItem(contentJson);
        } catch (EncodingException e) {
            e.printStackTrace();
            Timber.e("error");
        }

//        // update the local config
//        boolean findItem = false;
//        for (ClientCaItem item : m_config.m_caItems) {
//            if (item.m_caName == caItem.m_caName) {
//                findItem = true;
//                item = caItem;
//            }
//        }
//        if (!findItem) {
//            m_config.m_caItems.push_back(caItem);
//        }

        // verify the probe Data's sig
        if (!VerificationHelpers.verifyDataSignature(data, caItem.getAnchor())) {
            Timber.d("Cannot verify data signature from " + m_ca.m_caName.toUri());
            return;
        }
    }

    ClientCaItem extractCaItem(final JSONObject configSection) throws JSONException, EncodingException {
        ClientCaItem item = new ClientCaItem();
        item.m_caName = new Name(configSection.getString("ca-prefix"));
        item.m_caInfo = configSection.getString("ca-info");
        item.m_probe = configSection.getString("probe");
        String ss = configSection.getString("certificate");
        item.m_anchor = new CertificateV2();
        item.m_anchor.wireDecode(ByteBuffer.wrap(Base64.decode(ss, 0)));

        return item;
    }

    CertificateV2 onDownloadResponse(Data data) {
        try {
            CertificateV2 cert = new CertificateV2();
            cert.wireDecode(ByteBuffer.wrap(data.getContent().getImmutableArray()));

            keyChain.addCertificate(m_key, cert);
            Timber.d("Got DOWNLOAD response and installed the cert " + cert.getName());
            m_isCertInstalled = true;
            return cert;
        } catch (Exception e) {
            e.printStackTrace();
            Timber.d("Cannot add replied certificate into the keychain ");
            return null;
        }
    }
}
