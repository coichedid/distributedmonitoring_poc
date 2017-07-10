import Logger from './utils/Logger';
import nextService from './utils/NextServiceRandomizer';
import ServiceClient from './client/ServiceClient';
import config from 'config';

let applicationConfig = config.get('application');
let gatewayConfig = config.get('gateway');
let logger = new Logger(applicationConfig.name);
let client = new ServiceClient(gatewayConfig.host);

function genRequest() {
	let next = nextService();
	logger.log.info(logger.markTime({}),`Calling next service ${next}`);
	client.sayHello(next,0);
	setTimeout(genRequest,1000);
}

genRequest();
