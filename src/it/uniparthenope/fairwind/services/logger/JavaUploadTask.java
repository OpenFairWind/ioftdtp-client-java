package it.uniparthenope.fairwind.services.logger;

import it.uniparthenope.fairwind.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by marioruggieri on 11/10/2017.
 */

public class JavaUploadTask extends UploadTaskBase {

    public static final String LOG_TAG = "JAVA UPLOAD TASK";

    public JavaUploadTask(String httpClientClassName, String uploadUrl, Uploader uploader) {
        super(httpClientClassName, uploadUrl, uploader);
    }

    public boolean netIsAvailable() {
        try {
            Log.d(LOG_TAG,"Checking if network is available...");
            final URL url = new URL("http://fairwind.cloud:5050");
            final URLConnection conn = url.openConnection();
            conn.connect();
            return true;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return false;
        }
    }

    public boolean execute(File[] files) {
        if (files.length != 0) {
            if (netIsAvailable()) {
                UploadTask uploadTask = new UploadTask(files);
                uploadTask.start();
            }
            else {
                Log.d(LOG_TAG,"Network not available!");
                return false; //false only if net is not available
            }
        }
        return true;
    }

    private class UploadTask extends Thread {

        File[] files;

        public UploadTask(File[] files) {
            this.files = files;
        }

        public void run() {
            for (File file : files) {
                if (!isUploading(file.getAbsolutePath())) {
                    Log.d(LOG_TAG, "Clients:" + getCurrentClients() + "/" + getAvailableClients());
                    if (getAvailableClients() - getCurrentClients() > 0) {
                        putInUploading(file.getAbsolutePath());
                        Log.d("Upload:", "FileName:" + file.getName());
                        try {
                            // Get class from its name
                            // In this case client is an AsyncHttpClient which is an HttpClientBase
                            // with onPost implemented
                            Class<?> clazz = Class.forName(getHttpClientClassName());
                            Constructor<?> ctor = clazz.getConstructor(Uploader.class);
                            HttpClientBase client = (HttpClientBase)ctor.newInstance(getUploader()); //get concrete istance
                            try {
                                client.post(getUploadUrl(), file);    //AsyncHttpClient generates a thread for each post
                            } catch (FileNotFoundException e) {
                                Log.e(LOG_TAG, e.getMessage());
                            }
                        } catch (ClassNotFoundException ex1) {
                            throw new RuntimeException(ex1);
                        } catch (NoSuchMethodException ex2) {
                            throw new RuntimeException(ex2);
                        } catch (IllegalAccessException ex3) {
                            throw new RuntimeException(ex3);
                        } catch (InvocationTargetException ex4) {
                            throw new RuntimeException(ex4);
                        } catch (InstantiationException ex5) {
                            throw new RuntimeException(ex5);
                        }
                    } else break;
                }
            }
        }
    }
}