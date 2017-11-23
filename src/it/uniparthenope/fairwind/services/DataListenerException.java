package it.uniparthenope.fairwind.services;

/**
 * Created by raffaelemontella on 07/07/2017.
 */
public class DataListenerException extends Exception {
    public DataListenerException(Exception exception) {
        super(exception);
    }

    public DataListenerException(String msg) {
        super(msg);
    }

    public DataListenerException(String msg, Exception exception) {
        super(msg,exception);
    }
}
