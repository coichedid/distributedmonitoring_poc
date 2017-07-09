# Distributed Monitoring Proof of Concept
0. Create an ssh public key [SSH keys tutorial - Azure](https://docs.microsoft.com/en-us/azure/virtual-machines/linux/mac-create-ssh-keys)
1. Create a azure virtual machine **tracker** based on "Docker on Ubuntu Server" template [Docker Tutorial](http://keerats.com/blog/2016/setting-up-docker-on-mac-windows-linux-azure/)
2. Create a azure Ubuntu Linux standard virtual machine **service** to host services
3. Login to **tracker** with ssh
4. Run ElasticSearch container  
``` docker run -d -it --name es -p 9200:9200 -p 9300:9300 elasticsearch ```
5. Run Kibana container (sense plugin was already installed on version 5.*)  
```  
docker run -d -it --name kibana --link es:elasticsearch -p 5601:5601 kibana  
```
6. Run Logstach container  
``` docker run -d -it --name logstash --link es:elasticsearch -p 5000:5000 logstash -e 'input { tcp { port => 5000 codec => "json" } } output { elasticsearch { hosts => ["elasticsearch"] index => "log-%{serviceName}"} }' ```
7. Run zipkin container configured to use elasticsearch as storage  
``` docker run -d -it -e STORAGE_TYPE='elasticsearch' -e ES_HOSTS='http://elasticsearch:9200' -e ES_INDEX='log' --name zipkin --link es:elasticsearch -p 9411:9411 openzipkin/zipkin ```
> This docker will be connected to elasticsearch as its storage since we set environment variables STORAGE_TYPE and ES_HOSTS  
> Also we link elasticsearch container to ziplink container under hostname "elasticsearch"  
8. Run zipkin-dependencies container configured to use elasticsearch as storage (Needs to be scheduled)  
```  
docker run -d -it  -e STORAGE_TYPE='elasticsearch' -e ES_HOSTS='http://elasticsearch:9200' -e ES_INDEX='log' --name zipkin-deps --link es:elasticsearch openzipkin/zipkin-dependencies
```
8. Test everything
> Kibana url is http://<< **tracker** hostname>>:5601  
> Zipkin url is http://< **tracker** hostname>>:9411
9. Let's setup services and request simulators  
    1. Login to **service** with ssh
    2. Install jdk  
``` sudo apt-get update  ```  
``` sudo apt-get install default-jdk ```  
    3. Install maven  
> Download Maven from [official website](https://maven.apache.org/download.cgi)  
> Follow instructions from [Installing Apache Maven](https://maven.apache.org/install.html)  
    4. Clone this project into some base folder  
> This project has 6 java projects:  
> 1. pocinfradiscovery: Simple Spring Cloud and Eureka based rest service to serve discovery services  
> 2. pocinfragateway: Simple Spring Cloud and Zuul based rest service to serve as gateway to all "business" services as a single endpoint to clients  
> 3. pocjavaservice1: Dummy Spring Boot based rest service  
> 4. pocjavaservice2: Dummy Spring Boot based rest service  
> 5. pocjavaserviceerroneous: Spring Boot based rest service that always result in an error response  
> 6. pocjavaserviceslowly: Spring Boot based rest service that always delay 2 seconds to respond  
> And 4 Node JS projects:  
> 1. pocnodeservice1: Dummy RestifyJS based rest service  
> 2. pocnodeservice2: Dummy RestifyJS based rest service  
> 3. pocnodeserviceerroneous: RestifyJS based rest service that always result in an error response  
> 4. pocnodeserviceslowly: RestifyJS bases rest service that always delay 2 seconds to respond  
> All services, java and NodeJS, behavior is something like this:  
> 1. Receive a request with Number of Hops since first call as its own path: "api/pocjavaservice1/<numhops>"  
> 2. If numhops is less then 4, this service randomizes next hop between rest endpoints (those 8 "business" services)  
> 3. Call next hop  
> 4. Concatenate response with own service name and respond.  
    7. Deploy project **pocinfradiscovery**  
> Edit logback.xml file of **pocinfradiscovery** project  
```  
cd DistributedMonitoring_POC  
cd java/pocinfradiscovery/src/main/resources  
```  
> Edit file logback.xml  
> Find block <appender name="STASH">  
> Find tag <destination>logstash:5000</destination>  
> Change logstash to hostname of **tracker** server  
> Save file   
    8. Edit application.properties  
> Edit file application.properties  
> Find line spring.application.zipkin.baseUrl=http://zipkinservice:9411 
> Change zipkinservice to hostname of **tracker** server  
> Save file  
```  
cd ../../../../../
```  
    5. Build **pocinfradiscovery** project
```  
cd java/pocinfradiscovery  
mvn package docker:build  
cd ../../
```  
    6. Run **pocinfradiscovery** container  
```  
docker run -d -it -p 8761:8761 --name discovery -t pocdistributedmonitoring/pocinfradiscovery  
```  
    7. Deploy project **pocjavaservice1**  
> Edit logback.xml file of **pocjavaservice1** project  
```  
cd java/pocjavaservice1/src/main/resources  
```  
> Edit file logback.xml  
> Find block <appender name="STASH">  
> Find tag <destination>logstash:5000</destination>  
> Change logstash to hostname of **tracker** server  
> Save file    
> Edit file application.properties  
> Find line spring.application.zipkin.baseUrl=http://zipkinservice:9411 
> Change zipkinservice to hostname of **tracker** server  
> Find linke gateway.host=172.17.0.1
> Change gateway host ip to **service** hostname 
> Save file  
```  
cd ../../../../../
```  
    7. Build **pocjavaservice1** project  
```  
cd java/pocjavaservice1  
mvn package docker:build  
cd ../../
```  
    8. Run **pocinfragateway** container  
> This needs to be linked to Discovery service as **discoveryservice** hostname  
> This needs to be linked to Gateway service as **gatewayservice** hostname   
```  
docker run -d -it -h javaservice1 -p 2222:2222 --name javaservice1 --link discovery:discoveryservice --link gateway:gatewayservice -t pocdistributedmonitoring/pocjavaservice1  
```  
    7. Deploy project **pocinfragateway**  
> Edit logback.xml file of **pocinfragateway** project  
```  
cd java/pocinfragateway/src/main/resources  
```  
> Edit file logback.xml  
> Find block <appender name="STASH">  
> Find tag <destination>logstash:5000</destination>  
> Change logstash to hostname of **tracker** server  
> Save file   
    8. Edit application.properties  
> Edit file application.properties  
> Find line spring.application.zipkin.baseUrl=http://zipkinservice:9411 
> Change zipkinservice to hostname of **tracker** server  
> Save file  
```  
cd ../../../../../
```  
    7. Build **pocinfragateway** project  
```  
cd java/pocinfragateway  
mvn package docker:build  
cd ../../
```  
    8. Run **pocinfragateway** container  
> This needs to be linked to Discovery service as **discoveryservice** hostname
> This needs to have an environment variable named **ZIPKIN** with zipkin service endpoint (this is running on **tracker** server)  
```  
docker run -d -it -p 8765:8765 --name gateway --link discovery:discoveryservice -t pocdistributedmonitoring/pocinfragateway  
```  
