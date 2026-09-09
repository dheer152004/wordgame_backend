# wordgame_backend

Spring Boot backend for the WordGame application.

## AWS configuration

Use the `aws` Spring profile in AWS deployments:

```text
AWS_REGION=ap-south-1
S3_BUCKET=your-bucket
DB_URL=jdbc:postgresql://your-rds-host:5432/wordgame?sslmode=require
DB_USERNAME=your-db-user
DB_PASSWORD=your-db-password
MAIL_FROM=verified-sender@example.com
JWT_SECRET_KEY=your-production-jwt-secret
```

S3 media currently returns direct S3 object URLs. It uses `DefaultCredentialsProvider`, so Lambda, ECS, or EC2 should use an IAM role instead of access keys. The role needs S3 read/write/delete permissions and SES `ses:SendEmail` permission.

The API Gateway Lambda handler is:

```text
com.example.WordGame.StreamLambdaHandler::handleRequest
```

The SES sender must be verified in the selected AWS region. New SES accounts may remain in the SES sandbox until production access is approved.

The application requires PostgreSQL connection variables before startup.
