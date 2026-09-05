# 🌾 Farm-to-Cloud Kitchen

**Direct Farm-to-Cloud Kitchen Supply Chain Platform**

A REST API that connects farmers directly to cloud kitchens — cutting out middlemen, giving farmers fair and transparent pricing, and giving kitchens a reliable, verified supply chain with built-in trust scoring, demand intelligence, and dispute resolution.

---

## Table of contents

- [Overview](#overview)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Core features](#core-features)
- [Domain model](#domain-model)
- [Order lifecycle](#order-lifecycle)
- [API reference](#api-reference)
- [Authentication](#authentication)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Configuration](#configuration)
- [API documentation (Swagger)](#api-documentation-swagger)
- [Testing](#testing)
- [Deployment](#deployment)
- [Known limitations](#known-limitations)
- [Roadmap](#roadmap)

---

## Overview

Farm-to-Cloud Kitchen is a two-sided marketplace connecting two types of users:

| Role | Who they are | What they do |
|---|---|---|
| **FARMER** | Growers listing produce for sale | List crops, set prices, fulfill orders, get paid |
| **KITCHEN** | Cloud kitchens / restaurants sourcing produce | Browse listings, place orders, verify quality, pay farmers |

The platform layers in three things a plain marketplace doesn't have out of the box:

- **Smart pricing intelligence** — every listing gets a computed market price and a suggested fair price, so farmers aren't guessing and kitchens aren't overpaying.
- **Trust & verification system** — farmers build a trust score from delivery history; every order is weight/quality-verified by a delivery partner before a kitchen has to confirm it.
- **Dispute resolution** — either party can raise a dispute against an order, tracked independently of the order's own status.

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 11 |
| Framework | Spring Boot 2.7.16 |
| Database | MongoDB (via Spring Data MongoDB) |
| Security | Spring Security + JWT (JJWT 0.12.5, HS256) |
| API docs | springdoc-openapi-ui 1.7.0 (Swagger UI) |
| Boilerplate reduction | Lombok |
| Validation | Jakarta/Javax Bean Validation (`spring-boot-starter-validation`) |
| Testing | JUnit 5 + Mockito (dependencies present; test suite to be added) |
| Build tool | Maven |

---

## Architecture

Strict **layered architecture** — every request flows the same way, with no layer skipped:

```
HTTP request
    │
    ▼
JwtAuthenticationFilter  (validates JWT, populates SecurityContext)
    │
    ▼
Controller   (@RestController — routing, request/response shaping)
    │
    ▼
Service      (@Service — business logic, validation, orchestration)
    │
    ▼
Repository   (Spring Data MongoDB — persistence)
    │
    ▼
MongoDB
```

Controllers never call repositories directly. Some services (e.g. `OrderService`, `CropListingService`) also call *other* services to orchestrate cross-cutting logic — for example, placing an order touches `OrderService`, `CropListingService` (to decrement stock), `UserService` (to look up the buyer), and `IntelligenceService` (to record demand data), all in a single request.

---

## Core features

### 🧑‍🌾 For farmers
- Create, update, and delete crop listings with auto-computed market and fair pricing
- Dashboard with order history, trust score, delivery success rate, and demand insights
- Confirm or reject an order after it's been independently verified
- Track payments received per order

### 🍳 For kitchens
- Browse and search available crop listings (by name, price range, or location)
- Place orders against live listings with automatic stock deduction
- Get personalized recommendations (reorder suggestions + trending crops)
- Track order status and view farmer/payment history

### 🧠 Intelligence engine
- **Price comparison**: verdicts (underpriced / fair / overpriced) based on farmer price vs. market price
- **Demand heatmap**: crops ranked by order volume and demand level (HIGH / MEDIUM / LOW)
- **Recommendations**: personalized for each kitchen based on past order frequency + platform-wide trending crops

### ✅ Trust & verification
- Every order is weight/quality-checked by a delivery partner before the farmer has to confirm it
- Farmer trust score and delivery success rate recalculated automatically after each completed order
- Farmers become "verified" after 5+ orders with an 80%+ success rate

### 🚚 Delivery simulation
- Simulated delivery partner assignment and status tracking (ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED)

### 💸 Payments
- Simulated settlement flow generating a transaction ID once an order is delivered
- Automatically updates farmer trust score on successful payment

### ⚖️ Disputes
- Either party can raise a dispute tied to an order, with resolution tracking independent of order state

---

## Domain model

| Entity | MongoDB collection | Purpose |
|---|---|---|
| `User` | `users` | A farmer or kitchen account — includes trust score, verification flag, role-specific fields |
| `CropListing` | `crop_listings` | A farmer's produce for sale, with computed pricing and demand level |
| `Order` | `orders` | A kitchen's purchase against a listing; drives the core lifecycle |
| `Verification` | `verifications` | Delivery partner's weight/quality check, required before farmer confirmation |
| `DeliveryAssignment` | `delivery_assignments` | Simulated courier assignment and tracking |
| `Payment` | `payments` | Settlement record generated after delivery |
| `Dispute` | `disputes` | Complaint raised by either party against an order |
| `DemandData` | `demand_data` | Aggregated per-crop order stats powering the intelligence engine |

---

## Order lifecycle

```
PENDING → PICKUP_ASSIGNED → VERIFIED → FARMER_CONFIRMED → IN_TRANSIT → DELIVERED
                                              │
                                              └─→ CANCELLED (if farmer rejects)
```

| Status | Set by | Trigger |
|---|---|---|
| `PENDING` | `OrderService.placeOrder()` | Kitchen places an order |
| `PICKUP_ASSIGNED` | `DeliveryService.assignDelivery()` | Delivery partner assigned |
| `VERIFIED` | `VerificationService.verifyOrder()` | Delivery partner confirms weight/quality |
| `FARMER_CONFIRMED` / `CANCELLED` | `OrderService.farmerConfirm()` | Farmer accepts or rejects the verified order |
| `DELIVERED` | `DeliveryService.updateDeliveryStatus()` | Delivery partner marks delivery complete |

Payments can only be settled once an order reaches `DELIVERED`.

---

## API reference

All endpoints are prefixed with `/api`. 🔒 = requires `Authorization: Bearer <token>`.

### Auth (`/api/auth`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/signup` | Register a new farmer or kitchen |
| POST | `/login` | Authenticate and receive a JWT |

### Crop listings (`/api/crops`)
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/` | 🔒 | Create a listing |
| GET | `/` | — | List all available listings |
| GET | `/{id}` | — | Get a listing by ID |
| GET | `/farmer` | 🔒 | Get the logged-in farmer's own listings |
| PUT | `/{id}` | 🔒 | Update a listing |
| DELETE | `/{id}` | 🔒 | Delete a listing |

### Orders (`/api/orders`)
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/` | 🔒 | Place an order |
| GET | `/{id}` | — | Get order details |
| GET | `/farmer` | 🔒 | Orders for the logged-in farmer |
| GET | `/kitchen` | 🔒 | Orders for the logged-in kitchen |
| PUT | `/{id}/status` | — | Update order status |
| POST | `/{id}/farmer-confirm` | — | Farmer confirms or rejects a verified order |

### Verification (`/api/verify`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/{orderId}` | Submit weight/quality verification |
| GET | `/{orderId}` | Get verification details |

### Delivery (`/api/delivery`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/assign/{orderId}` | Assign a delivery partner |
| PUT | `/{id}/status` | Update delivery status |
| GET | `/order/{orderId}` | Get delivery details for an order |

### Payments (`/api/payments`)
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/settle/{orderId}` | — | Settle payment for a delivered order |
| GET | `/order/{orderId}` | — | Get payment for an order |
| GET | `/farmer` | 🔒 | Farmer's payment history |
| GET | `/kitchen` | 🔒 | Kitchen's payment history |

### Disputes (`/api/disputes`)
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/` | 🔒 | Raise a dispute |
| GET | `/{id}` | — | Get dispute details |
| GET | `/my` | 🔒 | Logged-in user's disputes |
| GET | `/order/{orderId}` | — | Disputes for an order |
| PUT | `/{id}/resolve` | — | Resolve a dispute |

### Intelligence (`/api/intelligence`)
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/price-compare` | — | Compare farmer price vs. market price |
| GET | `/demand-heatmap` | — | Crop demand levels platform-wide |
| GET | `/recommendations` | 🔒 | Personalized recommendations |

### Farmer & kitchen dashboards
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/farmer/dashboard` | 🔒 | Aggregated farmer dashboard |
| GET | `/api/farmer/trust-score` | 🔒 | Farmer's trust metrics |
| PUT | `/api/farmer/profile` | 🔒 | Update farmer profile |
| GET | `/api/kitchen/dashboard` | 🔒 | Aggregated kitchen dashboard |
| GET | `/api/kitchen/browse` | — | Browse/search/filter crops |
| PUT | `/api/kitchen/profile` | 🔒 | Update kitchen profile |

---

## Authentication

The platform uses **stateless JWT authentication**:

1. `POST /api/auth/signup` — passwords are hashed with `BCryptPasswordEncoder` before storage.
2. `POST /api/auth/login` — validates credentials and issues a JWT (HS256, 24-hour expiry) containing the user's ID, email, and role as claims.
3. Every request after that includes `Authorization: Bearer <token>`. `JwtAuthenticationFilter` intercepts it, validates the signature/expiry via `JwtTokenProvider`, and populates Spring Security's `SecurityContextHolder` with the user's ID and a `ROLE_<FARMER|KITCHEN>` authority.
4. Controllers read the current user via the injected `Authentication auth` parameter — `auth.getName()` returns the user's Mongo `_id`.

**Public routes** (no token required): `/api/auth/**`, `/api/public/**`, Swagger UI paths, and static assets.
**Everything else under `/api/**`** requires a valid, unexpired token.

---

## Project structure

```
src/main/java/net/farmtocloud/app/
├── FarmToCloudApplication.java       Application entry point
├── config/
│   ├── SpringSecurityConfig.java     Security filter chain, CORS, password encoder
│   ├── JwtAuthenticationFilter.java  Per-request JWT validation
│   └── SwaggerConfig.java            OpenAPI metadata bean
├── controller/                       REST endpoints (10 controllers)
├── service/                          Business logic (9 services)
├── repository/                       Spring Data MongoDB interfaces (8 repositories)
├── entity/                           MongoDB documents (8 entities)
├── dto/                              Request/response payloads
└── util/
    └── JwtTokenProvider.java         JWT generation/validation

src/main/resources/
├── application.yml                   Base config (port, JWT secret/expiry)
├── application-dev.yml               MongoDB connection URI
└── static/                           Bundled HTML/CSS/JS front end
```

---

## Getting started

### Prerequisites
- Java 11+
- Maven 3.6+
- A MongoDB instance — local, Docker, or MongoDB Atlas

### 1. Clone and configure

```bash
git clone <your-repo-url>
cd farm-to-cloud
```

Set your MongoDB connection string in `src/main/resources/application-dev.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/farmtocloud
```

Or for MongoDB Atlas, use your cluster's connection string in the same field.

### 2. Build

```bash
mvn clean install -DskipTests
```

### 3. Run

```bash
mvn spring-boot:run
```

The app starts on **http://localhost:8080**.

### 4. Try it

```bash
# Sign up as a farmer
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"Ravi Farmer","email":"ravi@test.com","password":"pass123","role":"FARMER","location":"Nashik"}'

# Log in
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ravi@test.com","password":"pass123"}'

# Create a crop listing (use the token from login)
curl -X POST http://localhost:8080/api/crops \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"cropName":"Tomato","quantity":100,"pricePerKg":25}'
```

---

## Configuration

| Property | File | Purpose |
|---|---|---|
| `spring.data.mongodb.uri` | `application-dev.yml` | MongoDB connection string |
| `server.port` | `application.yml` | HTTP port (default `8080`) |
| `app.jwt.secret` | `application.yml` | HS256 signing key for JWTs |
| `app.jwt.expiration-ms` | `application.yml` | Token lifetime in milliseconds (default 24h) |

> **Security note**: for any deployment beyond your own machine, move `app.jwt.secret` and `spring.data.mongodb.uri` into environment variables (e.g. `${JWT_SECRET}`, `${MONGODB_URI}`) rather than committing real values to version control.

---

## API documentation (Swagger)

Once running, interactive API docs are available at:

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **Raw OpenAPI spec**: `http://localhost:8080/v3/api-docs`

Swagger paths are publicly accessible even though most API routes require auth — click **Authorize** in the Swagger UI and paste `Bearer <your-token>` to test secured endpoints directly from the browser.

---

## Testing

The project ships with `spring-boot-starter-test`, `mockito-core`, and `junit-jupiter-api` on the classpath, ready for unit testing the service layer (services are cleanly separated from repositories via constructor/field injection, making them straightforward to mock).

Example pattern for testing a service with Mockito:

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrderRepository orderRepository;
    @Mock CropListingService cropListingService;
    @Mock UserService userService;
    @Mock IntelligenceService intelligenceService;
    @InjectMocks OrderService orderService;

    @Test
    void placeOrder_throwsWhenQuantityExceedsStock() {
        when(userService.getUserById("k1")).thenReturn(User.builder().id("k1").build());
        when(cropListingService.getListingById("c1"))
            .thenReturn(CropListing.builder().id("c1").quantity(5.0).pricePerKg(20.0).build());

        OrderRequest req = OrderRequest.builder().cropListingId("c1").quantity(10.0).build();

        assertThrows(RuntimeException.class, () -> orderService.placeOrder("k1", req));
    }
}
```

Run tests with:

```bash
mvn test
```

---

## Deployment

The app is a standard Spring Boot jar and runs anywhere Java + a reachable MongoDB instance are available.

**Quick path (Render / Railway):**
1. Externalize secrets — replace hardcoded values in `application.yml` / `application-dev.yml` with `${MONGODB_URI}`, `${JWT_SECRET}`, and `${PORT:8080}`.
2. Push the repo to GitHub.
3. Create a new Web Service, set build command `mvn clean package -DskipTests`, start command `java -jar target/farm-to-cloud-0.0.1-SNAPSHOT.jar`.
4. Add `MONGODB_URI` and `JWT_SECRET` as environment variables in the platform's dashboard.
5. In MongoDB Atlas, allow the deployment platform's IP range under **Network Access** (or `0.0.0.0/0` for testing).

**Docker / cloud VM / AWS-GCP-Azure** work the same way — build the jar, containerize or deploy it, and point `MONGODB_URI` at a reachable MongoDB instance.

---

## Known limitations

- **No dedicated test suite yet** — Mockito/JUnit dependencies are present but unused; adding one is a natural next step.
- **`JwtAuthenticationFilter` doesn't short-circuit on invalid tokens** — it silently skips setting authentication and lets the request continue; Spring Security's `authorizeRequests()` still blocks it downstream, but the response isn't shaped as the app's own `ApiResponse` error format.
- **Swagger has no `SecurityScheme` wired in** — the "Authorize" lock icon works but must be added manually per session; there's no auto-detection of secured endpoints in the OpenAPI bean.
- **Delivery partner assignment is simulated** — `DeliveryService` picks randomly from a hardcoded list rather than integrating a real courier network.
- **Payment settlement is simulated** — no real payment gateway integration; `PaymentService` marks payments `PAID` instantly with a generated transaction ID.
- **CORS is currently wide open (`*`)** — fine for development, should be scoped to a specific frontend origin before production use.

---

## Roadmap

- [ ] Add JUnit + Mockito test coverage for all service classes
- [ ] Custom `AuthenticationEntryPoint` for consistent JSON error responses on auth failures
- [ ] Wire a `SecurityScheme` into the OpenAPI bean for one-click Swagger authorization
- [ ] Real payment gateway integration (Razorpay/Stripe)
- [ ] Real-time order tracking via WebSockets
- [ ] Rate limiting on public endpoints
- [ ] Image upload support for crop listings and verification proofs (currently URL-only)

---

## License

Add your license here (MIT, Apache 2.0, etc.)

## Contact

BMD Team — team@farmtocloud.net