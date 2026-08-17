# Fincore

API REST para gestão de finanças pessoais com sugestões via IA (Gemini + LangChain4J).

## Stack

- Java 25 / Spring Boot 4.1.0
- Spring Security + OAuth2 Resource Server (JWT)
- Spring Data JPA + Flyway + PostgreSQL
- LangChain4J + Google Gemini API
- springdoc-openapi (Swagger UI)
- Docker

## Pré-requisitos

- Java 25+
- Maven 3.9+
- Docker (opcional)
- Conta no [Supabase](https://supabase.com) (PostgreSQL)
- Chave de API do [Google Gemini](https://aistudio.google.com)

## Variáveis de Ambiente

Copie o `.env.example` para `.env` e preencha:

| Variável | Descrição |
|----------|-----------|
| `SUPABASE_DB_URL` | URL JDBC do PostgreSQL (ex: `jdbc:postgresql://host:5432/db?sslmode=require`) |
| `SUPABASE_DB_USER` | Usuário do banco |
| `SUPABASE_DB_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Chave secreta para JWT (Base64, mínimo 256 bits) |
| `GEMINI_API_KEY` | Chave de API do Google Gemini |
| `PORT` | Porta da aplicação (padrão: 8080) |

## Como Executar

### Localmente

```bash
./mvnw spring-boot:run
```

### Com Docker

```bash
docker-compose up --build
```

A aplicação estará disponível em `http://localhost:8080`.

## Documentação da API

Acesse o Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```
http://localhost:8080/v3/api-docs
```

## Endpoints

### Autenticação

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/v1/auth/register` | Cadastro de usuário |
| POST | `/api/v1/auth/login` | Login (retorna JWT) |

### Usuário

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/v1/users/me` | Perfil do usuário logado |
| PUT | `/api/v1/users/me` | Atualizar perfil |

### Categorias

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/v1/categories` | Listar categorias |
| POST | `/api/v1/categories` | Criar categoria |
| PUT | `/api/v1/categories/{id}` | Atualizar categoria |
| DELETE | `/api/v1/categories/{id}` | Remover categoria |

### Contas Bancárias

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/v1/accounts` | Listar contas |
| POST | `/api/v1/accounts` | Criar conta |
| PUT | `/api/v1/accounts/{id}` | Atualizar conta |
| DELETE | `/api/v1/accounts/{id}` | Remover conta |

### Transações

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/v1/transactions` | Listar transações (filtros: data, categoria, conta, tipo) |
| POST | `/api/v1/transactions` | Criar transação (suporta parcelamento) |
| PUT | `/api/v1/transactions/{id}` | Atualizar transação |
| DELETE | `/api/v1/transactions/{id}` | Remover transação |
| GET | `/api/v1/transactions/installments/{parentId}` | Listar parcelas |

### Orçamentos

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/v1/budgets` | Listar orçamentos |
| POST | `/api/v1/budgets` | Criar orçamento |
| PUT | `/api/v1/budgets/{id}` | Atualizar orçamento |
| DELETE | `/api/v1/budgets/{id}` | Remover orçamento |
| GET | `/api/v1/budgets/summary?month=&year=` | Resumo gasto vs orçado |

### Metas Financeiras

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/v1/goals` | Listar metas |
| POST | `/api/v1/goals` | Criar meta |
| PUT | `/api/v1/goals/{id}` | Atualizar meta |
| DELETE | `/api/v1/goals/{id}` | Remover meta |
| GET | `/api/v1/goals/{id}/progress` | Percentual atingido |

### Dashboard

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/v1/dashboard/summary?month=&year=` | Resumo: receitas, despesas, saldo |
| GET | `/api/v1/dashboard/expenses-by-category?month=&year=` | Gastos por categoria |
| GET | `/api/v1/dashboard/monthly-evolution?months=12` | Evolução mensal |

### Inteligência Artificial

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/v1/ai/suggestion` | Sugestão financeira via IA |
| POST | `/api/v1/ai/insights` | Padrões de gasto via IA |

## Autenticação

Todas as rotas exceto `/api/v1/auth/**`, `/swagger-ui.html`, `/swagger-ui/**` e `/v3/api-docs/**` requerem autenticação via JWT.

Para autenticar:

1. Faça login em `POST /api/v1/auth/login`
2. Copie o token retornado
3. No Swagger UI, clique em **Authorize** e insira: `Bearer <token>`

## Licença

MIT
