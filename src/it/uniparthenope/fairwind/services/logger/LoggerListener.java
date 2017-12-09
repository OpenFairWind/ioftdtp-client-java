package it.uniparthenope.fairwind.services.logger;

import it.uniparthenope.fairwind.Log;
import it.uniparthenope.fairwind.Utils;
import it.uniparthenope.fairwind.model.UpdateException;
import it.uniparthenope.fairwind.services.DataListener;
import it.uniparthenope.fairwind.services.DataListenerException;
import it.uniparthenope.fairwind.services.logger.filepacker.SecureFilePacker;
import it.uniparthenope.fairwind.services.logger.filepacker.SecureFilePackerImpl;
import it.uniparthenope.fairwind.services.logger.filepacker.security.RSAKeysGenerator;
import mjson.Json;
import nz.co.fortytwo.signalk.handler.FullToDeltaConverter;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.event.PathEvent;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.util.JsonSerializer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by raffaelemontella on 07/07/2017.
 */
public class LoggerListener extends DataListener implements Runnable {
    private static final String LOG_TAG = "LOGGER_LISTENER";

    private int timeout=100;
    private String boardLogPath="";
    private boolean upload=false;
    private long millis=5000;//5000;
    private long updateMillis=6000;//15000;
    private long cutMillis=10000;//600000;
    private long fullMillis=8000;//300000;
    private long lastFullMillis=System.currentTimeMillis();
    private long lastCutMillis=lastFullMillis;
    private long lastUpdateMillis=lastFullMillis;
    private String currentFilename="";
    private static final String prkPath = "HERE_PATH_TO_PRIVATE_KEY";
    private static final String pbkPath = "HERE_PATH_TO_PUBLIC_KEY";
    private static final int rsaKeySize = 1024;

    private HashSet<String> pathEvents=new HashSet<String>();   //events cache in Algorithm 1 on the paper

    private ExecutorService mExecutor;

    private Uploader uploader;

    // The constructor
    public LoggerListener(String name, boolean upload, String boardLogPath, long cutMillis, long fullMillis) throws IOException, NoSuchAlgorithmException {
        super(name);

        this.upload=upload;
        this.boardLogPath=boardLogPath;
        this.cutMillis=cutMillis;
        this.fullMillis=fullMillis;

        String userId="raffaele.montella@uniparthenope.it";
        String deviceId= "1109d7a4-7329-4b72-816d-ae7b5f2053f5";
        String apiUrl="http://0.0.0.0:5057";

        // GET PUBLIC KEY FROM SERVER
        RSAKeysGenerator rkg = new RSAKeysGenerator(prkPath, pbkPath, rsaKeySize);
        String sourcePublicKey = rkg.getPublicKey();
        String sourcePrivateKey = rkg.getPrivateKey();

        String destPublicKey = exchangePublicKeys(apiUrl+"/generatekeys", sourcePublicKey);
        Log.d(LOG_TAG,"SOURCE PUBLIC KEY: " + sourcePublicKey);
        Log.d(LOG_TAG,"DESTINATION PUBLIC KEY: " + destPublicKey);

        if (destPublicKey != null) {
            SecureFilePacker secureFilePacker = new SecureFilePackerImpl(userId, deviceId, destPublicKey, sourcePrivateKey);

            Log.d(LOG_TAG, "apiUrl:" + apiUrl);
            Log.d(LOG_TAG, "userId:" + userId);
            Log.d(LOG_TAG, "deviceId:" + deviceId);
            Log.d(LOG_TAG, "upload:" + upload);

            // Check if userId and deviceId are consistent
            if (upload == true && userId != null && userId.isEmpty() == false && deviceId != null && deviceId.isEmpty() == false) {
                String uploadPath = boardLogPath + File.separator + "uploads";
                DBHelper dbHelper = new MyDBHelper("/Users/mario/IdeaProjects/SignalKLogger/db/database.db");
                int maxClients = Math.round(AsyncHttpClient.getNumberOfCores() * 1.6f);
                uploader = new Uploader("it.uniparthenope.fairwind.services.logger.AsyncHttpClient", dbHelper, secureFilePacker, apiUrl, userId, deviceId, uploadPath, maxClients);
                uploader.start();
            }
        }
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public void onStart() throws DataListenerException {
        mExecutor= Executors.newSingleThreadExecutor();
        mExecutor.submit(this);
    }

    @Override
    public void onStop() {

    }

    @Override
    public boolean onIsAlive() {
        if (mExecutor==null || mExecutor.isTerminated() || mExecutor.isShutdown() ) {
            return false;
        }
        return true;
    }

    @Override
    public boolean mayIUpdate() {
        return true;
    }

    @Override
    public  boolean isOutput() {
        return true;
    }

    @Override
    public  boolean isInput() {
        return false;
    }


    // when the websocketclient post a new json fragment onUpdate is called
    // because DataListener register itself to the BusEvent
    @Override
    public void onUpdate(PathEvent pathEvent) throws UpdateException {
        //Log.d(LOG_TAG,"onUpdate:"+pathEvent.getPath());
        long millis=System.currentTimeMillis();

        // Or cut time or first time so create a new file NAME
        if ((millis-lastCutMillis>cutMillis) || currentFilename==null || currentFilename.isEmpty()) {

            // if there is a file to upload and IS NOT EMPTY
            if (uploader!=null && currentFilename!=null && !currentFilename.isEmpty()) {
                Log.d(LOG_TAG,"Moving to upload folder...");
                uploader.moveToUpload();    //encrypt, compress and move files to upload folder
            }

            lastCutMillis=millis;

            // Creating a new file NAME
            Log.d(LOG_TAG,"Cutting...");
            //boolean added = false;
            //while (added==false) {
            Calendar c = Calendar.getInstance();

            SimpleDateFormat dfDate = new SimpleDateFormat("yyyyMMdd");
            dfDate.setTimeZone(TimeZone.getTimeZone("GMT"));
            String formattedDate = dfDate.format(c.getTime());

            SimpleDateFormat dfTime = new SimpleDateFormat("hhmmss");
            dfTime.setTimeZone(TimeZone.getTimeZone("GMT"));
            String formattedTime = dfTime.format(c.getTime());
            currentFilename = boardLogPath + File.separator + formattedDate + "Z" + formattedTime + ".signalk.json";
            Log.d(LOG_TAG, "New current file:" + currentFilename);
            if (uploader != null) {
                uploader.add(currentFilename);  //insert the new file in the local DB
            }
            //}

        }

        SignalKModel temp=null;

        // Why || lastCutMillis==millis ? Because if is the first time case
        // we have to create a file and to do it we have to write something (temp != null)
        if ( (millis-lastFullMillis>fullMillis) || lastCutMillis==millis ) {
            Log.d(LOG_TAG,"Dumping on file...");

            temp = SignalKModelFactory.getInstance(); //all document
            if (temp!=null) {
                lastFullMillis = millis;
                pathEvents.clear(); //clear cache
            }

        } else  {
            // add event to cache
            // keys cache (before . there is only the key)
            pathEvents.add(pathEvent.getPath().substring(0,pathEvent.getPath().lastIndexOf(".")));
            if (millis-lastUpdateMillis>updateMillis) {
                // get subtree which starts with the keys of pathEvents in temp
                Log.d(LOG_TAG,"Updating file...");
                temp = Utils.getSubTreeByKeys(pathEvents);
                if (temp!=null) {
                    lastUpdateMillis = millis;
                    pathEvents.clear(); //clear cache
                }
            }
        }

        // if there is something to write on currentFile
        // or is the first time so the file must be created
        if (temp!=null) {
            try {
                File file = new File(currentFilename);
                // Creating the new file or appending to the previous one
                FileWriter fileWriter = new FileWriter(file, true);
                PrintWriter printWriter = new PrintWriter(fileWriter);
                write(printWriter,temp);    //ADDED WRITING
                printWriter.flush();
                printWriter.close();
                fileWriter.close();
            } catch (IOException ex) {
                Log.e(LOG_TAG, ex.getMessage());
            }
        }

    }

    private void write(PrintWriter printWriter, SignalKModel temp) throws IOException {
        JsonSerializer jsonSerializer=new JsonSerializer();
        FullToDeltaConverter fullToDeltaConverter=new FullToDeltaConverter();
        String jsonString;

        Log.d(LOG_TAG,"Writing");
        Json jsonFull = jsonSerializer.writeJson(temp);

        List<Json> jsonDeltas = fullToDeltaConverter.handle(jsonFull);
        if (jsonDeltas != null && !jsonDeltas.isEmpty()) {
            for (Json jsonDelta : jsonDeltas) {
                if (jsonDelta.at("updates").asJsonList().size()>0) {
                    try {
                        Utils.fixSource(jsonDelta);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    jsonString = jsonDelta.toString();
                    Log.d(LOG_TAG, "L:delta " + jsonString);
                    printWriter.println(jsonString);
                }
            }
        }
    }

    private String exchangePublicKeys(String posturl, String sourcePublicKey) throws IOException {

        String url = posturl;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // add request header
        con.setRequestMethod("POST");
        con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty( "charset", "utf-8");

        String srcPublicKeyEncoded = sourcePublicKey;   //copying sourcePublicKey, in this way it will not be changed

        // need to encode the Byte64 string because of special characters +, = and /
        String urlParameters = "sourcePublicKey=" + URLEncoder.encode(srcPublicKeyEncoded,"UTF-8");

        // send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Json result = Json.read(response.toString());

        if (result.is("status","success")) {
            return result.at("publicKey").asString();
        }

        return null;
    }

    @Override
    public void run() {
        while (!isDone()) {
            Log.d(LOG_TAG,"Run");
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Log.e(LOG_TAG,e.getMessage());
            }
        }
    }

    /*
    @Override
    public void run() {
        while (!isDone()) {

            Log.d(LOG_TAG, "Upload...");

            // Each millis ms try to upload
            if (uploader!=null) {
                Log.d(LOG_TAG,"----> UPLOAD CALLED!!! <-----");
                uploader.upload();  // a new thread is generated
            }

            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Log.e(LOG_TAG,e.getMessage());
            }
        }
    }
    */
}


