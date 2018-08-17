# 采用alpine openjdk8 jre latest版本镜像  55m大小
FROM registry.cn-hangzhou.aliyuncs.com/jiuming/java-alpine-openjdk8-jre-shanghai
# author
MAINTAINER 'ming'
# 环境变量 默认dev
ENV MY_ENV=dev
# 容器内部使用root 权限
USER root
# 创建工作空间
WORKDIR /workspace
# 复制jar
COPY target/ming.jar /workspace
#暴露端口
EXPOSE 8080
# 运行  所有jar 使用8080启动
ENTRYPOINT java -jar /workspace/ming.jar --server.port=8080 --spring.cloud.config.profile=$MY_ENV
