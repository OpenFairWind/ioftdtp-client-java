package it.uniparthenope.fairwind.services.logger.filepacker.security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Created by marioruggieri on 16/11/2017.
 */

public class DigitalSigner {

    private Cipher RSACipher;
    private final static String RSA = "RSA/None/OAEPWithSHA1AndMGF1Padding";
    private final static String provider = "BC";

    public DigitalSigner(PrivateKey privateRSAKey) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, NoSuchProviderException {
        // RSA cipher
        RSACipher = Cipher.getInstance(RSA, provider);
        RSACipher.init(Cipher.ENCRYPT_MODE, privateRSAKey);
    }

    public String sign(String data) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        byte[] tag = sha256(data);
        String signature = obfuscate(tag);
        return signature;
    }

    private String obfuscate(byte[] tag) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException
    {
        // encrypt the string representing the json using the AES key
        // encode the output as a Base64 string to send it
        return Base64.getEncoder().encodeToString(RSACipher.doFinal(tag));
    }

    private byte[] sha256(String s) {
        final String SHA = "SHA-256";
        try {
            // create sha256 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance(SHA);
            digest.update(s.getBytes());

            return digest.digest();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
