# Application Properties Template
# ================================
# Copy this file to src/main/resources/application.properties 
# and fill in your actual values (do NOT commit to git)

# For local development, use environment variables or edit the file locally

## JWT Secret
## Generate a random string at least 32 characters:
## jwt.secret=generate_random_string_here_min_32_characters

## Google OAuth Credentials
## Get from: https://console.cloud.google.com/
## 1. Create project
## 2. Create OAuth 2.0 Client ID (Web Application)
## 3. Add redirect URI: http://localhost:8080/login/oauth2/code/google

# spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
# spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET

## Database (update credentials as needed)
# spring.datasource.password=YOUR_MYSQL_PASSWORD
