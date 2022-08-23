# DevIT
> 개발자 구인 구직 서비스 <br/>
> https://devit.shop/
<br/>

## Devit Certification Service
Devit 프로젝트 내 인증 서비스입니다. <br/>
로그인과 회원가입, 카카오 로그인, 토큰 갱신 기능을 담당하며 요청에 따라 사용자를 식별하고 토큰을 발급 및 갱신합니다. <br/>
회원가입이 진행되면 포인트 도메인과 회원 도메인에 RabbitMQ를 이용하여 메세지를 전달합니다.

JWT, Swagger, RabbitMQ, Eureka 를 활용하였습니다.

<br/>

## 0. 총 코드 
담당 개발자 : 이다혜 <br/>
> 유레카 서버 : https://github.com/ekgpgdi/devit-eureka-server <br/>
> 게이트웨이 : https://github.com/ekgpgdi/devit-gateway <br/>
> 인증 서비스 : https://github.com/ekgpgdi/devit-certification-service <br/> 
> 결제 서비스 : https://github.com/ekgpgdi/devit-payment <br/>


담당 개발자 : 김지호 <br/>
> 게시물 서비스 : https://github.com/kimziaco/devit-board <br/>
 

담당 개발자 : 김대희 
> 유저 서비스 : https://github.com/eet43/devit-user <br/>
> 채팅 서비스 : https://github.com/eet43/devit-chat <br/>

<br/>

> 프론트 : https://github.com/ekgpgdi/devit-front

<br/>

## 1. 제작 기간 & 참여 인원
* 2022.06.24 ~ 2022.07.29
* 3명 : [이다혜](https://github.com/ekgpgdi), [김지호](https://github.com/kimziaco?tab=repositories), [김대희](https://github.com/eet43)
<br/>

## 2. 사용 기술 
<b>```Back-end```<b/>
* Java11
* Spring Boot 2.7.1
* Gradle 7.4.1
* Spring Data JPA
* H2
* MySQL 8.0.29
* Spring Security 
* JWT
* Spring Cloud Eureka
* RabbitMQ
* Swagger

<b>```Front-end```<b/>
* HTML
* JavaScript
* Bootstrap

<br/>

## 3. ERD 설계

<img width="639" alt="스크린샷 2022-08-09 오후 4 27 57" src="https://user-images.githubusercontent.com/84092014/183590368-612bee33-4c73-4739-a265-0365dc22a75c.png">

 <img width="378" alt="스크린샷 2022-08-09 오후 4 28 09" src="https://user-images.githubusercontent.com/84092014/183590427-832e23ce-0ac3-4670-82e3-ca471412a0e2.png">



<br/>
<br/>

## 4. 핵심 기능
이 서비스의 핵심 기능은 구인자와 구직자의 결제입니다. <br/>
현재는 결제를 경험해보기 위하여 포인트 형태로 구현해두었습니다. <br/>
 
<img width="667" alt="스크린샷 2022-08-22 오전 11 03 59" src="https://user-images.githubusercontent.com/84092014/185824583-83de98c4-f28e-4de5-82d3-b6eaf6454c34.png">



<br/>

## 5. 아키텍처도
<img width="699" alt="스크린샷 2022-08-22 오후 9 10 27" src="https://user-images.githubusercontent.com/84092014/186058191-a32c6744-0af4-47c0-8c8d-c67b00d6227d.png">
<img width="1000" alt="스크린샷 2022-08-22 오후 9 10 35" src="https://user-images.githubusercontent.com/84092014/186058188-1d5e1188-0777-47a2-9fe9-1c818fce11b4.png">


<br/>

## 6. 핵심 트러블 슈팅
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

