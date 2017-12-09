package it.uniparthenope.fairwind.services.logger.filepacker.security;

import java.io.*;
import java.security.*;
import java.util.Base64;

/**
 * Created by marioruggieri on 17/10/2017.
 */
public class RSAKeysGenerator {

    private String privateKey;
    private String publicKey;

    public RSAKeysGenerator(String prkPath, String pbkPath, int size) throws NoSuchAlgorithmException, IOException {
        File prkf = new File(prkPath); File pbkf = new File(pbkPath);

        // if keys don't exist
        if ( !prkf.exists() || !pbkf.exists() ) {
            System.out.println("Generating the key pair...");
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(size);
            KeyPair kp = kpg.generateKeyPair();

            publicKey = keyToString(kp.getPublic());
            privateKey = keyToString(kp.getPrivate());

            storeKey(privateKey,prkf);
            storeKey(publicKey,pbkf);
        }
        else {  //else get them from files
            System.out.println("Getting keys from existing files...");
            FileReader fr = new FileReader(prkf);
            BufferedReader br = new BufferedReader(fr);
            privateKey = br.readLine();

            fr = new FileReader(pbkf);
            br = new BufferedReader(fr);
            publicKey = br.readLine();
        }
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    private static void storeKey(String keystring, File f) throws IOException {
        PrintWriter pw = new PrintWriter(f);
        pw.write(keystring);
        pw.close();
    }

    private static String keyToString(Key key){
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

}
