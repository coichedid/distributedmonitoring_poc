package com.coichedid.pocjavaservice2;

import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by coichedid on 7/8/17.
 */
@Configuration
@ComponentScan("com.coichedid.pocjavaservice2")
public class Pocjavaservice2Configuration {
    @Bean
    public AlwaysSampler defaultSampler() {
        return new AlwaysSampler();
    }
}
