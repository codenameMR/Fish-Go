# Fish-Go ([데모링크](http://211.217.160.129:3000 "fish-go"))

---
## 🧭개요
**FishGo**는 사용자들이 낚시 포인트를 공유하고, 조황 정보를 기록하며 소통할 수 있는 위치 기반 커뮤니티입니다.  
이 레포지토리는 **백엔드 REST API 서버**로, 클라이언트와 분리된 구조로 동작합니다.

- 📅 기간: 2024.02 ~ 2024.05
- 👥 팀 인원: 3명 (프론트엔드 1, 백엔드 2)
- 🧩 주요 역할: API 설계 및 구현, DB 설계, Spring Security 인증 구현

---

## 🧪 주요 기능 (Features)
- 🧑 회원가입 / 로그인 / JWT 인증
- 📝 조황 기록 (Catch Report) 등록, 조회
- 🔒 사용자별 접근 제어 및 인증 처리
- 📦 RESTful API 설계 및 예외 처리

---

## 🔧 기술 스택 (Tech Stack)
| 영역 | 기술                           |
|------|------------------------------|
| 언어 | Java 21                      |
| 프레임워크 | Spring Boot, Spring Security |
| ORM | JPA (Hibernate)              |
| 데이터베이스 | PostgreSQL                   |
| 인증 | JWT (JSON Web Token)         |
| API 문서화 | Swagger ([링크](http://211.217.160.129:7777/swagger-ui/index.html))             |
| 형상 관리 | Git, GitHub                  |

---

## 🧱 향후 개선 사항

관리자 기능 추가

게시글 카테고리 추가

뱃지 종류 추가