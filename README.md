# 다챠 서비스 클라이언트 모듈

다챠(Datcha)는 개인 간 모바일 데이터를 거래할 수 있는 플랫폼입니다.
클라이언트 모듈은 사용자가 데이터를 사고 팔고, 경매에 참여하고, 자체 재화인 페이를 통해 안전하게 결제하는 **모바일 데이터 거래 서비스의 핵심 기능을 제공합니다.**

---

## 주요 기능 (Features)

다챠 클라이언트 모듈은 사용자 간 모바일 데이터 거래를 안전하고 효율적으로 지원하기 위한 다양한 기능을 제공합니다.
다음은 해당 모듈에서 제공하는 기능입니다.

---

### 📊 데이터 거래 시스템

개인 간 데이터 거래를 위한 핵심 기능입니다.

#### • 일반 거래

* 사용자가 **판매 글을 등록**하고, 구매자는 **지정된 가격에 데이터를 즉시 구매**할 수 있습니다.
* 거래 성사 시, 판매자에게 페이가 지급되고 구매자에게는 **데이터 충전권**이 발급됩니다.

#### • 입찰 거래 

* 구매자들은 페이로 입찰에 참여하며, 가장 높은 입찰가를 제시한 사용자가 거래의 낙찰자가 됩니다.
* 경매 종료 후 자동 낙찰 처리 및 충전권 발급이 이루어집니다.

---

### 페이(Pay) 결제 시스템

서비스 내 안전한 거래를 위한 자체 결제 시스템입니다.

#### • 페이 충전

* 사용자는 결제 시스템을 통해 페이를 충전할 수 있습니다.

#### • 페이 사용

* 데이터 구매 및 입찰 참여 시 페이가 사용됩니다.

* 판매자는 거래 완료 후 획득한 페이를 **현금으로 환전**하거나 **서비스 내에서 재사용**할 수 있습니다.

---

### 데이터 거래 통합 관리

거래에 사용되는 데이터를 안전하게 관리하기 위한 기능입니다.

#### • 보유 데이터 관리

* 사용자의 데이터 잔여량을 관리합니다.

#### • 판매 가능 데이터 설정

* 사용자는 보유한 데이터 중 일부를 **‘판매용 데이터’로 설정**해야 거래에 등록할 수 있습니다.

---

### 데이터 충전권

서비스 내에서 사용 가능한 충전권 형태로 데이터가 지급됩니다.

#### • 충전권 생성

* 구매가 완료되면, 해당 거래를 기반으로 **사용자에게 맞는 통신사용 데이터 충전권**이 자동 발급됩니다.

#### • 충전권 사용

* 사용자는 충전권을 통해 자신의 데이터 충전할 수 있습니다.


---

## 기술 스택 

| 기술                  | 아이콘                                                                                                                | 설명                         |
| ------------------- | ------------------------------------------------------------------------------------------------------------------ | -------------------------- |
| **Spring Boot**     | ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat\&logo=Spring-Boot\&logoColor=white)    | 전반적인 백엔드 애플리케이션 프레임워크      |
| **Spring Data JPA**             | ![JPA](https://img.shields.io/badge/JPA-59666C?style=flat\&logo=hibernate\&logoColor=white)                        | 객체-관계 매핑을 통한 데이터베이스 연동     |
| **Spring Security** | ![Spring Security](https://img.shields.io/badge/Security-6DB33F?style=flat\&logo=Spring-Security\&logoColor=white) | 인증/인가 및 보안 처리 프레임워크        |
| **JWT**             | ![JWT](https://img.shields.io/badge/JWT-000000?style=flat\&logo=jsonwebtokens\&logoColor=white)                    | 사용자 인증을 위한 토큰 기반 보안 방식     |
| **OAuth 2.0**       | ![OAuth](https://img.shields.io/badge/OAuth%202.0-1C1C1C?style=flat\&logo=oauth\&logoColor=white)                  | 소셜 로그인 및 외부 인증 연동 방식       |
| **Redis**           | ![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat\&logo=redis\&logoColor=white)                        | 세션 관리 및 토큰 저장용 인메모리 데이터베이스 |
| **Kafka**           | ![Kafka](https://img.shields.io/badge/Kafka-231F20?style=flat\&logo=apache-kafka\&logoColor=white)                 | 비동기 메시징 및 이벤트 기반 통신        |
| **AWS EC2**         | ![EC2](https://img.shields.io/badge/AWS%20EC2-FF9900?style=flat\&logo=amazon-aws\&logoColor=white)                 | 백엔드 서버 배포 환경               |
| **AWS RDS**         | ![RDS](https://img.shields.io/badge/AWS%20RDS-527FFF?style=flat\&logo=amazon-aws\&logoColor=white)                 | 관계형 데이터베이스 서비스             |
| **AWS (공통)**        | ![AWS](https://img.shields.io/badge/AWS-232F3E?style=flat\&logo=amazon-aws\&logoColor=white)                       | 클라우드 기반 인프라 운영             |

