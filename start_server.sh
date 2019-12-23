#!/usr/bin/env bash

/usr/bin/nohup java -jar /opt/deployment/${PROJECT_NAME}/${JAR} -Djavax.net.ssl.keyStore=/etc/pki/devvahanacloud.jks --spring.config.location=file:/usr/deployment/application.properties


#java -jar /opt/deployment/tpg-api-gateway/${JAR} 
