package it.uniparthenope.fairwind.services.signalkclient;

import it.uniparthenope.fairwind.Log;
import it.uniparthenope.fairwind.model.UpdateException;
import it.uniparthenope.fairwind.services.DataListener;
import mjson.Json;
import nz.co.fortytwo.signalk.handler.DeltaToMapConverter;
import nz.co.fortytwo.signalk.handler.FullToMapConverter;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.event.PathEvent;
import nz.co.fortytwo.signalk.util.SignalKConstants;
import org.atmosphere.wasync.*;

import java.io.IOException;
import java.util.List;

/**
 * Created by raffaelemontella on 07/07/2017.
 */
public class SignalkWebSocketClient extends DataListener {
    // The Log tag
    private static final String LOG_TAG = "SIGNALK_WSOCKET_CLIENT";

    private boolean running=false;


    private int timeout=1000;
    private String uri="";
    private String signalKUuid="";
    private Client client;
    private RequestBuilder request;
    private Socket socket;

    private boolean changeSelf=false;
    private String fromSelf;


    private DeltaToMapConverter deltaToMapConverter=new DeltaToMapConverter();
    private FullToMapConverter fullToMapConverter=new FullToMapConverter();


    @Override
    public long getTimeout() { return timeout; }

    public SignalkWebSocketClient(String name, String uri)  {
        super(name);
        this.uri=uri;
        init();
    }

    private void init() {
        type="SignalK WebSocket Client";
    }


    public void onStart() {

        if (!uri.toLowerCase().startsWith("ws://")) {
            uri="ws://"+uri;
        }

        System.out.println("Uri -> "+uri);


        client = ClientFactory.getDefault().newClient();

        request = client.newRequestBuilder()
                .method(Request.METHOD.GET)
                .uri(uri)
                .encoder(new Encoder<Json, String>() {
                    @Override
                    public String encode(Json data) {
                        System.out.println("Encode -> "+data.toString());
                        return data.asString();
                    }
                })
                .decoder(new Decoder<String, Json>() {
                    @Override
                    public Json decode(Event type, String data) {
                        //Log.d(LOG_TAG, "Decode -> " + data);
                        data = data.trim();

                        // Padding
                        if (data.length() == 0) {
                            return null;
                        }
                        if (type.name().equals("MESSAGE")) {
                            return Json.read(data);
                        }
                        return null;
                    }

                })
                .transport(Request.TRANSPORT.WEBSOCKET);

        socket = client.create();
        try {
            socket.on("MESSAGE", new Function<Json>() {
                @Override
                public void on(final Json t) {  //handle new json data
                    //Log.d(LOG_TAG,"Socket:message");
                    if (t!=null) {
                        // Log.d(LOG_TAG,"t:"+t.toString());
                        SignalKModel signalKObject = null;
                        try {

                            if (changeSelf==true) {
                                String context=t.at("context").asString();
                                if (  context.endsWith(fromSelf)) {
                                    context = context.replace(fromSelf, SignalKConstants.self);
                                    t.set("context", context);
                                }
                            }

                            signalKObject = deltaToMapConverter.handle(t);
                            if (signalKObject == null) {
                                signalKObject = fullToMapConverter.handle(t);
                            }

                            if (signalKObject != null) {
                                //System.out.println("JSON segment ready to post!");
                                process(signalKObject); //post using EventBus the json data
                                // Log.d(LOG_TAG,"Socket:data model updated");
                            } else {
                                if (t.at("roles")!=null && t.at("name")!=null && t.at("self")!=null && t.at("version")!=null) {
                                    List<Json> roles=t.at("roles").asJsonList();
                                    String name=t.at("name").asString();
                                    String version=t.at("version").asString();
                                    String self = t.at("self").asString();
                                    Log.d(LOG_TAG,"Socket:"+name+"("+version+")");

                                    if (self.equals(SignalKConstants.self)==false) {

                                        for (Json role : roles) {
                                            if (role.asString().equals("master") || role.asString().equals("main")) {
                                                changeSelf = true;
                                                fromSelf = self;
                                                break;
                                            }
                                        }
                                        if (signalKUuid.isEmpty() == false) {
                                            changeSelf = true;
                                            fromSelf = signalKUuid;
                                        }

                                        if (changeSelf) {
                                            Log.d(LOG_TAG, "Change Self: " + self + "->" + SignalKConstants.self);
                                        }
                                        //Toast.makeText(FairWindApplication.getInstance(),name+"("+version+")",Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            //Log.d(LOG_TAG, "Socket -> " + e.getMessage());
                            throw new RuntimeException(e);
                            //e.printStackTrace();
                        }
                    }
                }
            })
            .on(new Function<Throwable>() {

                @Override
                public void on(Throwable t) {
                    Log.d(LOG_TAG,"Throwable. "+t.getMessage());
                    stop();
                }
            })
            .open(request.build());
        } catch (IOException e) {
            Log.d(LOG_TAG,e.getMessage());
            stop();
        }
        running=true;

    }

    public void onStop() {
        if (socket.status() == Socket.STATUS.OPEN) {
            socket.close();
            running = false;
        }
    }

    @Override
    public void onUpdate(PathEvent pathEvent) throws UpdateException {
        Log.d(LOG_TAG,"Event: "+pathEvent.getPath());

    }

    @Override
    public boolean onIsAlive() {
        return running;
    }

    @Override
    public boolean mayIUpdate() {
        return false;
    }


}
