package com.coichedid.pocjavaservice1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
public class Pocjavaservice1Application {

	public static void main(String[] args) {
		SpringApplication.run(Pocjavaservice1Application.class, args);
	}

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
