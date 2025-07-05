#!/bin/bash

echo "--------------- 서버 배포 시작 -----------------"
# AWS CLI 설치 (ubuntu 24.04에서는 apt로 설치 불가)
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip -q awscliv2.zip
sudo ./aws/install
aws ecr get-login-password --region ap-northeast-2 | \
docker login --username AWS --password-stdin 221082211696.dkr.ecr.ap-northeast-2.amazonaws.com
sudo docker stop capstone-server || true
sudo docker rm capstone-server || true
sudo docker pull 221082211696.dkr.ecr.ap-northeast-2.amazonaws.com/capstone-server:latest
sudo docker run -d --name capstone-server -p 8080:8080 221082211696.dkr.ecr.ap-northeast-2.amazonaws.com/capstone-server:latest
echo "--------------- 서버 배포 끝 -----------------"