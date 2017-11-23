package it.uniparthenope.fairwind.services.logger.filepacker.security;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;    //each 6 bits represent a number which is converted into an ASCII character

/**
 * Created by marioruggieri on 16/11/2017.
 */
public class Encryptor {

    // Asymmetric part
    private Cipher RSACipher;
    private PublicKey RSAKey;

    // Symmetric part
    private Cipher AESCipher;
    private IvParameterSpec IV;
    private SecretKey AESkey;

    private final static String RSA = "RSA";
    private final static String AES = "AES/CBC/PKCS5Padding";

    public Encryptor(PublicKey RSAPublickey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        // generate RSA key
        RSAKey = RSAPublickey;

        // generate AES128 secure key
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128);
        AESkey = generator.generateKey();

        // generate secure random IV
        AESCipher = Cipher.getInstance(AES);
        SecureRandom randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
        byte[] iv = new byte[AESCipher.getBlockSize()];
        randomSecureRandom.nextBytes(iv);
        IV = new IvParameterSpec(iv);

        // RSA cipher
        RSACipher = Cipher.getInstance(RSA);
        RSACipher.init(Cipher.ENCRYPT_MODE, RSAKey);

        // AES cipher
        AESCipher.init(Cipher.ENCRYPT_MODE, AESkey, IV);
    }

    public String getIV() throws BadPaddingException, IllegalBlockSizeException {
        // Get clear IV encoded as a Base64 string to send it
        return Base64.getEncoder().encodeToString(IV.getIV());   // No problem for a clear IV
    }

    public String getObfuscatedAESKey() throws BadPaddingException, IllegalBlockSizeException {
        // encrypt the AES key using the RSA public key
        // encode the encrypted AES key as a Base64 string to send it
        return Base64.getEncoder().encodeToString(RSACipher.doFinal(AESkey.getEncoded()));   // Key must be encrypted using RSA
    }

    public String encrypt(String text) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        // encrypt the string representing the json using the AES key
        // encode the output as a Base64 string to send it
        return Base64.getEncoder().encodeToString(AESCipher.doFinal(text.getBytes()));
    }

}

