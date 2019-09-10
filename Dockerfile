FROM duberlin/openjdk_aws
LABEL maintainer="devops@decimal.co.in"
      
EXPOSE 80 
ARG APP=/opt
ARG APP_HOME=/opt/deployment
ARG PROJECT_NAME="tpg-api-gateway"
ENV APP=${APP} \
    APP_HOME=${APP_HOME} \
    PROJECT_NAME=${PROJECT_NAME} \
    PROJECT_HOME=${APP_HOME}/${PROJECT_NAME}
ENV AWSCLI_VERSION "1.16.154"
RUN yum install -y -q python-pip; \
    yum clean all; \
    pip install awscli==$AWSCLI_VERSION

#RUN yum install -q -y unzip
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY
ARG AWS_REGION=ap-south-1
ARG build_name
echo build_name
RUN aws s3 cp s3://vconnect-builds/${PROJECT_NAME}/${build_name}.jar ${PROJECT_HOME}/
RUN ls -la ${PROJECT_HOME}/
ARG JAR=${build_name}.jar
ENV JAR=${JAR}
COPY start_server.sh ${PROJECT_HOME}/start_server.sh

ENTRYPOINT ["/opt/deployment/tpg-api-gateway/start_server.sh"]

