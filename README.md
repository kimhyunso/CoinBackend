# 📊 Crypto Dashboard

실시간 암호화폐 가격을 보여주는 대시보드 사이드 프로젝트입니다.
React + Spring Boot 4.1.0 기반으로, Binance WebSocket에서 실시간 데이터를 받아 GraphQL Subscription으로 클라이언트에 push합니다.

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
| 빌드 도구 | Gradle |

### 프론트엔드
| 항목 | 기술 |
|------|------|
| 프레임워크 | React |
| GraphQL 클라이언트 | Apollo Client |
| 실시간 구독 | Apollo `useSubscription` 훅 |

---

## 🏗 아키텍처

```
Binance WebSocket
      |
      | wss://stream.binance.com (실시간 코인 가격)
      ↓
Spring Boot (WebFlux)
      |
      | Flux<CoinPrice> 스트림
      ↓
GraphQL Subscription
      |
      | WebSocket (/graphql)
      ↓
React + Apollo Client
      |
      | useSubscription
      ↓
실시간 대시보드 렌더링
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

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.springframework.boot:spring-boot-starter-graphql'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    compileOnly 'org.projectlombok:lombok'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    runtimeOnly 'com.h2database:h2'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.graphql:spring-graphql-test'
    testCompileOnly 'org.projectlombok:lombok'
    testAnnotationProcessor 'org.projectlombok:lombok'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

---

## ⚙️ 설정 (`application.yml`)

```yaml
spring:
  application:
    name: dashboard
  graphql:
    http:
      path: /graphql
    websocket:
      path: /graphql
      connection-init-timeout: 60s
    graphiql:
      enabled: true
  h2:
    console:
      enabled: true
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: ""
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

---

## 📡 Binance WebSocket 데이터

별도 API 키나 회원가입 없이 바로 연결 가능합니다.

**엔드포인트**
```
wss://stream.binance.com:9443/ws/btcusdt@ticker
```

**수신 데이터 예시**
```json
{
  "s": "BTCUSDT",
  "c": "67842.50",
  "p": "+1234.20",
  "P": "+1.85",
  "v": "12453.231",
  "T": 1714912345678
}
```

| 필드 | 설명 |
|------|------|
| `s` | 심볼 (BTCUSDT 등) |
| `c` | 현재가 |
| `p` | 가격 변동 (절대값) |
| `P` | 변동률 (%) |
| `v` | 거래량 |
| `T` | 타임스탬프 |

---

## 🚀 실행 방법

### 백엔드
```bash
./gradlew bootRun
```

서버 실행 후 접근 가능한 URL:
- GraphQL 엔드포인트: `http://localhost:8080/graphql`
- GraphiQL (테스트 UI): `http://localhost:8080/graphiql`
- H2 콘솔: `http://localhost:8080/h2-console`

### 프론트엔드
```bash
cd frontend
npm install
npm start
```

---

## 📋 GraphQL 스키마 (예정)

```graphql
type CoinPrice {
  symbol: String!
  price: String!
  change: String!
  changePercent: String!
  volume: String!
  timestamp: String!
}

type Query {
  latestPrice(symbol: String!): CoinPrice
}

type Subscription {
  priceUpdated(symbol: String!): CoinPrice!
}
```

---

## 🗂 프로젝트 구조 (예정)

```
src/
└── main/
    ├── java/com/example/dashboard/
    │   ├── controller/
    │   │   └── CoinController.java       # @QueryMapping, @SubscriptionMapping
    │   ├── service/
    │   │   └── BinanceService.java       # WebClient로 Binance 연결
    │   ├── model/
    │   │   └── CoinPrice.java            # 데이터 모델
    │   └── DashboardApplication.java
    └── resources/
        ├── graphql/
        │   └── schema.graphqls           # GraphQL 스키마
        └── application.yml
```

---

## 📌 개발 순서

- [x] 기술 스택 선정
- [x] 프로젝트 초기 설정 (`build.gradle`, `application.yml`)
- [ ] GraphQL 스키마 작성
- [ ] Binance WebSocket 연결 (`WebClient`)
- [ ] GraphQL Subscription 구현 (`Flux<CoinPrice>`)
- [ ] React + Apollo Client 연결
- [ ] 실시간 대시보드 UI 구현

---

## 💡 왜 이 기술 스택인가?

**WebFlux를 선택한 이유**
Spring MVC(동기)와 달리 WebFlux는 논블로킹 방식으로 동작합니다. Binance에서 실시간으로 쏟아지는 데이터를 `Flux` 스트림으로 처리하고 수많은 클라이언트에 동시에 push하는 데 적합합니다.

**GraphQL Subscription을 선택한 이유**
REST API는 클라이언트가 주기적으로 요청해야 하지만(Polling), GraphQL Subscription은 서버가 데이터 변경 시 클라이언트에 자동으로 push합니다. WebSocket 위에서 동작하며 진짜 실시간 경험을 제공합니다.

**Binance WebSocket을 선택한 이유**
주식 실시간 데이터는 거래소 라이선스 구조로 인해 유료입니다. 반면 코인 거래소(Binance 등)는 거래량 증대를 위해 실시간 데이터를 무료로 공개합니다. API 키 없이 즉시 연결 가능합니다.