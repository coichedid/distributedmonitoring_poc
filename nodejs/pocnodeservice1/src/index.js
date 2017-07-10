import restify from 'restify';
import config from 'config';

import { version } from '../package.json';
import PocnodeserviceController from './api/PocnodeserviceController';
import Logger from './utils/Logger';
import ZipkinMiddleware from './utils/ZipkinMiddleware';

let applicationConfig = config.get('application');
let logger = new Logger(applicationConfig.name);

let serverPort = applicationConfig.serverPort;
let server = restify.createServer({
	name:applicationConfig.serverName,
	version: version
});
// Ensure we don't drop data on uploads
// server.pre(restify.pre.pause());
// Clean up sloppy paths like //todo//////1//
server.pre(restify.pre.sanitizePath());
// Handles annoying user agents (curl)
server.pre(restify.pre.userAgentConnection());
// Set a per request bunyan logger (with requestid filled in)
// server.use(restify.requestLogger());
// Allow 5 requests/second by IP, and burst to 10
server.use(restify.throttle({
    burst: 10,
    rate: 5,
    ip: true,
}));
// Use the common stuff you probably want
server.use(restify.acceptParser(server.acceptable));
server.use(restify.dateParser());
server.use(restify.queryParser());
server.use(restify.gzipResponse());
server.use(restify.bodyParser({
    mapParams: true
})); // Allows for JSON mapping to REST
server.use((req,res,next) => {
	req.logger = logger;
	req.wrapRequest = ZipkinMiddleware.wrapRequest;
	next();
})
server.use(ZipkinMiddleware.middleware(applicationConfig.name));
server.use(logger.middleware.bind(logger));

// Let's start using Passport.js
let controller = new PocnodeserviceController(server);
server.listen(serverPort, function() {
  logger.log.info(logger.markTime({serviceName:applicationConfig.name}),`${applicationConfig.name} ${server.name} server started at ${server.url}`);
});
