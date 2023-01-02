package de.doubleslash.poker.dealer.exceptionhandling.exceptions;

import java.io.Serial;
import java.io.Serializable;

public class ObjectNotFoundException extends Exception implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public ObjectNotFoundException() {
        super();
    }
    public ObjectNotFoundException(final String msg) {
        super(msg);
    }

}
