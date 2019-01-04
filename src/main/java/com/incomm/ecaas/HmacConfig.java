package com.incomm.ecaas;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import com.incomm.ecaas.dao.CredentialsProvider;
import com.incomm.ecaas.dao.InMemoryCredentialsProvider;
import com.incomm.ecaas.filter.HmacAccessFilter;

@Configuration
public class HmacConfig {

	@Bean
	CredentialsProvider credentialsProvider() {
		return new InMemoryCredentialsProvider("user", "secret");
	}

	@Bean
	CommonsRequestLoggingFilter logFilter() {
		CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setMaxPayloadLength(10000);
		filter.setIncludeHeaders(true);
		filter.setAfterMessagePrefix("REQUEST DATA : ");
		return filter;
	}

	@Bean
	public FilterRegistrationBean<HmacAccessFilter> filterRegistration() {

		FilterRegistrationBean<HmacAccessFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(hmacFilter());
		registration.addUrlPatterns("/api/*");
		registration.setName("hmacFilter");
		registration.setOrder(1);
		return registration;
	}

	@Bean(name = "hmacFilter")
	HmacAccessFilter hmacFilter() {
		return new HmacAccessFilter();
	}
}
