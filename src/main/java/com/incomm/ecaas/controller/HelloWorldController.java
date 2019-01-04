package com.incomm.ecaas.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.incomm.ecaas.model.Greeting;
import com.incomm.ecaas.model.GreetingRequest;

@RestController
@RequestMapping("/api")
public class HelloWorldController {

	private static final String TEMPLATE = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	@GetMapping("/sayHelloGet")
	@ResponseBody
	public Greeting sayHelloGet(@RequestParam(name = "name", required = false, defaultValue = "Stranger") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(TEMPLATE, name));
	}

	@PostMapping("/sayHelloPost")
	@ResponseBody
	public List<Greeting> sayHelloPost(@RequestBody GreetingRequest names) {
		List<Greeting> greetings = new ArrayList<>();
		names.getNames().forEach(name -> greetings.add(new Greeting(counter.incrementAndGet(), String.format(TEMPLATE, name))));
		return greetings;
	}
	
	@PostMapping("/sayHelloListPost")
	@ResponseBody
	public List<Greeting> sayHelloPost(@RequestBody List<String> names) {
		List<Greeting> greetings = new ArrayList<>();
		names.forEach(name -> greetings.add(new Greeting(counter.incrementAndGet(), String.format(TEMPLATE, name))));
		return greetings;
	}
	
	@PutMapping("/putGreeting")
	@ResponseBody
	public Greeting putGreeting(@RequestBody Greeting name) {
		return new Greeting(counter.incrementAndGet(), String.format(TEMPLATE, name.getContent()));
	}
	
	@DeleteMapping("/greetingDelete")
	public void deleteGreeting(@RequestBody Greeting greets) {
		counter.getAndDecrement();
	}
	
	
}
