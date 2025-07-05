#!/bin/bash

echo "--------------- 서버 배포 시작 -----------------"
if ! command -v docker &> /dev/null; then
  echo "Docker not found. Installing Docker..."
  sudo apt update
  sudo apt install -y docker.io
  sudo systemctl start docker
  sudo systemctl enable docker
  sudo usermod -aG docker ubuntu
fi

# AWS CLI 설치
if ! command -v aws &> /dev/null; then
  echo "Installing AWS CLI..."
  curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "/tmp/awscliv2.zip"
  unzip -q /tmp/awscliv2.zip -d /tmp
  sudo /tmp/aws/install
fi

aws ecr get-login-password --region ap-northeast-2 | \
docker login --username AWS --password-stdin 221082211696.dkr.ecr.ap-northeast-2.amazonaws.com
sudo docker stop capstone-server || true
sudo docker rm capstone-server || true
sudo docker pull 221082211696.dkr.ecr.ap-northeast-2.amazonaws.com/capstone-server:latest
sudo docker run -d --name capstone-server -p 8080:8080 221082211696.dkr.ecr.ap-northeast-2.amazonaws.com/capstone-server:latest
echo "--------------- 서버 배포 끝 -----------------"