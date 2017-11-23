package it.uniparthenope.fairwind.services;

import com.google.common.eventbus.Subscribe;
import it.uniparthenope.fairwind.Log;
import it.uniparthenope.fairwind.model.UpdateException;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.event.PathEvent;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;

/**
 * Created by raffaelemontella on 07/07/2017.
 */
public abstract class DataListener {
    private static final String LOG_TAG = "DATA_LISTENER";

    private String name;
    public String getName() {
        return name;
    }

    protected String type="Base Data Listener";
    public String getType() {return type;}

    public abstract long getTimeout();
    public abstract void onStart() throws DataListenerException;
    public abstract void onStop();
    public abstract void onUpdate(PathEvent pathEvent) throws UpdateException;
    public abstract boolean onIsAlive();
    public abstract boolean mayIUpdate();

    public void start() throws DataListenerException {

        if (!isAlive()) {
            //Log.d(LOG_TAG, "start");
            done=false;
            onStart();
            // Register to all events
            SignalKModel signalKModel=SignalKModelFactory.getInstance();
            signalKModel.getEventBus().register(this);
        }

    }

    public boolean isInput() {
        return false;
    }

    public boolean isOutput() {
        return false;
    }

    private boolean done=false;
    protected void setDone() {
        done=true;
    }
    protected boolean isDone() { return done; }

    public void stop() {
        if (isAlive()) {
            //Log.d(LOG_TAG,"stop");
            onStop();
            done=true;
            // Unregister to all events
            SignalKModel signalKModel=SignalKModelFactory.getInstance();
            signalKModel.getEventBus().unregister(this);
        }
    }

    public boolean isAlive() {
        boolean result=false;
        result=onIsAlive();
        return result;
    }

    public DataListener() {
    }


    public DataListener(String name) {
        this.name=name;
        init();
    }

    private void init() {

    }

    public void process(SignalKModel signalKObject) {
        SignalKModel signalKModel = SignalKModelFactory.getInstance();

        if (signalKObject != null && !signalKObject.getData().isEmpty() ) {

            //Log.d(LOG_TAG,"Put All SignalK new data");
            signalKModel.putAll(signalKObject.getData());
        }

        //Log.d(LOG_TAG, "process -> size: " + signalKModel.getData().size());
    }

    @Subscribe
    public void onEvent(PathEvent pathEvent) {
        //Log.d(LOG_TAG,"onEvent:"+pathEvent.getPath());
        //Log.d(LOG_TAG, "Is to update " + getName() + "? "+isOutput()+" "+mayIUpdate());
        if (isOutput()  && mayIUpdate()  ) {
            try {
                //Log.d(LOG_TAG, "updating -> " + getName());
                onUpdate(pathEvent);
            } catch (UpdateException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }
}
