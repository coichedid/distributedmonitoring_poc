import request from 'request-promise-native';

class ServiceClient {
  /**
   *
   * @param  {String} service     service to be connected
   * @param  {String} gatewayHost host of gateway
   * @return {ServiceClient}             new service client
   */
  constructor(gatewayHost) {
    this.gatewayHost = gatewayHost;
    this.gateway = `http://${this.gatewayHost}`;

    this.options = {
      method:'GET', //hardcoded we only have "gettable" services for this PoC
      uri:'',
      json:true
    }
  }

  /**
   * Call api of nextService url
   * @param  {number} hops Current hops executed
   * @return {String}      recursive response of this hop and next;
   */
  sayHello(nextService, hops) {
    return new Promise( (resolve, reject) => {
      this.options.uri = `${this.gateway}${nextService}${hops}`;
      request(this.options)
        .then(result => {
          resolve(result);
        })
        .catch( reason => {
          reject(reason);
        })
    });
  }
}

export default ServiceClient;
