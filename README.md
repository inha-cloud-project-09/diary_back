# 🎉 diary_back

`diary_back`은(는) **감정 일기 저장 및 분석**을 위한 백엔드 서버입니다.  
spring boot, Express, 데이터베이스(MySQL / PostgreSQL / MongoDB 등), JWT 인증 등을 활용한 RESTful API 구조를 가지고 있습니다.

---

## 📦 기능 요약
- **사용자 인증**
  - 회원가입 / 로그인 (JWT 기반)
- **일기 CRUD**
  - 일기 작성, 수정, 조회, 삭제
  - 일기에 감정 태그 자동/수동 부여
- **감정 분석**
  - 저장된 일기로부터 텍스트 감정 분석 및 통계 제공
- **일기 검색 및 필터링**
  - 날짜, 감정 태그, 키워드 기반 조회

---

## ⚙️ 기술 스택
- **언어**: Java
- **프레임워크**: spring boot
- **데이터베이스**: MySQL / PostgreSQL / MongoDB (선택)
- **인증 방식**: JWT
- **감정 분석**: (예: TensorFlow, Hugging Face, 간단 키워드 기반 등)
- **배포 플랫폼**: Docker + AWS / GCP / Heroku (예시)

---

## 🚀 설치 & 실행 방법

```bash
git clone https://github.com/inha-cloud-project-09/diary_back.git
cd diary_back

# 1. 환경 변수 설정 (예: .env 파일 생성)
# .env 예시:
# DB_HOST=localhost
# DB_USER=username
# DB_PASS=password
# DB_NAME=diary_db
# JWT_SECRET=your_jwt_token_key
# PORT=4000

npm install

# 2. 데이터베이스 마이그레이션 (ORM 사용 시)
npm run migrate

# 3. 개발 서버 실행
npm run dev
```

브라우저 혹은 API 테스트 도구(Postman / Insomnia 등)를 이용해 `http://localhost:8080/api/...` 엔드포인트로 접근 가능합니다.

---

## 🛠 주요 API

| 메서드 | 엔드포인트               | 설명               |
|--------|--------------------------|--------------------|
| POST   | `/api/auth/register`     | 회원가입           |
| POST   | `/api/auth/login`        | 로그인 (JWT 발급)  |
| GET    | `/api/diaries`           | 내 일기 목록 조회  |
| POST   | `/api/diaries`           | 새 일기 작성       |
| GET    | `/api/diaries/:id`       | 특정 일기 조회     |
| PUT    | `/api/diaries/:id`       | 일기 수정          |
| DELETE | `/api/diaries/:id`       | 일기 삭제          |
| GET    | `/api/diaries/search`    | 감정·키워드 검색   |

> 감정 분석 API는 `/api/analysis` 경로로 별도 제공

---

## 🧩 환경 변수

```env
DB_HOST=…
DB_PORT=…
DB_USER=…
DB_PASS=…
DB_NAME=…
JWT_SECRET=…
PORT=4000
```

필요한 변수는 코드 내 `config` 디렉토리 또는 `.env.example` 파일을 참고하세요.

---

## 🧪 테스트

```bash
npm test
```

테스트 코드가 포함되어 있다면, 유닛/통합 테스트 실행을 위해 위 명령을 사용하세요.

---

## 🧑‍💻 개발자 안내

1. **디렉토리 구조**
   ```
   src/
   ├── controllers/…
   ├── services/…
   ├── models/…
   ├── routes/…
   ├── middlewares/…
   ├── utils/…
   └── app.js
   ```

2. **감정 분석 로직**
   - `services/emotionService.js`에서 구현
   - 외부 라이브러리, API, 머신러닝 모델 등 연동

3. **인증 미들웨어**
   - `middlewares/auth.js`에서 JWT 검증, 사용자 권한 체크

4. **유닛 테스트**
   - `tests/` 디렉토리 내 `jest` / `mocha` 등으로 구성

---

## 📈 향후 발전 방향
- OAuth (Google, Apple 등) 로그인 지원
- 소셜 기능: 친구, 공유, 코멘트 등
- 감정 예측 AI 정확도 고도화
- 모바일/웹 클라이언트와의 통신 최적화 (GraphQL 등)
- CI/CD + Docker 자동 배포

---

## 🔖 라이선스
MIT License

---
