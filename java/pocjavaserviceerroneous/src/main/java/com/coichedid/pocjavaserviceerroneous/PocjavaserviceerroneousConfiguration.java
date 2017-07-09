package com.coichedid.pocjavaserviceerroneous;

import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by coichedid on 7/9/17.
 */
@Configuration
@ComponentScan("com.coichedid.pocjavaserviceerroneous")
public class PocjavaserviceerroneousConfiguration {
    @Bean
    public AlwaysSampler defaultSampler() {
        return new AlwaysSampler();
    }
}
