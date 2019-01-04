package com.incomm.ecaas.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

import com.incomm.ecaas.dao.CredentialsProvider;
import com.incomm.ecaas.model.AuthHeader;
import com.incomm.ecaas.utils.HmacSignatureBuilder;
import com.incomm.ecaas.utils.HmacUtil;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * Restricts access to resource if HMAC signature is not valid.
 * 
 */
public class HmacAccessFilter extends OncePerRequestFilter {

	@Autowired
	private CredentialsProvider credentialsProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		final AuthHeader authHeader = HmacUtil.getAuthHeader(request);

		if (authHeader == null) {
			// invalid authorization token
			logger.warn("Authorization header is missing");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		final String apiKey = authHeader.getApiKey();

		final byte[] apiSecret = credentialsProvider.getApiSecret(apiKey);
		if (apiSecret == null) {
			// invalid digest
			logger.error("Invalid API key");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid authorization data");
			return;
		}

		// If HttpMethod.GET then get query string as payload
		CachingRequestWrapper requestWrapper = new CachingRequestWrapper(request);

		byte[] contentAsByteArray = null;
		if (HttpMethod.GET.name().equals(request.getMethod())) {
			// If path param/path variable is being used pass in an ampersand as payload
			if (StringUtils.isBlank(request.getQueryString())) {
				contentAsByteArray = "?".getBytes(StandardCharsets.UTF_8);
			} else {
				contentAsByteArray = ("?" + request.getQueryString()).getBytes(StandardCharsets.UTF_8);
			}
		} else {
			contentAsByteArray = requestWrapper.getContentAsByteArray();
		}

		final HmacSignatureBuilder signatureBuilder = new HmacSignatureBuilder().algorithm(authHeader.getAlgorithm())
				.scheme(request.getScheme()).host(request.getServerName() + ":" + request.getServerPort())
				.method(request.getMethod()).resource(request.getRequestURI()).contentType(request.getContentType())
				.date(request.getHeader(HttpHeaders.DATE)).nonce(authHeader.getNonce()).apiKey(apiKey)
				.apiSecret(apiSecret).payload(contentAsByteArray);

		if (!signatureBuilder.isHashEquals(authHeader.getDigest())) {
			// invalid digest
			logger.error("Invalid digest");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid authorization data");
			return;
		}

		filterChain.doFilter(requestWrapper, response);
	}
}
