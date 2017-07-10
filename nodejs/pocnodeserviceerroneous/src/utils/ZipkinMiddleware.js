import {Tracer,ExplicitContext,BatchRecorder} from 'zipkin';
import {restifyMiddleware} from 'zipkin-instrumentation-restify';
import wrapRequestZipkin from 'zipkin-instrumentation-request';
import {HttpLogger} from 'zipkin-transport-http';
import config from 'config';

const zipkinConfig = config.get('logger').zipkin||{host:'127.0.0.1',port:9411};
const ctxImpl = new ExplicitContext();
const recorder = new BatchRecorder({
  logger: new HttpLogger({
    endpoint:`http://${zipkinConfig.host}:${zipkinConfig.port}/api/v1/spans`
  })
})
const tracer = new Tracer({
  ctxImpl,
  recorder
})

export default {
  middleware:(serviceName) => {
    return restifyMiddleware({tracer,serviceName:serviceName});
  },
  wrapRequest:(request, serviceName, nextServiceName) => {
    return wrapRequestZipkin(request,{tracer,serviceName,nextServiceName});
  }
}
