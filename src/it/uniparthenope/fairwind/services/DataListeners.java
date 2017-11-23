package it.uniparthenope.fairwind.services;

import it.uniparthenope.fairwind.Log;

import java.util.Vector;

/**
 * Created by raffaelemontella on 18/07/2017.
 */
public class DataListeners {
    private static final String LOG_TAG = "DATALISTENERS";

    private Vector<DataListener> dataListeners;
    public Vector<DataListener> get() { return dataListeners; }

    public DataListeners() {
        dataListeners =new Vector<DataListener>();
    }

    public boolean remove(DataListener dataListener) {
        if (dataListener ==null) return false;
        dataListener.stop();
        return dataListeners.remove(dataListener);
    }

    public void removeAll() {
        for(DataListener dataListener : dataListeners) {
            dataListeners.remove(dataListener);
        }
    }

    public boolean add(DataListener dataListener) throws DataListenerException {

        if (dataListener != null) {
            Log.d(LOG_TAG,"Adding -> "+dataListener.getName());
            StarterTask starterTask=new StarterTask(dataListener);
            if (starterTask.get()) {
                return dataListeners.add(dataListener);
            }

        }
        return false;
    }

    class StarterTask extends Thread {
        private boolean result=false;
        private boolean done=false;
        private DataListener dataListener;


        public StarterTask(DataListener dataListener) {
            this.dataListener=dataListener;

        }

        public boolean get() {
            Log.d(LOG_TAG,"get");
            long t0= System.currentTimeMillis();
            this.start();
            while (!done && (System.currentTimeMillis()-t0)<dataListener.getTimeout()) {
                try {
                    Thread.sleep(dataListener.getTimeout()/10);
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG,e.getMessage());
                }
            }
            Log.d(LOG_TAG,"get ->" +result);
            return result;
        }


        public void run() {
            try {
                try {
                    dataListener.start();
                    result = true;
                } catch (SecurityException se) {
                    Log.e(LOG_TAG,se.getMessage());
                }
            } catch (DataListenerException dle) {
                Log.e(LOG_TAG,dle.getMessage());
            }
            done=true;
        }
    }

    public DataListener find(Class<?> cls) {
        // Get a valid reference
        DataListener result = null;
        for (DataListener dataListener : dataListeners) {
            if (cls.isInstance(dataListener)) {
                result = dataListener;
                break;
            }
        }
        return result;
    }

    public DataListener find(String name) {
        // Get a valid reference
        DataListener result = null;
        for (DataListener dataListener : dataListeners) {
            if (dataListener.getName().equals(name)) {
                result = dataListener;
                break;
            }
        }
        return result;
    }

}

