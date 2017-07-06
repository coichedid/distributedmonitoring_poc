# Distributed Monitoring Proof of Concept
0. Create an ssh public key [SSH keys tutorial - Azure](https://docs.microsoft.com/en-us/azure/virtual-machines/linux/mac-create-ssh-keys)
1. Create a azure virtual machine **tracker** based on "Docker on Ubuntu Server" template [Docker Tutorial](http://keerats.com/blog/2016/setting-up-docker-on-mac-windows-linux-azure/)
2. Create a azure Ubuntu Linux standard virtual machine **service** to host services
3. Login to **tracker** with ssh
4. Run ElasticSearch container  
``` docker run -d -it --name es -p 9200:9200 -p 9300:9300 elasticsearch ```
5. Run Kibana container  
``` docker run -d -it --name kibana --link es:elasticsearch -p 5601:5601 kibana```
6. Run Logstach container  
``` docker run -d -it --name logstash -p 5000:5000 logstash -e 'input { tcp { port => 5000 codec => "json" } } output { elasticsearch { hosts => ["192.168.99.100"] index => "log-%{serviceName}"} }' ```
7. Run zipkin container configured to use elasticsearch as storage  
``` docker run -d -it -e STORAGE_TYPE='elasticsearch' -e ES_HOSTS='http://elasticsearch:9200' -e ES_INDEX='trace' --name zipkin --link es:elasticsearch -p 9411:9411 openzipkin/zipkin ```
> This docker will be connected to elasticsearch as its storage since we set environment variables STORAGE_TYPE and ES_HOSTS  
> Also we link elasticsearch container to ziplink container under hostname "elasticsearch"
8. Test everything
> Kibana url is http://<< **tracker** hostname>>:5601  
> Zipkin url is http://< **tracker** hostname>>:9411
9. Let's setup services and request simulators  
  1. Login to **service** with ssh
  2. Install jdk on it  
  ``` sudo apt-get install default-jdk
