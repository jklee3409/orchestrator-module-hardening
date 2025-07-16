# 다챠 서비스 클라이언트 모듈

다챠(Datcha)는 개인 간 모바일 데이터를 거래할 수 있는 플랫폼입니다.
클라이언트 모듈은 사용자가 데이터를 사고 팔고, 경매에 참여하고, 자체 재화인 페이를 통해 안전하게 결제하는 **모바일 데이터 거래 서비스의 핵심 기능을 제공합니다.**

---

## 주요 기능 (Features)

이 모듈은 다음과 같은 기능들을 제공합니다.

### **데이터 거래 시스템**

* **일반 거래**: 사용자가 판매글을 등록하고, 구매자는 지정된 가격으로 데이터를 구매합니다.
* **입찰 거래**: 판매자가 경매 방식으로 데이터를 등록하고, 구매자들이 입찰에 참여하여 거래가 성사됩니다.

### **페이(Pay) 결제 시스템**

* **페이 충전/사용/환전**: 현금 대신 자체 재화인 페이를 통해 안전하게 결제하고 환전을 처리합니다.

### **데이터 거래 통합 관리**

* **보유 데이터 관리**: 사용자의 데이터 잔여량과 판매 가능 데이터를 구분하여 관리합니다.
* **판매 데이터 등록/철회**: 보유한 데이터를 판매용으로 전환하여 판매할 수 있습니다.

### **데이터 쿠폰 발행**

* **충전권 생성 및 사용**: 구매한 데이터는 사용할 수 있는 ‘데이터 충전권’ 형태로 제공되며, 사용자는 이를 통해 손쉽게 데이터를 충전할 수 있습니다.

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

