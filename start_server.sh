#!/usr/bin/env bash

#hmod a+rx /opt/deployment/tpg-vconnect-config/${1}
/usr/bin/nohup java -jar /opt/deployment/${PROJECT_NAME}/${JAR} --spring.config.location=file:${PROJECT_HOME}/application.properties > console.log &
