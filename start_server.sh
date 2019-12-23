#!/usr/bin/env bash

/usr/bin/nohup java -jar /opt/deployment/${PROJECT_NAME}/${JAR}
-Djavax.net.ssl.keyStore=/etc/pki/devvahanacloud.jks
-Djavax.net.ssl.keyStoreType=jks
-Djavax.net.debug=ssl # very verbose debug
-Djavax.net.ssl.keyStorePassword=admin@123

--spring.config.location=file:/usr/deployment/application.properties


#java -jar /opt/deployment/tpg-api-gateway/${JAR} 
