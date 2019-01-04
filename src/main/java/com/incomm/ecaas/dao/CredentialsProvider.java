package com.incomm.ecaas.dao;

public interface CredentialsProvider {

	byte[] getApiSecret(String apiKey);
}
