package com.kapil.jpl.exceptions;

public class JPLException extends RuntimeException {
    public JPLException(String message) {
        super("🛑 JPL Error: " + message);
    }
}
