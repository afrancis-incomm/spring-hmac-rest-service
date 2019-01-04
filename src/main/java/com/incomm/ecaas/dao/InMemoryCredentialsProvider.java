package com.incomm.ecaas.dao;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCredentialsProvider implements CredentialsProvider{

	private final Map<String, byte[]> secrets = new ConcurrentHashMap<>();

    public InMemoryCredentialsProvider() {
    }

    public InMemoryCredentialsProvider(String apiKey, String secret) {
        addCredentials(apiKey, secret);
    }

    public void addCredentials(String apiKey, String secret) {
        secrets.put(apiKey, secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public byte[] getApiSecret(String apiKey) {
        if (apiKey == null) {
            return null;
        }
        return secrets.get(apiKey);
    }
}
