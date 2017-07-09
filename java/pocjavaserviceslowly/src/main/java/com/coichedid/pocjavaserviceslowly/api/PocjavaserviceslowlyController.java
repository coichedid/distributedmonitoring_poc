package com.coichedid.pocjavaserviceslowly.api;

import com.coichedid.pocjavaserviceslowly.client.ServiceClient;
import com.coichedid.pocjavaserviceslowly.utils.NextServiceRandomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Created by coichedid on 7/9/17.
 */
@RestController
public class PocjavaserviceslowlyController {
    private String serviceName = "pocjavaserviceslowly";
    private final RestTemplate restTemplate;
    private Logger logger = LoggerFactory.getLogger("com.coichedid.pocjavaserviceslowly.api.PocjavaserviceslowlyController");

    private final Tracer tracer; //Let's create a span to show slow method

    @Autowired
    PocjavaserviceslowlyController(RestTemplate restTemplate, Tracer tracer) {
        this.restTemplate = restTemplate;
        this.tracer = tracer;
    }

    @Value("${gateway.host}")
    private String gatewayHost;

    @RequestMapping("/java/serviceslowly/{hops}")
    public String sayHello(@PathVariable("hops") int hops) {
        logger.info("Saying hello");
        String nextServiceUrl = NextServiceRandomizer.getNextService(hops);

        String response = this.serviceName;
        doSamethingSlow();
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

    private void doSamethingSlow() {
        Span span = null;

        try {
            span = this.tracer.createSpan("Slow_thing_span");
            tracer.addTag("classification","strange_things");
            Thread.sleep(2000);
            span.logEvent("finish_slowly_thing");
        }
        catch (Exception e) {

        }
        finally {
            this.tracer.close(span);
        }
    }
}
