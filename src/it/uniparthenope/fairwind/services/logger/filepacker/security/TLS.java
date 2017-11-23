package it.uniparthenope.fairwind.services.logger.filepacker.security;

import mjson.Json;

import javax.crypto.*;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Created by marioruggieri on 16/11/2017.
 */

public class TLS {

    // TLS layer is composed of an encryptor and a digital signer
    private Encryptor encryptor;
    private DigitalSigner digitalSigner;

    public TLS(String destPublicKey, String srcPrivateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, UnsupportedEncodingException, InvalidKeyException {
        // get publicKey from string
        PublicKey destPBK = stringToPublicKey(destPublicKey);

        // get privateKey from string
        PrivateKey srcPRK = stringToPrivateKey(srcPrivateKey);

        encryptor = new Encryptor(destPBK);
        digitalSigner = new DigitalSigner(srcPRK);
    }

    public String obfuscate(Json json) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        String jsonAsString=json.toString();
        return encryptor.encrypt(jsonAsString);
    }

    public String getIV() throws BadPaddingException, IllegalBlockSizeException {
        // Get clear IV encoded as a Base64 string to send it
        return encryptor.getIV();  // No problem for a clear IV
    }

    public String getObfuscatedKey() throws BadPaddingException, IllegalBlockSizeException {
        // encrypt the AES key using the RSA public key
        // encode the encrypted AES key as a Base64 string to send it
        return encryptor.getObfuscatedAESKey();  // Key must be encrypted using RSA
    }

    public String sign(Json json) throws InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {
        String data = json.toString();
        return digitalSigner.sign(data);
    }

    private PublicKey stringToPublicKey(String pbk) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // decode from string to binary[]
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(pbk));
        // generate a PublicKey object from the binary array
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private PrivateKey stringToPrivateKey(String pvk) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec specPriv = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pvk));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(specPriv);
    }

}
