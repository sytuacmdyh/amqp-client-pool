package com.yzccz.rabbit.exceptions;

public class ChannelException extends RuntimeException {
    public ChannelException(String message, Throwable cause) {
        super(message, cause);
    }
}
