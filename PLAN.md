# PLAN.md — Fincore

## Visão Geral

API REST para gestão de finanças pessoais com sugestões via IA (Gemini + LangChain4J).

---

## Stack

- Java 25 / Spring Boot 4.1.0
- Spring Security + OAuth2 Resource Server (JWT)
- Spring Data JPA + Flyway + PostgreSQL
- LangChain4J + Google Gemini API
- Docker (para a aplicação)
- Supabase (PostgreSQL gerenciado na nuvem)
- Maven + JUnit 5 + Mockito
- OpenAPI/Swagger (springdoc-openapi)

---

## Fase 0 — Infraestrutura

- [X] **Supabase** — Criar projeto no Supabase Cloud (PostgreSQL 15+)
- [X] **`application.yaml`** — Configurar datasource, JPA (`validate`), Flyway, JWT, server, LangChain4J
- [X] **`.env`** — Secrets: `SUPABASE_DB_URL`, `SUPABASE_DB_USER`, `SUPABASE_DB_PASSWORD`, `JWT_SECRET`, `GEMINI_API_KEY`
- [X] **`.gitignore`** — Adicionar `.env`
- [X] **`Dockerfile`** — Dockerfile multi-stage apenas para a aplicação
- [ ] **`docker-compose.yml`** — Opcional, apenas para dev local com Testcontainers

---

## Fase 1 — Segurança e Usuário

### Entidade: `User`

| Campo | Tipo | Observação |
|---|---|---|
| id | UUID | PK |
| name | String | |
| email | String | unique, not null |
| password | String | bcrypt |
| role | Enum | `USER`, `ADMIN` |
| createdAt | LocalDateTime | |
| updatedAt | LocalDateTime | |

### Endpoints

- [X] `POST   /api/v1/auth/register` — Cadastro
- [X] `POST   /api/v1/auth/login` — Login → JWT
- [X] `GET    /api/v1/users/me` — Perfil do logado
- [X] `PUT    /api/v1/users/me` — Atualizar perfil

### Camadas

- [X] `AuthController`, `AuthService`, `UserRepository`, `SecurityConfig`

### Flyway

- [X] `V1__create_users_table.sql`

---

## Fase 2 — Categorias

### Entidade: `Category`

| Campo | Tipo | Observação |
|---|---|---|
| id | UUID | PK |
| name | String | |
| type | Enum | `INCOME`, `EXPENSE` |
| icon | String | opcional |
| color | String | opcional, hexadecimal |
| user | User FK | nullable → categorias globais |
| createdAt | LocalDateTime | |
| updatedAt | LocalDateTime | |

### Endpoints

- [X] `GET    /api/v1/categories` — Listar (globais + do usuário)
- [X] `POST   /api/v1/categories` — Criar
- [X] `PUT    /api/v1/categories/{id}` — Atualizar
- [X] `DELETE /api/v1/categories/{id}` — Remover

### Flyway

- [X] `V2__create_categories_table.sql`
- [X] `V3__seed_default_categories.sql`

---

## Fase 3 — Contas Bancárias

### Entidade: `Account`

| Campo | Tipo | Observação |
|---|---|---|
| id | UUID | PK |
| name | String | |
| type | Enum | `CHECKING`, `SAVINGS`, `CREDIT_CARD` |
| balance | BigDecimal | |
| user | User FK | |
| createdAt | LocalDateTime | |
| updatedAt | LocalDateTime | |

### Endpoints

- [ ] `GET    /api/v1/accounts` — Listar
- [ ] `POST   /api/v1/accounts` — Criar
- [ ] `PUT    /api/v1/accounts/{id}` — Atualizar
- [ ] `DELETE /api/v1/accounts/{id}` — Remover

### Flyway

- [ ] `V4__create_accounts_table.sql`

---

## Fase 4 — Transações (com parcelamento)

### Entidade: `Transaction`

| Campo | Tipo | Observação |
|---|---|---|
| id | UUID | PK |
| description | String | |
| amount | BigDecimal | |
| date | LocalDate | |
| type | Enum | `INCOME`, `EXPENSE` |
| category | Category FK | |
| account | Account FK | |
| user | User FK | |
| notes | String | opcional |
| isRecurring | Boolean | |
| installmentNumber | Integer | nullable, 1-based |
| totalInstallments | Integer | nullable |
| parentTransaction | Transaction FK | nullable, auto-ref para série |
| createdAt | LocalDateTime | |
| updatedAt | LocalDateTime | |

### Regras

- Se `totalInstallments > 1`, gerar N transações com `installmentNumber` sequencial vinculadas ao mesmo `parentTransaction`
- Ao deletar/atualizar, opção de aplicar em toda a série (`applyToAll`)
- Saldo da conta é atualizado automaticamente via Service

### Endpoints

- [ ] `GET    /api/v1/transactions` — Listar (filtros: data, categoria, conta, tipo)
- [ ] `POST   /api/v1/transactions` — Criar (suporta parcelamento)
- [ ] `PUT    /api/v1/transactions/{id}` — Atualizar
- [ ] `DELETE /api/v1/transactions/{id}` — Remover
- [ ] `GET    /api/v1/transactions/installments/{parentId}` — Listar parcelas

### Flyway

- [ ] `V5__create_transactions_table.sql`

---

## Fase 5 — Orçamentos

### Entidade: `Budget`

| Campo | Tipo | Observação |
|---|---|---|
| id | UUID | PK |
| name | String | |
| amount | BigDecimal | limite do orçamento |
| category | Category FK | |
| user | User FK | |
| month | Integer | 1-12 |
| year | Integer | |
| createdAt | LocalDateTime | |
| updatedAt | LocalDateTime | |

### Regras

- Um orçamento por categoria + mês/ano
- Consulta agregada para gastos reais vs orçado

### Endpoints

- [ ] `GET    /api/v1/budgets` — Listar
- [ ] `POST   /api/v1/budgets` — Criar
- [ ] `PUT    /api/v1/budgets/{id}` — Atualizar
- [ ] `DELETE /api/v1/budgets/{id}` — Remover
- [ ] `GET    /api/v1/budgets/summary?month=&year=` — Gasto vs orçado

### Flyway

- [ ] `V6__create_budgets_table.sql`

---

## Fase 6 — Metas Financeiras

### Entidade: `FinancialGoal`

| Campo | Tipo | Observação |
|---|---|---|
| id | UUID | PK |
| name | String | |
| targetAmount | BigDecimal | |
| currentAmount | BigDecimal | default 0 |
| deadline | LocalDate | opcional |
| user | User FK | |
| createdAt | LocalDateTime | |
| updatedAt | LocalDateTime | |

### Endpoints

- [ ] `GET    /api/v1/goals` — Listar
- [ ] `POST   /api/v1/goals` — Criar
- [ ] `PUT    /api/v1/goals/{id}` — Atualizar
- [ ] `DELETE /api/v1/goals/{id}` — Remover
- [ ] `GET    /api/v1/goals/{id}/progress` — Percentual atingido

### Flyway

- [ ] `V7__create_financial_goals_table.sql`

---

## Fase 7 — Dashboard e Relatórios

### Endpoints

- [ ] `GET /api/v1/dashboard/summary?month=&year=` — Resumo: receitas, despesas, saldo
- [ ] `GET /api/v1/dashboard/expenses-by-category?month=&year=` — Gastos por categoria
- [ ] `GET /api/v1/dashboard/monthly-evolution?months=12` — Evolução mensal

---

## Fase 8 — Sugestões com IA (Gemini + LangChain4J)

### Dependências

- [ ] `langchain4j-spring-boot-starter` — Integração Spring Boot com LangChain4J
- [ ] `langchain4j-google-ai-gemini` — Provider Google Gemini

### Configuração do datasource (Supabase)

```yaml
spring:
  datasource:
    url: ${SUPABASE_DB_URL}
    username: ${SUPABASE_DB_USER}
    password: ${SUPABASE_DB_PASSWORD}
    hikari:
      ssl: true
      maximum-pool-size: 10
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration
```

A URL do Supabase segue o padrão: `jdbc:postgresql://<host>:5432/<database>?sslmode=require`

### Configuração do LangChain4J (Gemini)

```yaml
langchain4j:
  google-ai-gemini:
    chat-model:
      api-key: ${GEMINI_API_KEY}
      model-name: gemini-2.0-flash
```

### Camadas

```
ai/
├── controller/AIController.java
├── service/AISuggestionService.java
├── dto/SuggestionRequest.java
├── dto/SuggestionResponse.java
└── config/AiConfig.java
```

`AISuggestionService` usa o `GoogleAiGeminiModel` do LangChain4J para gerar sugestões baseadas no resumo financeiro do usuário.

### Endpoints

- [ ] `POST /api/v1/ai/suggestion` — Resumo financeiro → sugestão textual
- [ ] `POST /api/v1/ai/insights` — Histórico → padrões de gasto

---

## Fase 9 — Testes

- [ ] Testes unitários para Services (Mockito)
- [ ] Testes de integração para Controllers (`@WebMvcTest`)
- [ ] Testes de repositório (`@DataJpaTest`)
- [ ] Cobertura mínima: regras de negócio e validações

---

## Fase 10 — Documentação e Deploy

- [ ] Dockerfile multi-stage para a aplicação
- [ ] OpenAPI via `springdoc-openapi` em `/swagger-ui.html`
- [ ] Deploy da aplicação em container Docker conectado ao Supabase
- [ ] Configurar pool de conexões e SSL (exigido pelo Supabase)

---

## Diagrama de Entidades (DER)

```
User 1──N Account
User 1──N Category
User 1──N Transaction
User 1──N Budget
User 1──N FinancialGoal
Category 1──N Transaction
Category 1──N Budget
Account 1──N Transaction
Transaction *──1 Transaction (parentTransaction)
```

---

## Estrutura de Pacotes

```
com.ruan.fincore
├── FincoreApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   └── OpenApiConfig.java
├── auth/
│   ├── controller/AuthController.java
│   ├── service/AuthService.java
│   └── dto/LoginRequest.java
│       RegisterRequest.java
│       TokenResponse.java
├── user/
│   ├── entity/User.java
│   ├── repository/UserRepository.java
│   ├── service/UserService.java
│   ├── controller/UserController.java
│   └── dto/UserRequest.java
│       UserResponse.java
├── category/
│   ├── entity/Category.java
│   ├── repository/CategoryRepository.java
│   ├── service/CategoryService.java
│   ├── controller/CategoryController.java
│   └── dto/CategoryRequest.java
│       CategoryResponse.java
├── account/
│   ├── entity/Account.java
│   ├── repository/AccountRepository.java
│   ├── service/AccountService.java
│   ├── controller/AccountController.java
│   └── dto/AccountRequest.java
│       AccountResponse.java
├── transaction/
│   ├── entity/Transaction.java
│   ├── repository/TransactionRepository.java
│   ├── service/TransactionService.java
│   ├── controller/TransactionController.java
│   └── dto/TransactionRequest.java
│       TransactionResponse.java
├── budget/
│   ├── entity/Budget.java
│   ├── repository/BudgetRepository.java
│   ├── service/BudgetService.java
│   ├── controller/BudgetController.java
│   └── dto/BudgetRequest.java
│       BudgetResponse.java
├── goal/
│   ├── entity/FinancialGoal.java
│   ├── repository/FinancialGoalRepository.java
│   ├── service/FinancialGoalService.java
│   ├── controller/FinancialGoalController.java
│   └── dto/FinancialGoalRequest.java
│       FinancialGoalResponse.java
├── dashboard/
│   ├── controller/DashboardController.java
│   ├── service/DashboardService.java
│   └── dto/DashboardSummary.java
├── ai/
│   ├── controller/AIController.java
│   ├── service/AISuggestionService.java
│   └── dto/SuggestionRequest.java
│       SuggestionResponse.java
└── common/
    ├── exception/GlobalExceptionHandler.java
    ├── exception/ResourceNotFoundException.java
    └── mapper/EntityMapper.java
```

---

## Migrations Flyway

- [X] `V1__create_users_table.sql`
- [X] `V2__create_categories_table.sql`
- [X] `V3__seed_default_categories.sql`
- [ ] `V4__create_accounts_table.sql`
- [ ] `V5__create_transactions_table.sql`
- [ ] `V6__create_budgets_table.sql`
- [ ] `V7__create_financial_goals_table.sql`

---

## Ordem de Execução

|  | Fase | Depende de | Previsão |
|---|---|---|---|
| [X] | 0 — Infraestrutura | — | — |
| [X] | 1 — Segurança + Usuário | Fase 0 | — |
| [X] | 2 — Categorias | Fase 1 | — |
| [ ] | 2 — Categorias | Fase 1 | — |
| [ ] | 3 — Contas | Fase 1 | — |
| [ ] | 4 — Transações | Fases 2, 3 | — |
| [ ] | 5 — Orçamentos | Fases 2, 4 | — |
| [ ] | 6 — Metas | Fase 1 | — |
| [ ] | 7 — Dashboard | Fase 4 | — |
| [ ] | 8 — IA (Gemini) | Fase 7 | — |
| [ ] | 9 — Testes | Fases 1-8 | — |
| [ ] | 10 — Deploy | Fase 9 | — |

---

## Dependências a Adicionar no `pom.xml`

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>

<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-spring-boot-starter</artifactId>
    <version>1.0.0-beta2</version>
</dependency>

<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-google-ai-gemini</artifactId>
    <version>1.0.0-beta2</version>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```
