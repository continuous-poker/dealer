package de.doubleslash.poker.dealer.exceptionHandling.exceptions;

import java.io.Serializable;

public class ObjectNotFoundException extends Exception implements Serializable {

    private static final long serialVersionUID = 1L;

    public ObjectNotFoundException() {
        super();
    }
    public ObjectNotFoundException(String msg) {
        super(msg);
    }
    public ObjectNotFoundException(String msg, Exception e)  {
        super(msg, e);
    }
}
