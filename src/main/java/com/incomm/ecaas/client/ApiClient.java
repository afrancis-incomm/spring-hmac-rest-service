package com.incomm.ecaas.client;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incomm.ecaas.model.Greeting;
import com.incomm.ecaas.model.GreetingRequest;
import com.incomm.ecaas.utils.HmacSignatureBuilder;

public class ApiClient {

	private static final String USER_AGENT = "ECAAS_REST_Client";
	private RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
	private ObjectMapper objectMapper = new ObjectMapper();
	private final CredentialsProvider credentialsProvider;

	private final Clock clock = Clock.systemUTC();

	private final String scheme;
	private final String host;
	private final HttpMethod method;
	private final int port;

	public ApiClient(CredentialsProvider credentialsProvider, HttpMethod method, String scheme, String host, int port) {
		this.credentialsProvider = credentialsProvider;
		this.scheme = scheme;
		this.host = host;
		this.port = port;
		this.method = method;
	}

	public ApiClient(HttpMethod method, String host, int port) {
		this(new BasicCredentialsProvider(), method, (port == 443) ? "https" : "http", host, port);
	}

	public <T> T invokeAPI(Object request, String operationName, Class<T> clazz) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		final AuthScope authScope = new AuthScope(host, port, AuthScope.ANY_REALM, scheme);
		final Credentials credentials = credentialsProvider.getCredentials(authScope);

		if (credentials == null) {
			throw new RuntimeException("Can't find credentials for AuthScope: " + authScope);
		}

		String apiKey = credentials.getUserPrincipal().getName();
		String apiSecret = credentials.getPassword();

		String nonce = UUID.randomUUID().toString();

		headers.setDate(clock.millis());
		String dateString = headers.getFirst(HttpHeaders.DATE);

		final String valueAsString;
		try {
			valueAsString = objectMapper.writeValueAsString(request);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		final String resource = "/api/" + operationName;
		UriComponentsBuilder builder1 = null;
		if (HttpMethod.GET.equals(method)) {
			builder1 = UriComponentsBuilder.newInstance().queryParam("name", request);
		}

		final HmacSignatureBuilder signatureBuilder = new HmacSignatureBuilder().scheme(scheme).host(host + ":" + port)
				.method(method.name()).resource(resource).apiKey(apiKey).contentType(MediaType.APPLICATION_JSON_VALUE)
				.nonce(nonce).date(dateString).apiSecret(apiSecret)
				.payload(method.equals(HttpMethod.GET) ? builder1.toUriString().getBytes(StandardCharsets.UTF_8)
						: valueAsString.getBytes(StandardCharsets.UTF_8));
		final String signature = signatureBuilder.buildAsBase64String();

		final String authHeader = signatureBuilder.getAlgorithm() + " " + apiKey + ":" + nonce + ":" + signature;
		headers.add(HttpHeaders.AUTHORIZATION, authHeader);

		headers.add(HttpHeaders.USER_AGENT, USER_AGENT);

		if (HttpMethod.GET.equals(method)) {
			HttpEntity<?> entity = new HttpEntity<>(headers);
			UriComponentsBuilder builder = UriComponentsBuilder
					.fromHttpUrl(scheme + "://" + host + ":" + port + resource).queryParam("name", request);
			return restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, clazz).getBody();
		} else if (HttpMethod.POST.equals(method)) {
			if (request instanceof GreetingRequest) {
				HttpEntity<GreetingRequest> entity = new HttpEntity<>((GreetingRequest) request, headers);
				ResponseEntity<List<Greeting>> resp = restTemplate.exchange(
						scheme + "://" + host + ":" + port + resource, HttpMethod.POST, entity,
						new ParameterizedTypeReference<List<Greeting>>() {
						});
				System.out.println(resp.getBody());
			} else {
				HttpEntity<List<String>> entity = new HttpEntity<>((List<String>) request, headers);
				ResponseEntity<List<Greeting>> resp = restTemplate.exchange(
						scheme + "://" + host + ":" + port + resource, HttpMethod.POST, entity,
						new ParameterizedTypeReference<List<Greeting>>() {
						});
				System.out.println(resp.getBody());
			}
		} else if (HttpMethod.PUT.equals(method)) {
			HttpEntity<Greeting> entity = new HttpEntity<>((Greeting) request, headers);
			return restTemplate.exchange(scheme + "://" + host + ":" + port + resource, HttpMethod.PUT, entity, clazz)
					.getBody();
		} else if (HttpMethod.DELETE.equals(method)) {
			HttpEntity<Greeting> entity = new HttpEntity<>((Greeting) request, headers);
			restTemplate.exchange(scheme + "://" + host + ":" + port + resource, HttpMethod.DELETE, entity, clazz);
		}
		return null;
	}

	public void setCredentials(String user, String password) {
		credentialsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM),
				new UsernamePasswordCredentials(user, password));
	}

	public void setCredentials(AuthScope authScope, String user, String password) {
		credentialsProvider.setCredentials(authScope, new UsernamePasswordCredentials(user, password));
	}

	/**
	 * Run this method to test REST API operations exposed. Need to add a request
	 * filter/intercepter to add Authorization header for regular usage
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ApiClient cli1 = new ApiClient(HttpMethod.POST, "localhost", 8989);
		cli1.setCredentials("user", "secret");
		GreetingRequest req = new GreetingRequest();
		req.getNames().add("abc");
		req.getNames().add("def");
		req.getNames().add("ghi");
		System.out
				.println("The response for POST call is " + cli1.invokeAPI(req, "sayHelloPost", GreetingRequest.class));

		ApiClient cli0 = new ApiClient(HttpMethod.POST, "localhost", 8989);
		cli0.setCredentials("user", "secret");
		List<String> names = new ArrayList<>();
		names.add("e3333");
		names.add("daasd");
		System.out.println("The response for POST call is " + cli0.invokeAPI(names, "sayHelloListPost", List.class));

		ApiClient cli = new ApiClient(HttpMethod.GET, "localhost", 8989);
		cli.setCredentials("user", "secret");
		System.out.println("The response for GET call is " + cli.invokeAPI("Hello_World", "sayHelloGet", String.class));

		ApiClient cli2 = new ApiClient(HttpMethod.GET, "localhost", 8989);
		cli2.setCredentials("user", "secret");
		System.out.println("The response for GET call is " + cli2.invokeAPI(null, "sayHelloGet", String.class));

		ApiClient cli3 = new ApiClient(HttpMethod.PUT, "localhost", 8989);
		cli3.setCredentials("user", "secret");
		System.out.println("The response for PUT call is "
				+ cli3.invokeAPI(new Greeting(123, "PutGreeting"), "putGreeting", Greeting.class));

		ApiClient cli4 = new ApiClient(HttpMethod.DELETE, "localhost", 8989);
		cli4.setCredentials("user", "secret");
		System.out.println("The response for DELETE call is "
				+ cli4.invokeAPI(new Greeting(123, "PutGreeting"), "greetingDelete", Greeting.class));

		ApiClient cli5 = new ApiClient(HttpMethod.GET, "localhost", 8989);
		cli5.setCredentials("user", "secret");
		System.out.println(
				"The response for GET call after delete is " + cli5.invokeAPI("strange", "sayHelloGet", String.class));

	}
}
