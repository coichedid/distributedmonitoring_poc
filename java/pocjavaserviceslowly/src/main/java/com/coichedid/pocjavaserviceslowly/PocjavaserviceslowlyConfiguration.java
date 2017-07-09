package com.coichedid.pocjavaserviceslowly;

import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by coichedid on 7/9/17.
 */
@Configuration
@ComponentScan("com.coichedid.pocjavaserviceslowly")
public class PocjavaserviceslowlyConfiguration {
    @Bean
    public AlwaysSampler defaultSampler() {
        return new AlwaysSampler();
    }
}

