package it.uniparthenope.fairwind.services.logger.filepacker;

import it.uniparthenope.fairwind.Log;
import it.uniparthenope.fairwind.services.logger.filepacker.security.TLS;
import mjson.Json;

import java.io.*;
import java.security.GeneralSecurityException;

/**
 * Created by raffaelemontella on 17/07/2017.
 */
public class SecureFilePackerImpl implements SecureFilePacker {

    public static final String LOG_TAG="SecureFilePackerImpl";

    private String packageName="it.uniparthenope.fairwind";
    private String userId;
    private String deviceId;
    private String publicKey;
    private String privateKey;

    public SecureFilePackerImpl(String userId, String deviceId, String destPublicKey, String sourcePrivateKey) {
        this.userId=userId;
        this.deviceId=deviceId;
        this.publicKey = destPublicKey;
        this.privateKey = sourcePrivateKey;
    }

    public String getUserId() { return userId; }

    @Override
    public void pack(File source, File destination) throws IOException, GeneralSecurityException {

        String userId=getUserId();
        if (userId!=null && userId.isEmpty()==false && deviceId!=null && deviceId.isEmpty()==false) {

            BufferedReader reader = new BufferedReader(new FileReader(source));
            String         line = null;
            StringBuilder  stringBuilder = new StringBuilder();

            String dataAsString=null;
            try {
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(",");
                }

                dataAsString = "{\"data\":[" + stringBuilder.toString() + "]}";
                reader.close();

                if (dataAsString != null && dataAsString.isEmpty() == false) {

                    // New TLS layer for encryption and digital signature
                    TLS tls = new TLS(publicKey, privateKey);

                    // Read the source file as a Json object
                    Json jsonData = Json.read(dataAsString);

                    // Compress data into data field
                    String compressedData = JsonGZipper.compress(jsonData.at("data"));

                    // Calculate the signature from its string representation
                    String signature = tls.sign(jsonData);

                    // Create a new Json object
                    Json jsonPack = Json.object();
                    // add the signature
                    jsonPack.set("signature", signature);
                    // add the compressed data
                    jsonPack.set("data", compressedData);

                    // get obfuscated json, obfuscated AES key and clear IV
                    String obfuscatedPack = tls.obfuscate(jsonPack);
                    String IV = tls.getIV();
                    String obfuscatedAESKey = tls.getObfuscatedKey();

                    // put them into a new json
                    Json finalJsonPack = Json.object();
                    finalJsonPack.set("dataPack",obfuscatedPack);
                    finalJsonPack.set("key",obfuscatedAESKey);
                    finalJsonPack.set("IV",IV);

                    // json to string
                    String finalPack = finalJsonPack.toString();

                    // generate the final json file
                    String destinationPath = destination.getAbsolutePath()+File.separator + deviceId+"_"+source.getName() +  ".aes.json";
                    FileWriter fw = new FileWriter(new File(destinationPath));
                    fw.write(finalPack);
                    fw.flush();
                    fw.close();

                    /*
                    // Use the string as input stream
                    InputStream is = new ByteArrayInputStream(finalPack.getBytes(StandardCharsets.UTF_8));

                    // Create the buffered input stream
                    BufferedInputStream origin = new BufferedInputStream(is, BUFFER);

                    String destinationPath = destination.getAbsolutePath()+File.separator + deviceId+"_"+source.getName() +  ".aes.zip";

                    Log.d(LOG_TAG, "secureFilePacking:" + source.getAbsolutePath() + " -> " + destinationPath);

                    // Create the destination file
                    FileOutputStream dest = new FileOutputStream(destinationPath);

                    // Compress the file
                    ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
                    byte data[] = new byte[BUFFER];
                    ZipEntry entry = new ZipEntry(deviceId+"_"+source.getName() + ".aes");
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    out.close();
                    */
                }
            } catch (IOException ex) {
                Log.d(LOG_TAG,ex.getMessage());
            }
        }
    }

}
