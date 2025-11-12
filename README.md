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

