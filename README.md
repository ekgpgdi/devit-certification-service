# Devit
<p align="center"> 
<img src = 'https://user-images.githubusercontent.com/84092014/177942862-e4755aa7-f87b-4eaa-8eae-07bcaeb3932e.png' style='width:300px;'/>
</p>
경험이 많고 실력 있는 개발자에게 도움을 받기 위한 플랫폼입니다. <br/>
기업 또는 개인에게 알맞는 개발자의 스펙과 원하는 직무를 등록하여 구인하고 개발자는 확인 후 지원서를 넣어 서로가 만족하는 상황이 되었을 때 계약이 진행될 수 있도록 중개하는 웹 사이트입니다. <br/>

## Devit Architecture
<img width="1005" alt="스크린샷 2022-07-25 오후 12 34 59" src="https://user-images.githubusercontent.com/84092014/180694026-b0c51181-5ddc-4e84-b659-2d32d33e05eb.png">

## Devit Certification Service
Devit 프로젝트 내 인증 서비스입니다. <br/>
로그인과 회원가입, 카카오 로그인, 토큰 갱신 기능을 담당하며 요청에 따라 사용자를 식별하고 토큰을 발급 및 갱신합니다. <br/>
회원가입이 진행되면 포인트 도메인과 회원 도메인에 RabbitMQ를 이용하여 메세지를 전달합니다.

JWT, Swagger, RabbitMQ, Eureka 를 활용하였습니다.

## API List
https://devit-spring.s3.ap-northeast-2.amazonaws.com/Swagger+UI.pdf
<img width="1173" alt="스크린샷 2022-07-08 오후 5 08 42" src="https://user-images.githubusercontent.com/84092014/177947346-a1816e49-da6e-47f0-a873-90a25d25e6e9.png">

## link to another repo
eureka server : https://github.com/ekgpgdi/devit-eureka-server  <br/>
gateway : https://github.com/ekgpgdi/devit-gateway <br/>
certification : https://github.com/ekgpgdi/devit-certification-service <br/>
board : https://github.com/kimziaco/devit-board <br/>
user : https://github.com/eet43/devit-user <br/>
chat : https://github.com/eet43/devit-chat <br/>
