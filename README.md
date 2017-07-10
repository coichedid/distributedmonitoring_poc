# Distributed Monitoring Proof of Concept
This project intends to demonstrate how the following architecture reference could be used to monitor and explore log and trace data from different services in a SOA or Microservice Architecture.
Reference Architecture consists in a data ingestion stack, where data is provided by a group of data shippers components and, finally, data can be visualized with Data Viewer and Data Correlator componentes.
The next diagram ilustrates the targeta architecture.  
  
![Reference Architecture](https://github.com/coichedid/distributedmonitoring_poc/raw/master/images/Reference_Architecture.png "Reference Architecture")  

For this PoC, we demonstrate how services and microservices can send log and trace data directly to Data Ingestion Pipeline and how some IoC controllers can trace related requests to create a map of called services.  
The followin diagram demonstrates how this PoC was implemented.  
  
![PoC Architecture](https://raw.githubusercontent.com/coichedid/distributedmonitoring_poc/master/images/PoC_Architecture.png "Poc Architecture")  
  
All components of this implementation are containerized, so, we have one container for each element of solution. Also, for this demonstration, we used two standard virtual machines of Azure Services, as ilustrated above.  
We hava one machine, as docker host, for services, named **Services**.  
Also, we have another machine, as docker host too, for Data Ingection Pipeline stack, named **Tracker**.  
  
We used docker containers to simplify deployment of solution, but it can run on whatever linux machine with java and nodejs.  
  
Last comment before show instructions.  
For this demonstrations, I implementaded 2 equal groups of services, with Java and Spring Boot and with Nodejs and RestifyJS.  
Each group of services has:  
1. 2 "business domain" services  
2. 1 erroneous service, that responds an error for every request  
3. 1 slowly service, that input a 2 seconds delay for every request   
For each main request, requested service randomizes next hop and calls target random service. This chain goes forward until 4 hops or an error.  

## Deployment Instructions ##  
First, let's create 2 machines on Azure Services for this demonstration:  
1. Create an ssh public key [SSH keys tutorial - Azure](https://docs.microsoft.com/en-us/azure/virtual-machines/linux/mac-create-ssh-keys)  
1. Create a azure virtual machine "**tracker**" based on "Docker on Ubuntu Server" template [Docker Tutorial](http://keerats.com/blog/2016/setting-up-docker-on-mac-windows-linux-azure/)  
1. Create a azure virtual machine "**services**" based on "Docker on Ubuntu Server" template [Docker Tutorial](http://keerats.com/blog/2016/setting-up-docker-on-mac-windows-linux-azure/)  
  
### Deploying Data Ingestion Pipeline ###  
1. Login to **tracker** with ssh
1. Run ElasticSearch container  
``` docker run -d -it --name es -p 9200:9200 -p 9300:9300 elasticsearch ```  
1. Run Kibana container (sense plugin was already installed on version 5.*)  
``` docker run -d -it --name kibana --link es:elasticsearch -p 5601:5601 kibana```  
1. Run Logstach container  
``` docker run -d -it --name logstash --link es:elasticsearch -p 5000:5000 logstash -e 'input { tcp { port => 5000 codec => "json" } } output { elasticsearch { hosts => ["elasticsearch"] index => "log-%{serviceName}"} }' ```  
1. Run zipkin container configured to use elasticsearch as storage  
``` docker run -d -it -e STORAGE_TYPE='elasticsearch' -e ES_HOSTS='http://elasticsearch:9200' -e ES_INDEX='log' --name zipkin --link es:elasticsearch -p 9411:9411 openzipkin/zipkin ```  
> This docker will be connected to elasticsearch as its storage since we set environment variables STORAGE_TYPE and ES_HOSTS  
> Also we link elasticsearch container to ziplink container under hostname "elasticsearch"  
6. On azure, create endpoints for ports 5601, 5000 and 9411 for machine **tracker** (this will allow us to access kibana, logstash and zipkin respectively)  
6. Test everything
    * Kibana url is http://<< **tracker** hostname>>:5601  
    * Zipkin url is http://< **tracker** hostname>>:9411  
6. We need to setup logs-* indexes to allow us to refresh index mapping on new data
    1. Enter Kibana site (http://<< **tracker** hostname>>:5601)
    1. Got to Dev Tools item on left menu (this is a console for commands to elasticsearch db)
    1. On console, enter the following command on the letf panel
    1. Run the command clicking on green arrow near to command
```  
PUT _template/template_logs
{
  "template": "logs-*",
  "settings": {
    "index.mapper.dynamic":true
  }
}

```  
> Data Ingestion Pipeline stack is up and running, let's go to services.
  
### Deploying services ###
1. Login to **services** with ssh
1. Install Java jdk
``` sudo apt-get update  ```  
``` sudo apt-get install default-jdk ```  
3. Install maven  
> Download Maven from [official website](https://maven.apache.org/download.cgi)  
> Follow instructions from [Installing Apache Maven](https://maven.apache.org/install.html)  
4. Install NodeJS  
``` sudo apt-get update  ```  
``` sudo apt-get install nodejs npm ```  
5. Clone this project into some base folder  
``` cd ~/ ```  
``` mkdir PoC;cd PoC ```  
``` git clone https://github.com/coichedid/distributedmonitoring_poc.git ```  
``` cd distributedmonitoring_poc ```  
> * This project has 6 java projects:  
>   * pocinfradiscovery: This is a discovery service for future implementations  
>   * pocinfragateway: This is a gateway service for future implementations  
>   * pocjavaservice1: Dummy Spring Boot based rest service  
>   * pocjavaservice2: Dummy Spring Boot based rest service  
>   * pocjavaserviceerroneous: Spring Boot based rest service that always result in an error response  
>   * pocjavaserviceslowly: Spring Boot based rest service that always delay 2 seconds to respond  
> * 4 Node JS service projects:  
>   * pocnodeservice1: Dummy RestifyJS based rest service  
>   * pocnodeservice2: Dummy RestifyJS based rest service  
>   * pocnodeserviceerroneous: RestifyJS based rest service that always result in an error response  
>   * pocnodeserviceslowly: RestifyJS bases rest service that always delay 2 seconds to respond  
> * 1 application in NodeJS for request simulation in every minute  
>  
> All services, java and NodeJS, behavior is something like this:  
> 1. Receive a request with Number of Hops since first call as its own path: "api/pocjavaservice1/<numhops>"  
> 2. If numhops is less then 4, this service randomizes next hop between rest endpoints (those 8 "business" services)  
> 3. Call next hop  
> 4. Concatenate response with own service name and respond.  
6. Build all service java projects  
> First enter java folder  
> ``` cd java ```  
> For each [pocjavaservice1, pocjavaservice2, pocjavaserviceerroneous, pocjavaserviceslowly] execute these commands:  
> * Run command ``` cd <service project: like pocjavaservice1> ```  
> * Edit file src/main/java/resources/logback.xml.  
>   * Find logstash:5000 and replace with **tracker** hostname:5000  
> * Edit file src/main/java/resources/application.properties
>   * Find http://zipkinservice:9411 and replace with **tracker** hostname:9411   
> * Run command ``` mvn package docker:build ```  
> * Run command ``` cd .. ```  
7. Build all service NodeJS projects  
> First enter node folder  
> ``` cd nodejs ```  
> For each [pocnodeservice1, pocnodeservice2, pocnodeserviceerroneous, pocnodeserviceslowly, pocRequestGenerator]  
> * Run command ``` cd <service project: like pocnodeservice1> ```  
> * Run command ``` npm install ```
> * Edit file config/default.json
>   * Find "172.17.0.2" and replace with **services** hostname  
>   * Find "host":"logstash" and replace "logstash" with **tracker** hostname  
>   * Find "host":"zipkinservice" and replace "zipkinservice" with **tracker** hostname  (In pocRequestGenerator project, this string is not present. No problems!)  
> * Run command ``` npm run build ```  
  
At this time, every service is build and its own container is done.  
It's time to deploy it one  
  
8. Run all service containers  
    1. Deploy pocjavaservice1:  
    ``` docker run -d -it -p 2222:2222 -h javaservice1 --name javaservice1 -t pocdistributedmonitoring/pocjavaservice1 ```  
    2. Deploy pocjavaservice2  
    ``` docker run -d -it -p 2223:2223 -h javaservice2 --name javaservice2 -t pocdistributedmonitoring/pocjavaservice2 ```  
    3. Deploy pocjavaserviceerroneous  
    ``` docker run -d -it -p 2224:2224 -h javaserviceerroneous --name javaserviceerroneous -t pocdistributedmonitoring/pocjavaserviceerroneous ```  
    4. Deploy pocjavaserviceslowly  
    ``` docker run -d -it -p 2225:2225 -h javaserviceslowly --name javaserviceslowly -t pocdistributedmonitoring/pocjavaserviceslowly ```  
    5. Deploy pocnodeservice1  
    ``` docker run -d -it -p 3333:3333 -h nodeservice1 --name nodeservice1 -t pocdistributedmonitoring/pocnodeservice1 ```  
    6. Deploy pocnodeservice2  
    ``` docker run -d -it -p 3334:3334 -h nodeservice2 --name nodeservice2 -t pocdistributedmonitoring/pocnodeservice2 ```  
    7. Deploy pocnodeserviceerroneous  
    ``` docker run -d -it -p 3335:3335 -h nodeserviceerroneous --name nodeserviceerroneous -t pocdistributedmonitoring/pocnodeserviceerroneous ```  
    8. Deploy pocnodeserviceslowly  
    ``` docker run -d -it -p 3336:3336 -h nodeserviceslowly --name nodeserviceslowly -t pocdistributedmonitoring/pocnodeserviceslowly ```  
9. On azure, create endpoints for ports 2222, 2223, 2224, 2225, 3333, 3334, 3335 and 3336 for machine **services** (this will allow those services to access each other)  
> If everything works fine, we can start to request random services to simulate normal trafic and collect some trace and log data. To do so, we need to start request simulation application  
10. Run requestGenerator application container on machine **services**  
``` docker run -d -it --name request_gen -t pocdistributedmonitoring/pocrequestgenerator ```  
> Now we are running our request simulator and in every second a random request is send to one of the services  
> To stop simulation, stop requestGenerator container on machine **services**  
``` docker stop request_gen ```  
> If we visit Kibana on **tracker** machine (http://<< **tracker** hostname>>:5601), we need to setup our index configuration:  
1. Go to Kinaba Management (last item on the left menu)  
1. Click on Indexes (first button)  
1. On textbox with "logstash-*" string, replace with "log-" string  
1. Select "Refresh index with search" checkbox  
1. Click on Create button ate the end of this page  
1. Back to Discovery (first item on left menu)  
> Try to do some filters with this data  
> Now let's visit Zipkin on **tracker** machine (http://< **tracker** hostname>>:9411), there we can see request map.  
> This map shows correlations between requests, errors and request durations  
> There is a link on top of zipkin page, named "Dependencies". To use this feature, we need to aggregate requests in a separated process.  
> To do so, we need to login to **tracker** machine and run a different container. It will connect to elasticsearch and do some aggregations for zipkin.  
``` docker run -d -it  -e STORAGE_TYPE='elasticsearch' -e ES_HOSTS='http://elasticsearch:9200' -e ES_INDEX='log' --name zipkin-deps --link es:elasticsearch openzipkin/zipkin-dependencies ```  
> This process needs to be run everyday, so we can have aggregations on zipkin site. For this demonstration we will run this container everytime we need.  
> Back to zipkin site and click on "Dependencies". There we can see all dependencies found by zipkin on trace data.
>  
> To finish all containers and end this demonstration, login to both machines **tracker** and **services** and run:  
``` docker stop $(docker ps -aq)  ```  
``` docker rm $(docker ps -aq) ``` 
> Remember to destroy azure machines!