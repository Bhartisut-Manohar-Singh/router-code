FROM openjdk:17
LABEL maintainer="devops@decimal.co.in"

RUN apt-get update && \
    apt-get install -y build-essential libssl-dev libcurl4-openssl-dev wget

# Download and install curl 8.4.0 with OpenSSL TLS backend
RUN wget https://curl.se/download/curl-8.4.0.tar.gz && \
    tar -xvf curl-8.4.0.tar.gz && \
    cd curl-8.4.0 && \
    ./configure --with-openssl && \
    make && \
    make install && \
    cd .. && \
    rm -rf curl-8.4.0* && \
    apt-get remove -y build-essential wget && \
    apt-get autoremove -y && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set the correct library path and update ldconfig cache
ENV LD_LIBRARY_PATH /usr/local/lib
RUN ldconfig
      
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

