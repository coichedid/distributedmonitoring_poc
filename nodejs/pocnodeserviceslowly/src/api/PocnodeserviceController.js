import nextService from '../utils/NextServiceRandomizer';
import config from 'config';
import ServiceClient from '../client/ServiceClient';

class PocnodeserviceController {
  constructor(server) {
    this.applicationConfig = config.get('application')
    this.server = server;
    if (!server) throw new Error('Undefined server provided');
    server.get('/node/service1/:hops',this.sayHello.bind(this));
    this.serviceName = this.applicationConfig.name;
    this.gatewayHost = config.get('gateway').host;
  }

  sayHello(req,res) {
    let params = req.params;
    req.log.info(req.logger.markTime({}),`${this.applicationConfig.name} sayHello`)
    if (!params.hops && params.hops != 0) { //Just a little hack as 0 is also false
      req.log.info(req.logger.markTime({}),`First hop, setting to zero`);
      params.hops = 0;
    }

    let nextServiceUrl = nextService(params.hops); //Drawing next hop
    let response = this.serviceName;

    if (nextServiceUrl) { //We have another hop service to visit, so let's go
      req.log.info(req.logger.markTime({}),`Going to next hop`);
      let serviceClient = new ServiceClient(nextServiceUrl,this.gatewayHost,req);
      req.log.info(req.logger.markTime({}),'Taking some time to sleep...');
      setTimeout(() => {
        req.log.info(req.logger.markTime({}),'Awaking');
        serviceClient.sayHello(params.hops + 1)
          .then( result => {
            response += `> ${result}`;
            return res.send(200,response);
          })
          .catch( reason => {
            req.log.error(req.logger.markTime({}),reason.message||reason);
            response += `> got error on ${nextServiceUrl}`;
            return res.send(500,response);
          });
      },)
    }
    else {
      req.log.info(req.logger.markTime({}),`Chain finished`);
      return res.send(200,response);
    }
  }
}

export default PocnodeserviceController;
