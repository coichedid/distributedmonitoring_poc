package com.coichedid.pocinfragateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication //Enable spring application with web server
@EnableZuulProxy // Setups zuul gateway
@RestController // This is a controller of api
public class PocinfragatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocinfragatewayApplication.class, args);
	}

	@Bean // Force sample every request to zipkin
	public AlwaysSampler defaultSampler() {
	  return new AlwaysSampler();
	}
}
