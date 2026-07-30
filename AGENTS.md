# AGENTS.md

## Objetivo

Desenvolver uma API REST para gestão de finanças pessoais utilizando Java, Spring Boot e PostgreSQL.

## Stack

- Java 25
- Spring Boot
- Spring Security
- JWT (OAuth2 Resource Server)
- Spring Data JPA
- PostgreSQL
- Flyway
- Docker
- Maven
- JUnit 5

## Arquitetura

Utilizar arquitetura em camadas:

- Controller
- Service
- Repository
- Entity
- DTO
- Mapper
- Exception
- Config

Toda regra de negócio deve ficar na camada Service.

## Convenções

- Utilizar inglês para nomes de classes, métodos, variáveis e banco de dados.
- Utilizar português apenas em mensagens retornadas ao usuário quando necessário.
- Utilizar `camelCase` para métodos e variáveis.
- Utilizar `PascalCase` para classes.
- Utilizar `UPPER_SNAKE_CASE` para constantes e enums.

## Código

- Não escrever comentários no código.
- Escrever código limpo e autoexplicativo.
- Evitar duplicação de código (DRY).
- Priorizar legibilidade.
- Manter métodos pequenos e com responsabilidade única.
- Não criar código morto ou não utilizado.
- Não adicionar dependências sem necessidade.

## Boas práticas

- Validar entradas utilizando Bean Validation.
- Utilizar DTOs para entrada e saída.
- Nunca expor entidades diretamente pela API.
- Utilizar tratamento global de exceções.
- Utilizar transações apenas quando necessário.
- Preferir composição em vez de herança quando possível.
- Seguir os princípios SOLID.

## Banco de Dados

- Todas as alterações no banco devem ser feitas através do Flyway.
- Nunca utilizar `ddl-auto=create` ou `ddl-auto=update`.
- Utilizar `ddl-auto=validate`.

## API

- Seguir padrões REST.
- Utilizar códigos HTTP corretos.
- Documentar endpoints com OpenAPI/Swagger.
- Versionar endpoints quando necessário.

## Testes

- Criar testes unitários para regras de negócio.
- Utilizar Mockito para mocks.
- Cobrir cenários de sucesso e erro.

## Segurança

- Toda rota deve possuir nível de acesso definido.
- Utilizar autenticação via JWT.
- Nunca armazenar senhas em texto puro.

## Git

- Commits pequenos e descritivos.
- Não versionar arquivos sensíveis.
- Manter `.env`, logs e arquivos temporários no `.gitignore`.

## Dependências

Adicionar novas dependências somente quando houver necessidade real e justificativa técnica.