# Fish-Go ([데모링크](http://211.217.160.129:3000 "fish-go"))

---
![애플리케이션 이미지](/readme/fish_go_screen_shot.png)

## 🧭개요
**FishGo**는 사용자들이 낚시 포인트를 공유하고, 조황 정보를 기록하며 소통할 수 있는 위치 기반 커뮤니티입니다.  
이 레포지토리는 **백엔드 REST API 서버**로, 클라이언트와 분리된 구조로 동작합니다.

- 📅 기간: 2025.02 ~ 2025.05
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

## 📃 ERD (Entity Relationship Diagram)
![ERD](/readme/erd.png)

---

## 📁 프로젝트 구조 (Project Structure)

```bash
fishgo/
├── src/
│   └── main/
│       ├── java/com/fishgo/
│       │   ├── badge/                # 첫 게시물 생성, 7일 연속 접속 등 뱃지 시스템 패키지
│       │   │   ├── controller/       # REST API 컨트롤러
│       │   │   ├── domain/           # JPA 엔티티
│       │   │   ├── dto/              # 요청/응답용 DTO 클래스
│       │   │   │   └── mapper/       # DTO와 Entity 변환용 클래스
│       │   │   ├── event/
│       │   │   ├── repository/       # Spring Data JPA 레포지토리 인터페이스
│       │   │   └── service/          # 비즈니스 로직 서비스 계층
│       │   ├── common/
│       │   │   ├── constants/        # ErrorCode, UploadPath 등 절대값을 가진 Enum
│       │   │   ├── exception/        # CustomException, GlobalExceptionHandler
│       │   │   ├── filter/           # Jwt 등 인증 관련 필터
│       │   │   ├── response/         # 데이터 반환 획일화를 위한 ResponstDTO
│       │   │   ├── util/             # 이미지 검증, 닉네임 랜덤생성 등 유틸리티 클래스
│       │   │   └── ...
│       │   ├── config/               # 보안, JWT 관련 설정 클래스
│       │   ├── posts/                # 게시글 CRUD 관련 패키지
│       │   │   ├── comments/         # 댓글 CRUD 관련 패키지
│       │   │   │   └── ...
│       │   │   └── ...
│       │   ├── users/                # 회원가입, 로그인, 프로필 수정 등 유저 관련 패키지
│       │   │   └── ...
│       └── resources/
│           └── application.properties   # 애플리케이션 설정 (DB, JWT 등)
```

---

## 🧱 향후 개선 사항

관리자 기능 추가

게시글 카테고리 추가

뱃지 종류 추가