package com.kapil.jpl.exceptions;

public class ReturnException extends RuntimeException {
    private final Object value;
    public ReturnException(Object value) { this.value = value; }
    public Object getValue() { return value; }
}
