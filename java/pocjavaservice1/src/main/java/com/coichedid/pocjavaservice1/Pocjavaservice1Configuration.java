package com.coichedid.pocjavaservice1;

import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.coichedid.pocjavaservice1")
public class Pocjavaservice1Configuration {

	@Bean
	public AlwaysSampler defaultSampler() {
	  return new AlwaysSampler();
	}

}
