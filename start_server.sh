#!/usr/bin/env bash

/usr/bin/nohup java -jar /opt/deployment/${PROJECT_NAME}/${JAR} --spring.config.location=file:/app/application.properties 

#java -jar /opt/deployment/tpg-api-gateway/${JAR} 
