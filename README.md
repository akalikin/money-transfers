# Revolut - Money Transfers

## Running

```
mvn clean compile assembly:single
mvn exec:java
```

## API

`GET /v1/accounts` - list of all currently stored accounts

`GET /v1/accounts/{accountId}` - retrieve the details of a requested account

`POST /v1/accounts/{accountId}` - create a new account

`PATCH /v1/accounts/from/{fromId}/to/{toId}` - transfer money from one account to another
