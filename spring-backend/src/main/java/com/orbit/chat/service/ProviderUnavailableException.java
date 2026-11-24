package com.orbit.chat.service;

public class ProviderUnavailableException extends RuntimeException {
    public ProviderUnavailableException() {
        super("KI:connect could not reply. Try again or check the backend key and model.");
    }
}
