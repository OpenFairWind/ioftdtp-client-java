package it.uniparthenope.fairwind.services.logger;

import it.uniparthenope.fairwind.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by marioruggieri on 11/10/2017.
 */

public class JavaUploadTask extends UploadTaskBase {

    public static final String LOG_TAG = "JAVA UPLOAD TASK";

    public JavaUploadTask(String httpClientClassName, String uploadUrl, Uploader uploader) {
        super(httpClientClassName, uploadUrl, uploader);
    }

    public void execute(File[] files) {
        UploadTask uploadTask = new UploadTask(files);
        uploadTask.start();
    }

    private class UploadTask extends Thread {

        File[] files;

        public UploadTask(File[] files) {
            this.files = files;
        }

        public void run() {
            for (File file : files) {
                if (!isUploading(file.getAbsolutePath())) {
                    putInUploading(file.getAbsolutePath());
                    Log.d(LOG_TAG, "Clients:" + getCurrentClients() + "/" + getAvailableClients());
                    if (getAvailableClients() - getCurrentClients() > 0) {
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


