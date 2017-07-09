package com.coichedid.pocjavaservice2.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Created by coichedid on 7/8/17.
 */
public class ServiceClient {
    private final RestTemplate restTemplate;

    private String gatewayHost;

    private String gateway;
    private String nextService;

    private Logger logger = LoggerFactory.getLogger("com.coichedid.pocjavaservice2.client.ServiceCliet");

    public ServiceClient(RestTemplate restTemplate, String service, String gatewayHost) {
        this.gatewayHost = gatewayHost;
        this.restTemplate = restTemplate;
        this.gateway = "http://" + this.gatewayHost;
        this.nextService = service;
    }

    public String sayHello(int hops) {
        //Get an client to next service with URL like
        //http://gateway:port/api/nextServiceUrl/hops
        String nextServiceUrl = this.gateway + this.nextService + hops;
        logger.info("Calling next service with " + nextServiceUrl);
        String response = this.restTemplate
                .getForObject(nextServiceUrl, String.class);
        return response;
    }
}
