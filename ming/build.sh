#!/bin/bash

dd = $(date +%Y%m%d)
sudo mvn -DskipTests install
dockerVersion = registry.cn-hangzhou.aliyuncs.com/jiuming/ming:${dd}
sudo docker build -t ${dockerVersion}
sudo docker push ${dockerVersion}

