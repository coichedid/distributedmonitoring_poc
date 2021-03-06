package com.coichedid.pocjavaservice1.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coichedid.pocjavaservice1.utils.NextServiceRandomizer;
import com.coichedid.pocjavaservice1.client.ServiceClient;

/**
 * @author Clovis Chedid
 * @description This is the
 */
@RestController
class Pocjavaservice1Controller{
  private String serviceName = "pocjavaservice1";
  private final RestTemplate restTemplate;
  private Logger logger = LoggerFactory.getLogger("com.coichedid.pocjavaservice1.api.Pocjavaservice1Controller");

  @Autowired
  Pocjavaservice1Controller(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Value("${gateway.host}")
  private String gatewayHost;

  @RequestMapping("/java/service1/{hops}")
  public String sayHello(@PathVariable("hops") int hops) {
    logger.info("Saying hello");
    String nextServiceUrl = NextServiceRandomizer.getNextService(hops);

    String response = this.serviceName;
    if (nextServiceUrl != null) {
      logger.info("Calling new hop on " + nextServiceUrl);
      ServiceClient serviceClient = new ServiceClient(this.restTemplate,nextServiceUrl, this.gatewayHost);
      try{
        response += (" > ".concat(serviceClient.sayHello(hops + 1)));
      }
      catch (Exception e) {
        logger.error(e.getMessage());
        response += ("> got error on " + nextServiceUrl);
      }
    }
    else logger.warn("Ending chain");
    return response;
  }
}
