import originalRequest from 'request-promise-native';
import config from 'config';

class ServiceClient {
  /**
   *
   * @param  {String} service     service to be connected
   * @param  {String} gatewayHost host of gateway
   * @return {ServiceClient}             new service client
   */
  constructor(service,gatewayHost,req) {
    this.applicationConfig = config.get('application')
    this.nextService = service;
    this.gatewayHost = gatewayHost;
    this.gateway = `http://${this.gatewayHost}`;
    this.req = req;

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
  sayHello(hops) {
    return new Promise( (resolve, reject) => {
      this.options.uri = `${this.gateway}${this.nextService}${hops}`;
      this.req.log.info(this.req.logger.markTime({}),`calling service at ${this.options.uri}`);
      let request = this.req.wrapRequest(originalRequest,this.applicationConfig.name,this.nextService);
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
import originalRequest from 'request-promise-native';
import config from 'config';

let applicationConfig = config.get('application');

class ServiceClient {
  /**
   *
   * @param  {String} service     service to be connected
   * @param  {String} gatewayHost host of gateway
   * @return {ServiceClient}             new service client
   */
  constructor(service,gatewayHost,req) {
    this.nextService = service;
    this.gatewayHost = gatewayHost;
    this.gateway = `http://${this.gatewayHost}`;
    this.req = req;

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
  sayHello(hops) {
    return new Promise( (resolve, reject) => {
      this.options.uri = `${this.gateway}${this.nextService}${hops}`;
      this.req.log.info(this.req.logger.markTime({}),`calling service at ${this.options.uri}`);
      let request = this.req.wrapRequest(originalRequest,applicationConfig.name,this.nextService);
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
