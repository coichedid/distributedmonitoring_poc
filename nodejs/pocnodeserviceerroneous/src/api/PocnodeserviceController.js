import nextService from '../utils/NextServiceRandomizer';
import config from 'config';
import ServiceClient from '../client/ServiceClient';

class PocnodeserviceController {
  constructor(server) {
    this.applicationConfig = config.get('application')
    this.server = server;
    if (!server) throw new Error('Undefined server provided');
    server.get('/node/serviceerroneous/:hops',this.sayHello.bind(this));
    this.serviceName = this.applicationConfig.name;
    this.gatewayHost = config.get('gateway').host;
  }

  sayHello(req,res) {
    req.log.info(req.logger.markTime({}),`${this.applicationConfig.name} sayHello`)
    req.log.error(req.logger.markTime({}),"I'm an erroneous service, return an error");
    return res.send(500,new Error('Erroneous service'));
  }
}

export default PocnodeserviceController;
