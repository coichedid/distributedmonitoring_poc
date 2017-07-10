import * as _ from 'lodash';

let serviceEndpoints = [
  ':2222/java/service1/',
  ':2223/java/service2/',
  ':2224/java/serviceerroneous/',
  ':2225/java/serviceslowly/',
  ':3333/node/service1/',
  ':3334/node/service2/',
  ':3335/node/serviceerroneous/',
  ':3336/node/serviceslowly/'
];

/**
 * This function peeks a random endpoit from serviceEndpoints list and return
 * It hops are above 4 (max number of hops) it returns null (break chain)
 * @param  {number} hop Current hop cycle
 * @return {String}     Next endpoit or null to break chain
 */
function getNextService(hop) {
  if (hop > 4) return null; //That's enough, stop request chain

  //Randomize a number from zero to endpoint's list length
  let randNext = _.random(serviceEndpoints.length - 1);
  return serviceEndpoints[randNext];
}

export default getNextService;
