package it.uniparthenope.fairwind.model;

/**
 * Created by raffaelemontella on 07/07/2017.
 */
public class UpdateException extends Exception {
    public UpdateException(Exception exception) {
        super(exception);
    }

    public UpdateException(String msg) {
        super(msg);
    }

    public UpdateException(String msg, Exception exception) {
        super(msg,exception);
    }
}

