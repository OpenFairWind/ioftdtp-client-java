package it.uniparthenope.fairwind;

import it.uniparthenope.fairwind.services.DataListener;
import it.uniparthenope.fairwind.services.DataListenerException;
import it.uniparthenope.fairwind.services.DataListeners;
import it.uniparthenope.fairwind.services.logger.DBHelper;
import it.uniparthenope.fairwind.services.logger.LoggerListener;
import it.uniparthenope.fairwind.services.logger.MyDBHelper;
import it.uniparthenope.fairwind.services.signalkclient.SignalkWebSocketClient;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.util.SignalKConstants;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class Main {

    private DataListeners dataListeners;

    public static void main(String[] args) throws InterruptedException {
	// write your code here
        Main main=new Main();
    }

    public Main() throws InterruptedException {
        String self="urn:mrn:signalk:uuid:00000000-0000-0000-0000-000000000000";
        String userId="raffaele.montella@uniparthenope.it";
        String deviceId="1109d7a4-7329-4b72-816d-ae7b5f2053f5";
        String boardLogPath="/Users/mario/IdeaProjects/SignalKLogger/log";

        DBHelper dbHelper = new MyDBHelper("/Users/mario/IdeaProjects/SignalKLogger/db/database.db");

        //dbHelper.insertFile("/tmp/file01.doc");
        //dbHelper.insertFile("/tmp/file02.doc");
        //dbHelper.insertFile("/tmp/file03.doc");

        System.out.println("Files to move to uploads");
        ArrayList<String> filesList=dbHelper.getAllFiles();
        for (String fileItem:filesList) {
            System.out.println(fileItem);
        }

        //dbHelper.deleteFile("/tmp/file03.doc");
        //dbHelper.deleteFile("/tmp/file02.doc");
        //dbHelper.deleteFile("/tmp/file01.doc");

        SignalKModel signalKModel = SignalKModelFactory.getCleanInstance();
        SignalKConstants.self=self;

        String wsUrl="ws://demo.signalk.org/signalk/v1/stream";//localhost:3000/signalk/v1/stream";
        dataListeners=new DataListeners();

        try {
            SignalkWebSocketClient signalkWebSocketClient = new SignalkWebSocketClient("SignalK", wsUrl);
            dataListeners.add(signalkWebSocketClient);
        } catch (DataListenerException ex) {
            throw new RuntimeException(ex);
        }

        TimeUnit.SECONDS.sleep(10); // TO AVOID NULL DATA THE FIRST TIME

        try {
            LoggerListener loggerListener=new LoggerListener("LoggerListener",true,boardLogPath,10000/*600000*/,8000/*300000*/);
            dataListeners.add(loggerListener);
        } catch (DataListenerException ex) {
            throw new RuntimeException(ex);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }
}
