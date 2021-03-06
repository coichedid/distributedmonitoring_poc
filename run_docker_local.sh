#!/bin/sh
#echo "Loading elastic search container"
docker run -d -it --name es -p 9200:9200 -p 9300:9300 elasticsearch
#echo "Loading kibana container"
docker run -d -it --name kibana --link es:elasticsearch -p 5601:5601 kibana
#echo "Loading logstash container"
docker run -d -it --name logstash --link es:elasticsearch -p 5000:5000 logstash -e 'input { tcp { port => 5000 codec => "json" } } output { elasticsearch { hosts => ["elasticsearch"] index => "log-services-%{serviceName}"} }'
#echo "Loading zipkin container"
docker run -d -it -e STORAGE_TYPE='elasticsearch' -e ES_HOSTS='http://elasticsearch:9200' -e ES_INDEX='log-zipkin' --name zipkin --link es:elasticsearch -p 9411:9411 openzipkin/zipkin
echo "Loading javaservice1"
docker run -d -it -p 2222:2222 -h javaservice1 --name javaservice1 --link zipkin:zipkinservice --link logstash -t pocdistributedmonitoring/pocjavaservice1
echo "Loading javaservice2"
docker run -d -it -p 2223:2223 -h javaservice2 --name javaservice2 --link zipkin:zipkinservice --link logstash -t pocdistributedmonitoring/pocjavaservice2
echo "Loading javaserviceerroneous"
docker run -d -it -p 2224:2224 -h javaserviceerroneous --name javaserviceerroneous --link zipkin:zipkinservice --link logstash -t pocdistributedmonitoring/pocjavaserviceerroneous
echo "Loading javaserviceslowly"
docker run -d -it -p 2225:2225 -h javaserviceslowly --name javaserviceslowly --link zipkin:zipkinservice --link logstash -t pocdistributedmonitoring/pocjavaserviceslowly
echo "Loading nodeservice1"
docker run -d -it -p 3333:3333 -h nodeservice1 --name nodeservice1 --link zipkin:zipkinservice --link logstash -t pocdistributedmonitoring/pocnodeservice1
echo "Loading nodeservice2"
docker run -d -it -p 3334:3334 -h nodeservice2 --name nodeservice2 --link zipkin:zipkinservice --link logstash -t pocdistributedmonitoring/pocnodeservice2
echo "Loading nodeserviceerronous"
docker run -d -it -p 3335:3335 -h nodeserviceerroneous --name nodeserviceerroneous --link zipkin:zipkinservice --link logstash -t pocdistributedmonitoring/pocnodeserviceerroneous
echo "Loading nodeserviceslowly"
docker run -d -it -p 3336:3336 -h nodeserviceslowly --name nodeserviceslowly --link zipkin:zipkinservice --link logstash -t pocdistributedmonitoring/pocnodeserviceslowly
docker run -d -it --name request_gen --link logstash -t pocdistributedmonitoring/pocrequestgenerator


docker rm javaserviceslowly javaserviceerroneous javaservice2 javaservice1 kibana zipkin logstash es

docker start es
docker restart kibana
docker restart logstash
docker restart zipkin javaservice1 javaservice2 javaserviceerroneous javaserviceslowly
docker restart nodeservice1 nodeservice2 nodeserviceerroneous nodeserviceslowly
