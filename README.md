# 📊 CoinDash - 실시간 암호화폐 대시보드

Spring Boot 4.1.0 + React 기반의 실시간 암호화폐 가격 대시보드입니다.
Binance WebSocket에서 실시간 데이터를 받아 GraphQL Subscription으로 클라이언트에 push합니다.

---

## 🛠 기술 스택

### 백엔드
| 항목 | 기술 |
|------|------|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 4.1.0 |
| 웹 | Spring WebFlux (논블로킹) |
| GraphQL | Spring for GraphQL |
| DB | H2 (인메모리) + Spring Data JPA |
| 실시간 데이터 | Binance WebSocket API |
| 인증 | Spring Security + OAuth2 + JWT |
| 빌드 도구 | Gradle |

### 프론트엔드
| 항목 | 기술 |
|------|------|
| 프레임워크 | React + TypeScript |
| 스타일 | Tailwind CSS |
| GraphQL 클라이언트 | Apollo Client 4.x |
| 실시간 구독 | Apollo `useSubscription` 훅 |
| 라우팅 | React Router DOM 7.x |

---

## 🏗 아키텍처

```
Binance WebSocket
      |
      | wss://data-stream.binance.vision (실시간 코인 가격)
      ↓
Spring Boot (WebFlux)
      |
      | Flux<CoinPrice> 스트림
      ↓
GraphQL Subscription (WebSocket /graphql)
      |
React + Apollo Client
      |
      | useSubscription
      ↓
실시간 대시보드 렌더링
```

---

## 📁 프로젝트 구조

```
src/main/java/or/kr/bashboard/
├── coin/
│   ├── controller/
│   │   ├── CoinController.java         # Subscription + coins Query
│   │   └── FavoriteController.java     # 즐겨찾기 Query/Mutation
│   ├── entity/
│   │   ├── Coin.java                   # 코인 엔티티
│   │   └── Favorite.java              # 즐겨찾기 엔티티
│   ├── model/
│   │   └── CoinPrice.java             # GraphQL 응답 모델 (Binance 데이터)
│   ├── repository/
│   │   ├── CoinRepository.java
│   │   └── FavoriteRepository.java
│   └── service/
│       ├── BinanceService.java         # Binance WebSocket 연결/스트림 관리
│       └── FavoriteService.java        # 즐겨찾기 비즈니스 로직
├── member/
│   ├── controller/
│   │   └── MemberController.java       # 회원가입/로그인 REST API
│   ├── dto/
│   │   ├── SignupRequest.java
│   │   └── LoginRequest.java
│   ├── entity/
│   │   └── Member.java                 # 회원 엔티티
│   ├── repository/
│   │   └── MemberRepository.java
│   └── service/
│       └── MemberService.java          # 회원가입/로그인 비즈니스 로직
└── global/
    ├── config/
    │   ├── SecurityConfig.java         # Spring Security + CORS 설정
    │   ├── WebSocketConfig.java        # CORS 설정
    │   └── JwtAuthFilter.java          # JWT 인증 필터
    ├── jwt/
    │   └── JwtProvider.java            # JWT 발급/검증
    └── oauth/
        ├── OAuth2SuccessHandler.java   # 소셜 로그인 성공 후 JWT 발급
        └── OAuth2UserService.java      # 소셜 로그인 유저 정보 처리
```

---

## 📦 의존성 (`build.gradle`)

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.1.0'
    id 'io.spring.dependency-management' version '1.1.7'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencyManagement {
    imports {
        mavenBom 'me.paulschwarz:spring-dotenv-bom:5.1.0'
    }
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.springframework.boot:spring-boot-starter-graphql'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    developmentOnly 'me.paulschwarz:springboot4-dotenv'
    compileOnly 'org.projectlombok:lombok'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    runtimeOnly 'com.h2database:h2'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.graphql:spring-graphql-test'
    testCompileOnly 'org.projectlombok:lombok'
    testAnnotationProcessor 'org.projectlombok:lombok'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

---

## 🗄 DB 설계 (`schema.sql`)

```sql
CREATE TABLE member (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(100) NOT NULL UNIQUE,
    name       VARCHAR(50)  NOT NULL,
    password   VARCHAR(255),           -- 소셜 로그인은 null
    provider   VARCHAR(20)  NOT NULL,  -- google / local
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE coin (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol     VARCHAR(20)  NOT NULL UNIQUE,
    name       VARCHAR(50)  NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE favorite (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT NOT NULL,
    coin_id    BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    FOREIGN KEY (coin_id)   REFERENCES coin(id)   ON DELETE CASCADE
);

CREATE TABLE price_history (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    coin_id     BIGINT         NOT NULL,
    price       DECIMAL(20, 8) NOT NULL,
    volume      DECIMAL(30, 8) NOT NULL,
    recorded_at TIMESTAMP      NOT NULL,
    FOREIGN KEY (coin_id) REFERENCES coin(id) ON DELETE CASCADE
);

CREATE TABLE price_stat (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    coin_id    BIGINT         NOT NULL UNIQUE,
    high_price DECIMAL(20, 8) NOT NULL,
    low_price  DECIMAL(20, 8) NOT NULL,
    high_at    TIMESTAMP      NOT NULL,
    low_at     TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (coin_id) REFERENCES coin(id) ON DELETE CASCADE
);

CREATE INDEX idx_price_history_coin_time
    ON price_history (coin_id, recorded_at DESC);

INSERT INTO coin (symbol, name) VALUES
    ('BTCUSDT', 'Bitcoin'),
    ('ETHUSDT', 'Ethereum'),
    ('BNBUSDT', 'BNB'),
    ('SOLUSDT', 'Solana'),
    ('XRPUSDT', 'XRP');
```

---

## 📡 GraphQL 스키마 (`schema.graphqls`)

```graphql
type CoinPrice {
  symbol: String!
  price: String!
  change: String!
  changePercent: String!
  volume: String!
  timestamp: String!
}

type Coin {
  id: ID!
  symbol: String!
  name: String!
}

type PriceHistory {
  price: String!
  volume: String!
  recordedAt: String!
}

type PriceStat {
  highPrice: String!
  lowPrice: String!
  highAt: String!
  lowAt: String!
}

type Member {
  id: ID!
  email: String!
  name: String!
  provider: String!
}

type Query {
  me: Member
  coins: [Coin!]!
  favorites: [Coin!]!
  priceHistory(symbol: String!, page: Int, size: Int): [PriceHistory!]!
  priceStat(symbol: String!): PriceStat
}

type Mutation {
  addFavorite(symbol: String!): Coin!
  removeFavorite(symbol: String!): Boolean!
}

type Subscription {
  priceUpdated(symbol: String!): CoinPrice!
}
```

---

## 🔐 인증 흐름

### 구글 소셜 로그인
```
브라우저 → /oauth2/authorization/google
      ↓
구글 로그인 완료
      ↓
OAuth2UserService → DB에 Member 저장/조회
      ↓
OAuth2SuccessHandler → JWT 발급
      ↓
http://localhost:5173?token=xxx 리디렉션
      ↓
React TokenHandler → localStorage 저장
```

### 일반 로그인
```
POST /api/auth/login { email, password }
      ↓
MemberService → 비밀번호 검증
      ↓
JwtProvider → JWT 발급
      ↓
{ token: "eyJhbGc..." } 응답
```

### JWT 인증 필터
```
GraphQL 요청 (Authorization: Bearer 토큰)
      ↓
JwtAuthFilter → 토큰 파싱
      ↓
ReactiveSecurityContextHolder에 저장
      ↓
FavoriteController.getCurrentMember() → 현재 유저 조회
```

---

## 🚀 실행 방법

### 환경변수 설정 (`.env`)
```
GOOGLE_CLIENT_ID=구글_클라이언트_ID
GOOGLE_CLIENT_SECRET=구글_클라이언트_시크릿
JWT_SECRET=32자_이상_랜덤_문자열
```

### 백엔드 실행
```bash
./gradlew bootRun
```

### 접근 가능한 URL
| URL | 설명 |
|-----|------|
| `http://localhost:8080/graphql` | GraphQL 엔드포인트 |
| `http://localhost:8080/graphiql` | GraphQL 테스트 UI |

---

## 📌 개발 현황

### 완료
- [x] Spring Boot 4.1.0 + WebFlux 설정
- [x] Binance WebSocket 연결 (실시간 코인 가격)
- [x] GraphQL Subscription (실시간 push)
- [x] GraphQL Query (코인 목록, 즐겨찾기)
- [x] GraphQL Mutation (즐겨찾기 추가/삭제)
- [x] 구글 소셜 로그인 (OAuth2 + JWT)
- [x] 일반 회원가입/로그인
- [x] JWT 인증 필터
- [x] 비밀번호 유효성 검증 (8자 이상, 영문/숫자/특수문자)
- [x] 소셜 로그인 이메일 일반 가입 차단

### 예정
- [ ] 가격 히스토리 누적 저장 및 조회
- [ ] 고가/저가 통계 (PriceStat)
- [ ] 비밀번호 변경 API
- [ ] me Query (내 정보 GraphQL 조회)
