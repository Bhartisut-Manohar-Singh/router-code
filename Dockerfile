FROM openjdk:17
LABEL maintainer="devops@decimal.co.in"
      
EXPOSE 80 
ARG JOB_NAME
ARG APP=/opt
ARG APP_HOME=/opt/deployment
ARG PROJECT_NAME=${JOB_NAME}
ENV APP=${APP} \
    APP_HOME=${APP_HOME} \
    PROJECT_NAME=${PROJECT_NAME} \
    PROJECT_HOME=${APP_HOME}/${PROJECT_NAME}

ARG build_name

COPY ${JOB_NAME}/target/${build_name}.jar ${PROJECT_HOME}/
RUN ls -la ${PROJECT_HOME}/
ARG JAR=${build_name}.jar
ENV JAR=${JAR}
COPY start_server.sh ${PROJECT_HOME}/start_server.sh
RUN chmod +x ${PROJECT_HOME}/start_server.sh

RUN unlink /etc/localtime
RUN ln -s /usr/share/zoneinfo/Asia/Kolkata /etc/localtime

ENTRYPOINT ["/opt/deployment/tpg-api-gateway/start_server.sh"]

