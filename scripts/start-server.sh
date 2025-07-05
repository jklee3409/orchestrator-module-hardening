#!/bin/bash

echo "--------------- 서버 배포 시작 -----------------"
sudo apt update
sudo apt install -y awscli
aws ecr get-login-password --region ap-northeast-2 | \
docker login --username AWS --password-stdin 221082211696.dkr.ecr.ap-northeast-2.amazonaws.com
sudo docker stop capstone-server || true
sudo docker rm capstone-server || true
sudo docker pull 221082211696.dkr.ecr.ap-northeast-2.amazonaws.com/capstone-server:latest
sudo docker run -d --name capstone-server -p 8080:8080 221082211696.dkr.ecr.ap-northeast-2.amazonaws.com/capstone-server:latest
echo "--------------- 서버 배포 끝 -----------------"