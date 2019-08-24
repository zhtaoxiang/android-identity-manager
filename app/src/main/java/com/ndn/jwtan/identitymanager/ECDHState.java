//package com.ndn.jwtan.identitymanager;
//
//import android.util.Base64;
//
//import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
//import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
//import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
//import org.bouncycastle.crypto.params.ECDomainParameters;
//import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
//import org.bouncycastle.crypto.params.ECPublicKeyParameters;
//import org.bouncycastle.jce.ECNamedCurveTable;
//import org.bouncycastle.jce.spec.ECParameterSpec;
//import org.bouncycastle.math.ec.ECPoint;
//
//import java.io.IOException;
//import java.math.BigInteger;
//import java.security.Key;
//import java.security.KeyStore;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.PrivateKey;
//import java.security.SecureRandom;
//import java.security.cert.Certificate;
//import java.security.cert.CertificateException;
//import java.security.interfaces.ECPublicKey;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Enumeration;
//
//import timber.log.Timber;
//
//public class ECDHState {
//
//    final String PRIVATE_KEY = "privateKey";
//    final String PUBLIC_KEY = "publicKey";
//    final String SECRET_KEY = "secretKey";
//    final String KEY_STORE = "key_store";
//    byte[] secretKey;
//    //    private PublicKey publicKey;
//    private PrivateKey privateKey;
//    private BigInteger xp;
//    private BigInteger yp;
//    ECParameterSpec ecParameters = ECNamedCurveTable.getParameterSpec("prime256v1");
//    final ECDomainParameters domainParameters = new ECDomainParameters(ecParameters.getCurve(), ecParameters.getG(), ecParameters.getN());
//    AsymmetricCipherKeyPair senderPair;
//    ECPublicKeyParameters publicKey;
//
//    public ECDHState() {
//        try {
//            generateNewKeyPair();
//        } catch (Exception e) {
//            e.printStackTrace();
//            Timber.i("error");
//        }
//    }
//
//    public PrivateKey loadPrivateKey() throws Exception {
//        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
//        keyStore.load(null);
//        PrivateKey key = (PrivateKey) keyStore.getKey(KEY_STORE, null);
//        return key;
//    }
//
//    public Key loadPublicKey() throws Exception {
//        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
//        keyStore.load(null);
//        // Get certificate of public key
//        Certificate cert = keyStore.getCertificate(KEY_STORE);
//        Key publicKey = cert.getPublicKey();
//        // Return a key pair
//        return publicKey;
//    }
//
//    public ECPublicKey loadPublicECKey() throws Exception {
//        return (ECPublicKey) this.loadPublicKey();
//    }
//
//    public byte[] loadPublicECKeyByte() throws Exception {
////        return compressedToUncompressed(this.loadPublicECKey());
//        return compressedToUncompressed1(xp, yp);
//    }
//
//    public void generateNewKeyPair() throws Exception {
////        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
////        keyStore.load(null);
////
////        if (!keyStore.containsAlias(KEY_STORE)) {
////            Timber.i("generating");
////            // use the Android keystore
////            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
////            keyGen.initialize(
////                    new KeyGenParameterSpec.Builder(
////                            KEY_STORE,
////                            KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY | KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
////                            .setAlgorithmParameterSpec(new ECGenParameterSpec("prime256v1"))
////                            .setDigests(KeyProperties.DIGEST_SHA256)
//////                            .setRandomizedEncryptionRequired(true)
////                            .build());
////            // generates the keypair
////            KeyPair keyPair = keyGen.generateKeyPair();
////
////            Timber.i("" + keyPair);
////            Timber.i("" + keyPair.getPrivate());
////            Timber.i("" + keyPair.getPublic());
////
////        } else {
////            Timber.i("contains");
////        }
////        Timber.i("" + keyStore.containsAlias(KEY_STORE));
////        Timber.i("" + keyStore.getKey(KEY_STORE, null));
////        Timber.i("" + loadPrivateKey());
////        Timber.i("" + loadPublicKey());
//
//
//        final SecureRandom random = new SecureRandom();
//        final ECKeyPairGenerator gen = new ECKeyPairGenerator();
//        gen.init(new ECKeyGenerationParameters(domainParameters, random));
//        senderPair = gen.generateKeyPair();
//        publicKey = ((ECPublicKeyParameters) gen.generateKeyPair().getPublic());
//        xp = publicKey.getQ().getAffineXCoord().toBigInteger();
//        yp = publicKey.getQ().getAffineYCoord().toBigInteger();
////        SubjectPublicKeyInfoFactory pubKeyInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(senderPair.getPublic());
//        Timber.i("X: " + xp);
//        Timber.i("Y: " + yp);
//        Timber.i("Y: " + publicKey.getQ().getEncoded().length);
//    }
//
//    public ArrayList<String> allKeys() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
//        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
//        ks.load(null);
//        Enumeration<String> aliases = ks.aliases();
//        ArrayList<String> keyAliases = new ArrayList<>();
//        while (aliases.hasMoreElements())
//            keyAliases.add(aliases.nextElement());
//
//        return keyAliases;
//    }
//
//    byte[] compressedToUncompressed(ECPublicKey publicKey) {
//        BigInteger yi = publicKey.getW().getAffineY();
//        BigInteger xi = publicKey.getW().getAffineX();
//
//        byte[] x = xi.toByteArray();
//        byte[] y = yi.toByteArray();
//
//        if (x.length != 32 || y.length != 32) {
//            Timber.i("error");
//            return null;
//        }
//        // concat 0x04, x, and y, make sure x and y has 32-bytes:
//        byte[] toRet = new byte[65];
//        toRet[0] = (byte) 0x04;
//        System.arraycopy(x, 0, toRet, 1, 32);
//        System.arraycopy(y, 0, toRet, 33, 32);
//        return toRet;
//    }
//
//    byte[] compressedToUncompressed1(BigInteger xi, BigInteger yi) {
//
//        byte[] x = xi.toByteArray();
//        byte[] y = yi.toByteArray();
//
//        if (x.length != 32 || y.length != 32) {
//            Timber.i("error : " + x.length + " : " + y.length);
//            return null;
//        }
//        // concat 0x04, x, and y, make sure x and y has 32-bytes:
//        byte[] toRet = new byte[65];
//        toRet[0] = (byte) 0x04;
//        System.arraycopy(x, 0, toRet, 1, 32);
//        System.arraycopy(y, 0, toRet, 33, 32);
//        return toRet;
//    }
//
//    public byte[] deriveSecret(final byte[] peerkey) {
//        Timber.i(peerkey.length + "");
//        Timber.i(peerkey[0] + "");
//
//        ECParameterSpec ecParameters = ECNamedCurveTable.getParameterSpec("prime256v1");
//        final ECDomainParameters domainParameters = new ECDomainParameters(ecParameters.getCurve(), ecParameters.getG(), ecParameters.getN());
//        ECPublicKeyParameters otherKey = new ECPublicKeyParameters(getECPoint(peerkey), domainParameters);
////        gen.generateKeyPair().getPublic());
//
//        final ECDHBasicAgreement senderAgreement = new ECDHBasicAgreement();
//        senderAgreement.init(senderPair.getPrivate());
//        final BigInteger senderResult = senderAgreement.calculateAgreement(otherKey);
//        Timber.i(senderResult.toString());
//        Timber.i(Arrays.toString(senderResult.toByteArray()));
//        String token = Base64.encodeToString(senderResult.toByteArray(), 0);
//        Timber.i(token + "");
//        return null;
////        }
//    }
//
//    private ECPoint getECPoint(byte[] encoded) {
//        try {
//            ECParameterSpec ecParameters = ECNamedCurveTable.getParameterSpec("prime256v1");
//            Timber.i("here");
//            ECPoint point = ecParameters.getCurve().decodePoint(encoded);
//            Timber.i(point.getRawXCoord().toBigInteger() + "");
//            return point;
//        } catch (Exception e) {
//            e.printStackTrace();
//            Timber.i("error");
//        }
//        return null;
//    }
//
//}