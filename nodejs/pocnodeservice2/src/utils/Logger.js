import bunyan from 'bunyan';
import config from 'config';

class Logger {
  constructor(name) {
    this.application = name;
    let loggerConfig = config.get('logger');
    let loggerOptions = {
      name:name,
      streams:[]
    }
    loggerOptions.streams.push({
      level:'info',
      stream:process.stdout
    });
    if (loggerConfig.logstash) {
      loggerOptions.streams.push({
        level:'info',
        type:'raw',
        stream: require('bunyan-logstash-tcp').createStream({
          host:loggerConfig.logstash.host,
          port:loggerConfig.logstash.port
        })
      })
    }

    this.log = bunyan.createLogger(loggerOptions);
  }

  markTime(obj) {
    return Object.assign({},obj,{'@timestamp':(new Date()).toISOString()});
  }

  middleware(req,res,next) {
    let childPayload = this.reqSerializer(req);
    childPayload['serviceName'] = this.application;
    req.log = this.log.child(childPayload);
    next();
  }

  reqSerializer(req) {
    return {
      host:req.ip,
      'X-B3-TraceId':req.headers['X-B3-TraceId'],
      'X-B3-SpanId':req.headers['X-B3-SpanId'],
      'X-B3-ParentSpanId':req.headers['X-B3-ParentSpanId'],
    }
  }
}

export default Logger;
