# Translation Service

A microservice that translates document titles using Google's Gemini API as part of the Document Management System.

## Features

- Translation of document titles from English to Spanish
- Communication with Document Service via Kafka
- Integration with Google's Gemini API
- Error handling with automatic retries
- Logging for monitoring
- REST API for direct translation requests

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Kafka** for message processing
- **Google Gemini API** for translations
- **Maven** for dependency management and builds

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Running Kafka instance (local or remote)
- Google Gemini API key

## Quick Start

1. Clone the repository
2. Create a `.env` file in the project root with your Gemini API key:
   ```
   GEMINI_API_KEY=your_api_key_here
   ```
3. Build the project:
   ```bash
   mvn clean install
   ```
4. Run the service:
   ```bash
   mvn spring-boot:run
   ```

## Configuration

The service runs on port 8083 by default and connects to Kafka on localhost:9092. You can modify these settings in the `application.yml` file.

## Kafka Topics

The service uses the following Kafka topics:
- **translation-topic**: Receives translation requests from Document Service
- **document.translation.response**: Sends translation results back to Document Service

## API Endpoints

For direct testing without using Kafka:

```
POST /api/translate
```

This endpoint is accessible through the API Gateway at `http://localhost:8083/api/translate`.

Example request:
```bash
curl -X POST http://localhost:8083/api/translate \
  -H "Content-Type: application/json" \
  -d '{"title":"Annual Financial Report", "sourceLanguage":"en", "targetLanguage":"es"}'
```

## Message Flow

1. Document Service publishes a message to `translation-topic` with document ID and title
2. Translation Service consumes the message
3. Translation Service calls Gemini API to translate the title
4. Translation Service sends the translated title back to Document Service via `document.translation.response`
5. Document Service updates the document with the translated title

## Environment Variables

| Name | Description | Required |
|------|-------------|----------|
| GEMINI_API_KEY | Google Gemini API key | Yes |
| SPRING_KAFKA_BOOTSTRAP_SERVERS | Kafka broker addresses | No (default: localhost:9092) |
