package com.coichedid.pocjavaservice1.utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Clovis coichedid
 * @description This static class is used to randomize
 * the next hop endpoint of request chain
 * If current hops length is greater then 4, it's
 * end request chain
 */
public class NextServiceRandomizer {
  //List of existing services endpoints
  private final static String[] serviceEndpoints = {
    ":2222/java/service1/",
    ":2223/java/service2/",
    ":2224/java/serviceerroneous/",
    ":2225/java/serviceslowly/",
    ":3333/node/service1/",
    ":3334/node/service2/",
    ":3335/node/serviceerroneous/",
    ";3336/node/serviceslowly/"
  };

  public static String getNextService(int hop) {
    if (hop > 4) return null; //That's enough, stop request chain

    //Randomize a number from 0 to the number of endpoints
    int randomNum = ThreadLocalRandom.current().nextInt(0, serviceEndpoints.length);
    return serviceEndpoints[randomNum];
  }
}
