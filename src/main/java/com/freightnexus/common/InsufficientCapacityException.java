package com.freightnexus.common;

public class InsufficientCapacityException extends RuntimeException {
    public InsufficientCapacityException(String message) { super(message); }
}
