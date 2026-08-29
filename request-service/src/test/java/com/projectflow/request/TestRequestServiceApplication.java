package com.projectflow.request;

import org.springframework.boot.SpringApplication;

public class TestRequestServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(RequestServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
