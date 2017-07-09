package com.coichedid.pocjavaserviceerroneous.api;

import com.coichedid.pocjavaserviceerroneous.utils.NextServiceRandomizer;
import com.coichedid.pocjavaserviceerroneous.client.ServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Created by coichedid on 7/9/17.
 */
@RestController
public class PocjavaserviceerroneousController {
    private String serviceName = "pocjavaserviceerroneous";
    private final RestTemplate restTemplate;
    private Logger logger = LoggerFactory.getLogger("com.coichedid.pocjavaserviceerroneous.api.PocjavaserviceerroneousController");

    @Autowired
    PocjavaserviceerroneousController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${gateway.host}")
    private String gatewayHost;

    @RequestMapping("/java/serviceerroneous/{hops}")
    public String sayHello(@PathVariable("hops") int hops) throws Exception {
        logger.info("Saying hello");
        String nextServiceUrl = NextServiceRandomizer.getNextService(hops);

        String response = this.serviceName;
        throw new Exception("Erroneous service called");
    }
}
