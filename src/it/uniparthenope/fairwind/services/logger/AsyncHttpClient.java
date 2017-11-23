package it.uniparthenope.fairwind.services.logger;


import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Param;
import com.ning.http.client.Response;
import com.ning.http.client.multipart.FilePart;
import it.uniparthenope.fairwind.Log;
import mjson.Json;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by raffaelemontella on 17/07/2017.
 */
public class AsyncHttpClient extends HttpClientBase {

    public static final String LOG_TAG="ASYNCHTTPCLIENT";

    private com.ning.http.client.AsyncHttpClient client;

    public AsyncHttpClient(Uploader uploader) {
        super(uploader);

        client = new  com.ning.http.client.AsyncHttpClient();
        //client.setTimeout(30000);
    }

    @Override
    public boolean onPost(String uploadUrl, File file) throws FileNotFoundException {
        boolean result=false;

        com.ning.http.client.AsyncHttpClient.BoundRequestBuilder boundRequestBuilder=client.preparePost(uploadUrl);
        boundRequestBuilder.setRequestTimeout(3000);

        ArrayList<Param> queryParams=new ArrayList<>();
        queryParams.add(new Param("userid", getUploader().getUserId()));
        queryParams.add(new Param("deviceid", getUploader().getDeviceId()));
        queryParams.add(new Param("sessionid",getSessionId()));

        if (file.exists()) {
            FilePart part = new FilePart("file", file);
            boundRequestBuilder.addBodyPart(part);
            boundRequestBuilder.addQueryParams(queryParams);
            boundRequestBuilder.execute(new AsyncCompletionHandler<Response>() {
                @Override
                public Response onCompleted(Response response) throws Exception {
                    int status = response.getStatusCode();
                    String responseBody = response.getResponseBody();
                    Log.d(LOG_TAG, "onSuccess:");
                    Log.d(LOG_TAG, "          headers ->" + response.getHeaders());
                    Log.d(LOG_TAG, "          responseBody ->" + responseBody);
                    if (status >= 200 && status < 300) {
                        success(responseBody);
                    } else {
                        failure(responseBody);
                    }
                    return response;
                }

                @Override
                public void onThrowable(Throwable t) {
                    // Something wrong happened.
                    String responseBody="{\"status\":\"fail\",\"message\":\""+t.getMessage()+"\"}";
                    failure(responseBody);
                }


            });
            Log.d(LOG_TAG,"-----> WAITING RESPONSE! <----");
            result=true;
        }

        return result;
    }

    // DIFFERENT FROM ANDROID IMPLEMENTATION
    public static int getNumberOfCores() {
        return Runtime.getRuntime().availableProcessors();
    }
}
