# 다챠 서비스 클라이언트 모듈

**다챠(Datcha)** 는 개인 간 모바일 데이터를 안전하게 사고팔 수 있는 플랫폼입니다.  
클라이언트 모듈은 데이터 거래, 경매, 자체 결제(Pay) 기능을 포함해 모바일 데이터 거래의 전 과정을 지원합니다.

---

## 기능 개요

### 1. 데이터 거래
- **일반 거래**: 판매자가 지정한 가격에 즉시 구매 가능  
- **입찰 거래**: Redis Lua Script를 활용한 동시성 제어 기반 경매 기능 제공  
  - 최고 입찰자가 자동 낙찰  
  - 거래 완료 시 충전권 발급

| 거래 방식 | 설명 | 결제 방식 |
|-----------|------|-----------|
| 일반 거래 | 판매자가 설정한 가격에 즉시 거래 | Pay |
| 입찰 거래 | 최고가 입찰자가 낙찰 | Pay |

---

### 2. Pay 결제
- **충전**: 결제 시스템을 통해 Pay 충전
- **사용**: 데이터 구매 및 입찰 참여 시 Pay 차감
- **환전**: 거래 완료 후 획득한 Pay를 현금 환전 또는 서비스 내 재사용 가능

---

### 3. 데이터 관리
- **보유 데이터 관리**: 사용자 데이터 잔여량 조회/관리
- **판매용 데이터 설정**: 보유 데이터 중 일부를 거래 가능 상태로 지정

---

### 4. 데이터 충전권
- 거래 완료 시 사용자 통신사에 맞는 **데이터 충전권** 자동 발급
- 발급된 충전권을 통해 서비스 내 데이터 충전 가능

---

### 5. 검색 및 필터
- **Elasticsearch** 기반의 거래글 검색 및 조건별 필터링 지원
- 가격, 용량, 거래 방식 등 다양한 조건으로 검색 가능

---

## 프로젝트 구조
<details>
<summary>프로젝트 구조 보기</summary>

``` text
orchestrator-module/
├── src/
│ ├── main/
│ │ ├── java/eureca/capstone/project/orchestrator/
│ │ │ ├── alarm/ # 실시간 알림 관련 기능
│ │ │ ├── auth/ # 인증/인가 관련 기능
│ │ │ ├── common/ # 공통 기능, 유틸리티, 예외 처리
│ │ │ ├── event/ # 이벤트 퀴즈 관련 기능 
│ │ │ ├── market_statistics/ # 데이터 시세/통계 기능
│ │ │ ├── pay/ # 결제 관련 기능 (토스 페이먼츠)
│ │ │ ├── transaction_feed/ # 거래 내역 관련 기능
│ │ │ └── user/ # 사용자 관리 기능
│ │ └── resources/
│ │ ├── elasticsearch/ # Elasticsearch 설정 및 매핑
│ │ └── scripts/ # Lua 스크립트
│ └── test/
│ └── java/eureca/capstone/project/orchestrator/ # 단위/통합 테스트
```
</details> 

---

## 시스템 아키텍처
> 서비스 전체 구조 및 데이터 흐름 다이어그램  
<img width="2276" height="1250" alt="Image" src="https://github.com/user-attachments/assets/71ca666b-249a-4690-bb9d-d6f1e418d656" />

---

## 알림 시스템 구조
> Kafka + Redis Pub/Sub + SSE 기반 실시간 알림 아키텍처
<img width="2276" height="1181" alt="Image" src="https://github.com/user-attachments/assets/5fa7f892-eda9-42d6-bc32-ed2d7ec35397" />

---

## 입찰 시스템 구조
> Redis Lua Script 기반 입찰 동시성 제어 아키텍처
<img width="2290" height="1206" alt="Image" src="https://github.com/user-attachments/assets/0126d321-544e-4bf8-a765-820b0efe0a36" />

---

## 성능 개선 사례

| 개선 항목 | 이전 성능 | 개선 성능 | 개선 효과 |
|-----------|-----------|-----------|-----------|
| 입찰 처리 TPS | 14.35 req/sec | 91.58 req/sec | **6.4배 증가** |
| 입찰 평균 응답시간 | 32s | 0.09s | 99% 이상 성능 단축 |
| 입찰 95% 응답시간 | 60s | 0.13s | 99% 이상 성능 단축 |

- **Redis Lua Script**로 입찰 동시성 제어 최적화
- **Jmeter**로 성능 부하 테스트 진행 (1000명의 가상 사용자)

---

## 트러블슈팅 사례
- **SSE 알림 유실 문제**: Redis Pub/Sub 을 통한 알림 전파, Nginx 타임아웃 해결을 위한 하트비트 구축, 버퍼 설정 조정
- **입찰 중복 처리**: DB 비관적 락 → Lua Script 처리로 해결

---

## 기술 스택

| 기술 | 아이콘 | 설명 |
|------|--------|------|
| **Spring Boot** | ![Spring Boot] | 백엔드 애플리케이션 프레임워크 |
| **Spring Data JPA** | ![JPA] | ORM 기반 데이터베이스 연동 |
| **Spring Security** | ![Spring Security] | 인증/인가 및 보안 처리 |
| **JWT** | ![JWT] | 토큰 기반 인증 방식 |
| **OAuth 2.0** | ![OAuth] | 소셜 로그인 및 외부 인증 연동 |
| **Redis** | ![Redis] | 세션 관리, 토큰 저장, 경매 동시성 제어 (Lua Script) |
| **Kafka** | ![Kafka] | 비동기 메시징 및 이벤트 기반 처리 |
| **Elasticsearch** | ![Elasticsearch] | 거래글 검색 및 필터 기능 |
| **AWS EC2** | ![EC2] | 서버 배포 환경 |
| **AWS RDS** | ![RDS] | 관계형 데이터베이스 |
| **AWS S3** | ![S3] | 정적 파일 저장 |
| **Grafana** | ![Grafana] | 로그 모니터링 |
| **Jmeter** | ![Jmeter] | 시스템 성능 부하 테스트 |

[Spring Boot]: https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=Spring-Boot&logoColor=white 
[JPA]: https://img.shields.io/badge/JPA-59666C?style=flat&logo=hibernate&logoColor=white 
[Spring Security]: https://img.shields.io/badge/Security-6DB33F?style=flat&logo=Spring-Security&logoColor=white 
[JWT]: https://img.shields.io/badge/JWT-000000?style=flat&logo=jsonwebtokens&logoColor=white 
[OAuth]: https://img.shields.io/badge/OAuth%202.0-1C1C1C?style=flat&logo=oauth&logoColor=white 
[Redis]: https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white 
[Kafka]: https://img.shields.io/badge/Kafka-231F20?style=flat&logo=apache-kafka&logoColor=white 
[Elasticsearch]: https://img.shields.io/badge/Elasticsearch-005571?style=flat&logo=elasticsearch&logoColor=white 
[EC2]: https://img.shields.io/badge/AWS%20EC2-FF9900?style=flat&logo=amazon-aws&logoColor=white 
[RDS]: https://img.shields.io/badge/AWS%20RDS-527FFF?style=flat&logo=amazon-aws&logoColor=white 
[S3]: https://img.shields.io/badge/AWS%20S3-569A31?style=flat&logo=amazon-s3&logoColor=white 
[Grafana]: https://img.shields.io/badge/-Grafana-5f5f5f?style=flat&logo=grafana&labelColor=ffffff
[JMeter]: https://img.shields.io/badge/JMeter-brightgreen?logo=apachejmeter&logoColor=white

