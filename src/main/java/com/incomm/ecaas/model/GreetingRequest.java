package com.incomm.ecaas.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GreetingRequest {

	List<String> names = new ArrayList<>();

	public List<String> getNames() {
		return names;
	}

	public void setNames(List<String> names) {
		this.names = names;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GreetingRequest [names=").append(names).append("]");
		return builder.toString();
	}
	
}
