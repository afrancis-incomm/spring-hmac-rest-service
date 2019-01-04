package com.incomm.ecaas.model;

import java.util.Arrays;

/**
 * Represents contents for HMAC HTTP <code>Authorization</code> header.
 */
public class AuthHeader {

	private final String algorithm;
	private final String apiKey;
	private final String nonce;
	private final byte[] digest;

	public AuthHeader(String algorithm, String apiKey, String nonce, byte[] digest) {
		this.algorithm = algorithm;
		this.apiKey = apiKey;
		this.nonce = nonce;
		this.digest = digest;
	}

	public AuthHeader(String algorithm, String apiKey, byte[] digest) {
		this(algorithm, apiKey, null, digest);
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public String getApiKey() {
		return apiKey;
	}

	public byte[] getDigest() {
		return digest;
	}

	public String getNonce() {
		return nonce;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AuthHeader [algorithm=").append(algorithm).append(", apiKey=").append(apiKey).append(", nonce=")
				.append(nonce).append(", digest=").append(Arrays.toString(digest)).append("]");
		return builder.toString();
	}
}
