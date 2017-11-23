package it.uniparthenope.fairwind.services.logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.UUID;

import it.uniparthenope.fairwind.Log;
import mjson.Json;

/**
 * Created by raffaelemontella on 11/07/2017.
 */

public abstract class HttpClientBase {
    public static final String LOG_TAG="HTTPCLIENTBASE";

    private Uploader uploader;


    private long timeStamp;
    private File file;
    private String sessionId= UUID.randomUUID().toString();

    public HttpClientBase(Uploader uploader) {
        this.uploader=uploader;

    }
    public Uploader getUploader() { return uploader; }
    public void setUploader(Uploader uploader) { this.uploader=uploader; }
    public String getSessionId() { return sessionId; }

    public File getFile() { return file; }
    public long getTimeStamp() { return timeStamp; }


    public abstract boolean onPost(String url, File file) throws FileNotFoundException;

    public void post(String url, File file) throws FileNotFoundException {

        this.file = file;
        timeStamp = System.currentTimeMillis();

        if (onPost(url, file) == true) {
            Log.d(LOG_TAG, "CLIENT PUTTED!");
            uploader.put(sessionId, this);

        }

    }

    public void failure(String jsonString) {

        Json result=null;
        try {
            result=Json.read(jsonString);
        } catch (RuntimeException ex) {

        }
        if (result!=null) {
            Log.d(LOG_TAG, "result:" + result.toString());

            if (result.is("status", "fail")) {

            }
        }
        uploader.remove(sessionId);
        uploader.removeFromUploading(file.getAbsolutePath());
    }

    public void success(String jsonString) {
        Object semaphore=new Object();
        Json result=null;
        try {
            result=Json.read(jsonString);
        } catch (RuntimeException ex) {

        }
        if (result!=null) {
            Log.d(LOG_TAG, "result:" + result.toString());

            if (result.is("status", "fail")) {
                Log.d(LOG_TAG, "CLIENT REMOVED!");
                uploader.remove(sessionId);

            } else if (result.is("status", "success")) {
                Log.d(LOG_TAG, "CLIENT REMOVED!");
                uploader.remove(sessionId,file);
            }

            uploader.removeFromUploading(file.getAbsolutePath());
        }
    }
}

