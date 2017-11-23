package it.uniparthenope.fairwind.services.logger.filepacker;

import mjson.Json;

import java.io.*;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by marioruggieri on 17/10/2017.
 */
public class JsonGZipper {

    public static String compress(Json jsonData) throws IOException {
        String data = jsonData.toString();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data.getBytes());
        gzip.close();
        byte[] compressed = bos.toByteArray();
        bos.close();
        return Base64.getEncoder().encodeToString(compressed);
    }

    public static Json decompress(String compressedString) throws IOException {
        byte[] compressed = Base64.getDecoder().decode(compressedString);
        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(bis);
        BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        gis.close();
        bis.close();
        String jsonAsString = sb.toString();
        return Json.read(jsonAsString);
    }
}

