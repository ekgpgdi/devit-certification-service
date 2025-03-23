# DevIT (개발자 구인 구직 플랫폼)의 Gateway 

### Devit Certification Service
Devit 프로젝트 내 인증 서비스입니다. <br/>
로그인과 회원가입, 카카오 로그인, 토큰 갱신 기능을 담당하며 요청에 따라 사용자를 식별하고 토큰을 발급 및 갱신합니다. <br/>
회원가입이 진행되면 포인트 도메인과 회원 도메인에 RabbitMQ를 이용하여 메세지를 전달합니다. <br/>

JWT, Swagger, RabbitMQ, Eureka 를 활용하였습니다. <br/>

<br/>

## 프로젝트 개요
DevIT은 **MSA(Microservices Architecture)** 기반으로 설계된 개발자 구인·구직 플랫폼입니다. 본 프로젝트는 부트캠프 내 개발팀을 꾸려 진행되었으며, 실무에서 활용 가능한 MSA 구조를 학습하고 적용하는 것을 목표로 했습니다.

### 🎯 프로젝트 목표
- **구인자와 구직자를 위한 포인트 결제 시스템 구현**
- **MSA 기반 서비스 설계 및 구축**
- **비동기 메시지 큐(RabbitMQ)를 활용한 안정적인 데이터 흐름 구축**
- **CI/CD 자동화 및 AWS 배포 환경 구성**
- **사용자 피드백을 반영한 지속적인 개선**

<br/>

## 🛠 기술 스택
### **Backend**
- Java 11, Spring Boot 2.7.1
- Spring Security, Spring Cloud Eureka
- RabbitMQ, MySQL
- JWT, Swagger

### **Frontend**
- HTML, JavaScript, Bootstrap

### **Infra & Deployment**
- AWS (EC2, RDS, S3)
- Github Actions (CI/CD)
- Docker, Nginx

<br/>

## 📅 프로젝트 기간 & 참여 인원
- **제작 기간**: 2022.06.24 ~ 2022.07.29
- **참여 인원**: 3명  
  [이다혜](https://github.com/ekgpgdi), [김지호](https://github.com/kimziaco), [김대희](https://github.com/eet43)

<br/>

## 🏗 프로젝트 구조
### **MSA 기반 서비스 분리 및 역할**
| 서비스  | 담당 개발자 | 저장소 |
|---------|-----------|--------|
| 유레카 서버 | 이다혜 | [devit-eureka-server](https://github.com/ekgpgdi/devit-eureka-server) |
| 게이트웨이 | 이다혜 | [devit-gateway](https://github.com/ekgpgdi/devit-gateway) |
| 인증 서비스 | 이다혜 | [devit-certification-service](https://github.com/ekgpgdi/devit-certification-service) |
| 결제 서비스 | 이다혜 | [devit-payment](https://github.com/ekgpgdi/devit-payment) |
| 게시물 서비스 | 김지호 | [devit-board](https://github.com/kimziaco/devit-board) |
| 유저 서비스 | 김대희 | [devit-user](https://github.com/eet43/devit-user) |
| 채팅 서비스 | 김대희 | [devit-chat](https://github.com/eet43/devit-chat) |
| 프론트엔드 | 전체 참여 | [devit-front](https://github.com/ekgpgdi/devit-front) |

<p align="center">
  <img src="https://github.com/user-attachments/assets/382bec55-02db-43d4-b68e-2b882ec5a1e5" width="70%">
</p>

## 🌟 핵심 기능
- **포인트 기반 결제 시스템** (구인자-구직자 간 거래)
- **JWT 기반 인증 및 보안 강화**
- **RabbitMQ를 활용한 비동기 메시징 시스템**
- **API 문서 자동화 (Swagger 적용)**
- **Spring Cloud Eureka를 통한 서비스 디스커버리 및 로드 밸런싱**

### 🖥 ERD 설계

<p align="center">
  <img src="https://github.com/user-attachments/assets/a01f6b32-1d9e-4aab-b50c-f0e29c2e6a2f" width="70%">
</p>

<br/>

## 🚀 트러블슈팅

### **1. 배포 후 간헐적인 CORS 에러 발생**
- **문제**: ELB의 타겟 그룹에 **Gateway 이외의 서비스가 등록됨** → 특정 요청 시 CORS 에러 발생
- **해결**: ELB 설정을 확인하여 **Gateway만 타겟 그룹에 등록하도록 수정**

### **2. 프론트 & 백엔드 도메인 불일치로 인한 쿠키 저장 문제**
- **문제**: 크롬 80 쿠키 정책으로 인해 프론트와 백엔드 도메인이 다르면 쿠키 저장 불가
- **해결**: 프론트와 백엔드 도메인을 `devit.shop`으로 통일하여 문제 해결

### **3. RabbitMQ 무한 롤백으로 인한 서버 마비**
- **문제**: 메시지 처리 중 예외 발생 시 **무한 재시도**로 인해 서버 과부하 발생
- **해결**: Dead Letter Queue(DLQ) 도입하여 **오류 메시지를 별도 큐로 분리**

<details><summary>고객 피드백 반영</summary>
 
[고객 피드백 확인](https://ddori-lee.tistory.com/entry/%EA%B3%A0%EA%B0%9D-%ED%94%BC%EB%93%9C%EB%B0%B1-%EB%B0%98%EC%98%81?category=1019915) 참고

1. http 요청에 대한 처리 <br/>
2. 회원가입 시 이메일 검증 추가 
3. 각 도메인의 자료형이 달라 생기는 문제 해결 
4. XSS 공격에 대한 대처 
5. 게시글 작성 시간과 현재 시간의 불일치 해결
6. 사진 크기에 따른 업로드 에러 해결
7. 메세지큐 무한 롤백으로 인한 서버 마비 현상 해결
</details>

<br/>

## 🎯 담당 역할 (이다혜)
✅ **MSA 기반 서비스 설계 및 구축**
- Spring Cloud Eureka Server 구축 및 서비스 디스커버리 구현
- Spring Cloud Gateway 적용 (요청 전달 및 인증 처리)

✅ **비동기 메시지 처리**
- RabbitMQ 설정 및 이벤트 기반 데이터 처리 설계

✅ **인증 & 결제 시스템 개발**
- `Certification Service`: JWT 기반 인증 및 보안 강화
- `Payment Service`: **포인트 기반 결제 시스템** 구현하여 구인자와 구직자 간 결제 흐름 제공

✅ **CI/CD 자동화 및 배포**
- Github Actions 활용한 **CI/CD 파이프라인 구축**
- AWS 환경 (EC2, RDS, S3)에서 배포 및 운영 관리
