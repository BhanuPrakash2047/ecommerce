# Production Deployment Guide

## 🚀 Deploying Your Dual Authentication System to Production

This guide covers deploying your Spring Boot ecommerce application with JWT + OAuth2 authentication to production.

---

## ✅ Pre-Deployment Checklist

### Security
- [ ] Changed `jwt.secret` to a unique, secure 32+ character string
- [ ] All credentials are in environment variables (not hardcoded)
- [ ] HTTPS/SSL certificate obtained and configured
- [ ] Database password is strong and unique
- [ ] Google OAuth2 credentials are for production domain
- [ ] CORS origins updated to production frontend URL
- [ ] Removed debug logging from application properties

### Database
- [ ] MySQL database created and tested
- [ ] Database backups configured
- [ ] User with minimal required permissions created
- [ ] Connection pool configured (HikariCP)
- [ ] Flyway/Liquibase migrations prepared (if needed)

### Configuration
- [ ] `application-prod.properties` created
- [ ] Database connection pool size optimized
- [ ] JWT expiration set appropriately
- [ ] Rate limiting configured
- [ ] Error logging configured

### Testing
- [ ] All endpoints tested with real data
- [ ] JWT refresh tested
- [ ] OAuth2 Google login tested end-to-end
- [ ] Protected endpoints verified
- [ ] Role-based access tested
- [ ] CORS tested from frontend domain

### Documentation
- [ ] Deployment steps documented
- [ ] Rollback procedure documented
- [ ] Monitoring dashboard set up
- [ ] Alert thresholds configured

---

## 🔐 Production Security Configuration

### 1. Update application-prod.properties

```properties
# ============================================
# SERVER CONFIGURATION
# ============================================
server.port=8080
server.servlet.context-path=/
server.shutdown=graceful
server.error.include-message=never
server.error.include-stacktrace=never
server.error.include-exception=false

# ============================================
# HTTPS/SSL CONFIGURATION
# ============================================
server.ssl.enabled=true
server.ssl.key-store=${SSL_KEYSTORE_PATH}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
server.ssl.protocol=TLSv1.2
server.http2.enabled=true

# ============================================
# DATABASE CONFIGURATION
# ============================================
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Connection Pool Settings (HikariCP)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.auto-commit=true

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

# ============================================
# JWT CONFIGURATION
# ============================================
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

# ============================================
# OAUTH2 GOOGLE CONFIGURATION
# ============================================
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri=https://yourdomain.com/login/oauth2/code/google
spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/v2/auth
spring.security.oauth2.client.provider.google.token-uri=https://www.googleapis.com/oauth2/v4/token
spring.security.oauth2.client.provider.google.user-info-uri=https://www.googleapis.com/oauth2/v1/userinfo
spring.security.oauth2.client.provider.google.user-name-attribute=sub

# ============================================
# LOGGING
# ============================================
logging.level.root=WARN
logging.level.com.snackecommerce=INFO
logging.level.org.springframework.security=WARN
logging.level.org.springframework.web=WARN
logging.file.name=/var/log/ecommerce/application.log
logging.file.max-size=10MB
logging.file.max-history=10
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# ============================================
# ACTUATOR (MONITORING)
# ============================================
management.endpoints.web.exposure.include=health,metrics
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true
```

### 2. Environment Variables Setup

Create `.env` file or set in your deployment platform:

```bash
# Database
DB_URL=jdbc:mysql://db-host:3306/snack_ecommerce
DB_USERNAME=ecommerce_user
DB_PASSWORD=secure_password_here

# JWT
JWT_SECRET=generate-with-openssl-rand-base64-32
JWT_EXPIRATION=86400000

# Google OAuth2
GOOGLE_CLIENT_ID=your-production-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-production-secret

# SSL/TLS
SSL_KEYSTORE_PATH=/etc/ecommerce/keystore.p12
SSL_KEYSTORE_PASSWORD=keystore_password
```

### 3. Generate JWT Secret

```bash
# On Linux/Mac
openssl rand -base64 32

# On Windows PowerShell
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

---

## 🐳 Docker Deployment

### Dockerfile

```dockerfile
FROM maven:3.8-openjdk-17 as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# Create non-root user
RUN useradd -m -u 1000 appuser && \
    chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", \
            "-Dspring.config.location=classpath:/application-prod.properties", \
            "-jar", \
            "app.jar"]
```

### docker-compose.yml

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_URL: jdbc:mysql://mysql:3306/snack_ecommerce
      DB_USERNAME: ecommerce_user
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID}
      GOOGLE_CLIENT_SECRET: ${GOOGLE_CLIENT_SECRET}
    depends_on:
      - mysql
    networks:
      - ecommerce
    restart: unless-stopped

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: snack_ecommerce
      MYSQL_USER: ecommerce_user
      MYSQL_PASSWORD: ${DB_PASSWORD}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - ecommerce
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/nginx/ssl:ro
    depends_on:
      - app
    networks:
      - ecommerce
    restart: unless-stopped

volumes:
  mysql_data:

networks:
  ecommerce:
    driver: bridge
```

### nginx.conf

```nginx
upstream backend {
    server app:8080;
}

server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com www.yourdomain.com;

    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    client_max_body_size 10M;

    location / {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # CORS headers (if not handled by Spring)
        add_header 'Access-Control-Allow-Origin' 'https://yourdomain.com' always;
        add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS' always;
        add_header 'Access-Control-Allow-Headers' 'Content-Type, Authorization' always;
        
        if ($request_method = 'OPTIONS') {
            return 204;
        }
    }

    location /health {
        proxy_pass http://backend/actuator/health;
        access_log off;
    }
}
```

---

## 🚀 Cloud Deployment Platforms

### AWS EC2

```bash
# 1. SSH into instance
ssh -i your-key.pem ec2-user@your-instance-ip

# 2. Install Java and MySQL
sudo yum update -y
sudo yum install java-17-amazon-corretto-headless -y
sudo yum install mysql80-server -y

# 3. Create application user
sudo useradd -m -s /sbin/nologin ecommerce

# 4. Deploy JAR
sudo mkdir -p /opt/ecommerce
sudo chown ecommerce:ecommerce /opt/ecommerce
scp -i your-key.pem target/snack-ecommerce-0.0.1.jar ec2-user@your-instance-ip:/home/ec2-user/

# 5. Move and set permissions
sudo mv /home/ec2-user/snack-ecommerce-0.0.1.jar /opt/ecommerce/
sudo chown ecommerce:ecommerce /opt/ecommerce/snack-ecommerce-0.0.1.jar

# 6. Create systemd service
sudo tee /etc/systemd/system/ecommerce.service > /dev/null <<EOF
[Unit]
Description=Snack Ecommerce Application
After=network.target

[Service]
Type=simple
User=ecommerce
WorkingDirectory=/opt/ecommerce
EnvironmentFile=/opt/ecommerce/.env
ExecStart=/usr/bin/java -jar snack-ecommerce-0.0.1.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 7. Start service
sudo systemctl daemon-reload
sudo systemctl enable ecommerce
sudo systemctl start ecommerce
sudo systemctl status ecommerce
```

### Heroku

```bash
# 1. Install Heroku CLI
curl https://cli-assets.heroku.com/install.sh | sh

# 2. Login
heroku login

# 3. Create app
heroku create snack-ecommerce

# 4. Set environment variables
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set JWT_SECRET="your-secure-secret"
heroku config:set GOOGLE_CLIENT_ID="your-id"
heroku config:set GOOGLE_CLIENT_SECRET="your-secret"

# 5. Create Procfile
echo "web: java -Dserver.port=\$PORT \$JAVA_OPTS -jar target/snack-ecommerce-0.0.1.jar" > Procfile

# 6. Deploy
git push heroku main
```

### Google Cloud Run

```bash
# 1. Build image
gcloud builds submit --tag gcr.io/PROJECT_ID/snack-ecommerce

# 2. Deploy
gcloud run deploy snack-ecommerce \
  --image gcr.io/PROJECT_ID/snack-ecommerce \
  --platform managed \
  --region us-central1 \
  --memory 512Mi \
  --cpu 1 \
  --set-env-vars SPRING_PROFILES_ACTIVE=prod,JWT_SECRET=secret,...
```

---

## 📊 Monitoring & Logging

### Metrics with Prometheus

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'ecommerce'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
```

### Logging Aggregation (ELK Stack)

```yaml
version: '3'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.0.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"

  kibana:
    image: docker.elastic.co/kibana/kibana:8.0.0
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch

  logstash:
    image: docker.elastic.co/logstash/logstash:8.0.0
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    ports:
      - "5000:5000/udp"
    depends_on:
      - elasticsearch
```

---

## 🔄 Backup & Disaster Recovery

### Database Backup Strategy

```bash
#!/bin/bash
# Daily backup script

BACKUP_DIR="/var/backups/mysql"
DB_NAME="snack_ecommerce"
DB_USER="root"
RETENTION_DAYS=30

# Create backup
mysqldump -u $DB_USER -p $DB_NAME | \
  gzip > $BACKUP_DIR/backup_$(date +%Y%m%d_%H%M%S).sql.gz

# Delete old backups
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +$RETENTION_DAYS -delete

# Upload to S3
aws s3 cp $BACKUP_DIR/ s3://your-backup-bucket/ --recursive
```

### Schedule with cron:
```bash
# Backup every day at 2 AM
0 2 * * * /path/to/backup.sh
```

---

## 🚨 Alert Configuration

### Email Alerts for Failures

Configure in application-prod.properties:
```properties
# Alert service
alert.enabled=true
alert.email.to=admin@yourdomain.com
alert.thresholds.error-rate=0.05
alert.thresholds.response-time=5000
```

### Key Alerts

- [ ] Authentication failures > 10/minute
- [ ] Database connection pool exhausted
- [ ] JWT validation errors > threshold
- [ ] API response time > 5 seconds
- [ ] Server disk space < 20%
- [ ] Memory usage > 80%

---

## 📋 Post-Deployment Verification

### 1. Health Checks

```bash
# Check application health
curl https://yourdomain.com/actuator/health

# Expected response:
# {"status":"UP","components":{"db":{"status":"UP"},...}}
```

### 2. Authentication Tests

```bash
# Test JWT login
curl -X POST https://yourdomain.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"pass"}'

# Test OAuth2 redirect
curl -L https://yourdomain.com/oauth2/authorization/google

# Test protected endpoint
curl https://yourdomain.com/api/auth/profile \
  -H "Authorization: Bearer <token>"
```

### 3. Database Connectivity

```bash
# Verify connection from app logs
tail -f /var/log/ecommerce/application.log | grep "Hibernate"
```

---

## 🔄 Deployment Rollback Procedure

### If Deployment Fails

```bash
# 1. Check service status
sudo systemctl status ecommerce

# 2. View logs
sudo journalctl -u ecommerce -n 100 --no-pager

# 3. Rollback to previous version
sudo cp /opt/ecommerce/backup/previous.jar /opt/ecommerce/snack-ecommerce-0.0.1.jar
sudo systemctl restart ecommerce

# 4. Verify
curl https://yourdomain.com/actuator/health
```

---

## 📈 Performance Optimization

### Database Connection Pool Tuning

```properties
# For 100 concurrent users
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

### Cache Configuration

```java
// Add to SecurityConfig
@Bean
public CacheManager cacheManager() {
    return new ConcurrentMapCacheManager("users", "tokens");
}
```

---

## 📝 Deployment Checklist

- [ ] Production credentials configured
- [ ] Database backup tested
- [ ] SSL certificates configured
- [ ] Monitoring and logging setup
- [ ] Alert thresholds configured
- [ ] Rollback plan documented
- [ ] Team trained on deployment
- [ ] Documentation updated
- [ ] Health checks passing
- [ ] Load testing completed
- [ ] Security audit completed
- [ ] HTTPS enforced
- [ ] CORS properly configured
- [ ] Database optimized
- [ ] Connection pools tuned

---

## 🎯 Go-Live Checklist

- [ ] Load test completed successfully
- [ ] Security penetration testing done
- [ ] Backup and recovery tested
- [ ] Team on-call for 24 hours
- [ ] Customer support trained
- [ ] Status page updated
- [ ] Communication plan executed

---

**Your application is production-ready! 🚀**

After deployment, monitor closely for the first 24-48 hours and be ready to rollback if needed.

