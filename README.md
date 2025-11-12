# flowpay-ai

Small Spring Boot service exposing a single endpoint to perform a payment from Joao (payer) to Pedro (payee). Data is kept in-memory.

## Requirements
- Java 17+
- Maven 3.9+

## Build
```bash
mvn clean package
```

## Run
```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

Tip: if you prefer the Maven Wrapper, run `mvn -N wrapper` once and then use `./mvnw` / `mvnw.cmd` for consistent builds.

## Endpoint
- `POST /payment`
  - Request body:
    ```json
    { "amount": 150.00 }
    ```
  - Success response `200 OK`:
    ```json
    {
      "payer": "joao",
      "payee": "pedro",
      "amount": 150.00,
      "payerBalance": 850.00,
      "payeeBalance": 650.00
    }
    ```
  - Errors:
    - `400 Bad Request` when `amount` is missing/<= 0.
    - `422 Unprocessable Entity` when payer has insufficient funds.

## Notes
- Initial balances: Joao = 1000.00, Pedro = 500.00.
- All state is volatile; restarting the app resets balances.

## PostgreSQL + pgvector (for RAG)
- Dependencies: Spring JDBC, PostgreSQL driver, Flyway (already added).
- Configure environment variables or edit `src/main/resources/application.properties`:
  - `DB_URL=jdbc:postgresql://localhost:5432/flowpay`
  - `DB_USER=postgres`
  - `DB_PASSWORD=postgres`
- Flyway migrations provided:
  - `V1__enable_pgvector.sql`: enables `vector` extension
  - `V2__create_documents.sql`: creates `documents(id, content, embedding VECTOR(768))` and HNSW index

### Quick local setup (Docker Compose)
Spin up Postgres (pgvector) and the app:

```bash
docker compose up -d --build
```

This starts:
- `db`: Postgres with pgvector on `localhost:5432` (db: `flowpay`, user/pass: `postgres`)
- `app`: flowpay-ai on `http://localhost:8080`

### Environment variables
- A local `.env` file is included for convenience (ignored by Git). It provides defaults used by Spring and VS Code:
  - `DB_URL=jdbc:postgresql://localhost:5432/flowpay`
  - `DB_USER=postgres`
  - `DB_PASSWORD=postgres`
  - `SERVER_PORT=8080`
  - `JAVA_OPTS=`
You can edit `.env` to match your environment.

### RAG endpoints
- Upsert document: `POST /documents`
  - Body:
    ```json
    {
      "content": "some text",
      "embedding": [0.01, 0.02, 0.03, ...]
    }
    ```
  - Response: `{ "id": "<uuid>" }`

- Search by embedding: `POST /documents/search`
  - Body:
    ```json
    {
      "embedding": [0.01, 0.02, 0.03, ...],
      "topK": 5
    }
    ```
  - Response: array of `{ id, content, distance }` ordered by similarity

Notes:
- The `VECTOR(768)` dimension must match your embedding model size.
- If your Postgres/pgvector version doesn’t support HNSW, switch the index to IVFFlat.
 - To stop and remove containers: `docker compose down`
- To reset DB data: `docker compose down -v` (removes the named volume `db-data`)

## n8n integration (email notification)
Goal: send an email after a successful payment.

1) In n8n, create a workflow:
- Webhook node
  - Method: POST
  - Path: `/event/payment.completed`
  - Respond: JSON → `{ "status": "OK" }`
- Email node (SMTP/Gmail)
  - Subject: `Você recebeu um pagamento!`
  - Body:
    `Olá {{$json.receiverId}}, você recebeu R${{$json.amount}} de {{$json.payerId}}.`
  - Map recipient as you prefer (fixed or from JSON).

Tip for local testing without real SMTP: use MailHog (already included in docker compose).
- SMTP Host: `mailhog`
- SMTP Port: `1025`
- No auth/SSL
- View captured emails at `http://localhost:8025`

2) Copy the production webhook URL from the Webhook node (it will look like `http://<n8n-host>/webhook/event/payment.completed`).

3) Configure the app to call n8n:
- Set env var `N8N_WEBHOOK_URL` (or edit `.env`). Default: `http://localhost:5678/webhook/event/payment.completed`.

On each successful `/payment`, the app POSTs to n8n with:
```json
{ "payerId": "joao", "receiverId": "pedro", "amount": 123.45 }
```
