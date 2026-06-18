# Auth API Examples

Base URL: `{{base_url}}` (replace with your server address)

---

## Register

POST /api/auth/register
Headers:
- `Content-Type: application/json`

Request JSON:
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "s3cret123",
  "displayName": "Alice"
}

Success (200) - LoginResponseDTO:
{
  "token": "<jwt>",
  "id": 123,
  "username": "alice",
  "email": "alice@example.com",
  "displayName": "Alice",
  "avatarUrl": "https://...",
  "totalXp": 0,
  "level": 1,
  "currentStreak": 0,
  "roles": ["USER"]
}

Error (400) - username/email conflict:
{
  "success": false,
  "message": "Username already exists",
  "statusCode": 400
}

Curl:
```bash
curl -X POST {{base_url}}/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"s3cret123","displayName":"Alice"}'
```

---

## Login

POST /api/auth/login
Headers:
- `Content-Type: application/json`

Request JSON:
{
  "username": "alice",
  "password": "s3cret123"
}

Success (200) - LoginResponseDTO (same shape as register):
{
  "token": "<jwt>",
  "id": 123,
  "username": "alice",
  "email": "alice@example.com",
  "displayName": "Alice",
  "avatarUrl": "https://...",
  "totalXp": 0,
  "level": 1,
  "currentStreak": 0,
  "roles": ["USER"]
}

Curl:
```bash
curl -X POST {{base_url}}/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"s3cret123"}'
```

---

## Logout

POST /api/auth/logout
Headers:
- `Authorization: Bearer <token>`

Body: none required

Success (200):
{
  "message": "Logged out",
  "success": true
}

Curl:
```bash
curl -X POST {{base_url}}/api/auth/logout \
  -H "Authorization: Bearer <your-jwt-token>"
```

---

## Error format (consistent)

- Api errors (`ApiException`) → 400
{
  "message": "<error message>",
  "success": false,
  "statusCode": 400
}

- Unexpected errors → 500
{
  "message": "An unexpected error occurred: <msg>",
  "success": false,
  "statusCode": 500
}

---

## Notes
- Register returns a `LoginResponseDTO` and issues a JWT in the `token` field.
- `roles` field is a list of role names (e.g., `USER`, `ADMIN`, `EDITOR`).
- Protect admin endpoints by including `Authorization: Bearer <token>` and ensure the token corresponds to a user with `ROLE_ADMIN`.
- For server-side logout, tokens are blacklisted until they expire (in-memory blacklist).

---

If you want, I can also:
- export a Postman collection JSON for import,
- add example responses for failure cases (invalid password, validation errors), or
- add code snippets for frontend usage (axios/fetch).
