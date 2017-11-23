package it.uniparthenope.fairwind.services.logger.filepacker.security;

import java.security.*;
import java.util.Base64;

/**
 * Created by marioruggieri on 17/10/2017.
 */
public class RSAKeysGenerator {

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public RSAKeysGenerator(String type, int size) throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(type);
        kpg.initialize(size);
        KeyPair kp = kpg.generateKeyPair();
        publicKey = kp.getPublic();
        privateKey = kp.getPrivate();

        //storeKey(prvk,"/Users/mario/IdeaProjects/SignalKLogger/keys/private_key.txt");
        //storeKey(pubk,"/Users/mario/IdeaProjects/SignalKLogger/keys/public_key.txt");
    }

    public String getPrivateKey() {
        return keyToString(privateKey);
    }

    public String getPublicKey() {
        return keyToString(publicKey);
    }

    private String keyToString(Key key){
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /*
    private static void storeKey(Key key, String path) throws IOException {
        String keystring = keyToString(key);
        PrintWriter pw = new PrintWriter(new File(path));
        pw.write(keystring);
        pw.close();
    }

    private static String keyToString(Key key){
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    */

}
