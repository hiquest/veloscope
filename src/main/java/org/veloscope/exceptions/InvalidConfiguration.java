package org.veloscope.exceptions;

public class InvalidConfiguration extends RuntimeException {

    public InvalidConfiguration(String text) {
        super(text);
    }

    public InvalidConfiguration(Throwable e) {
        super(e);
    }

}
