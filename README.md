# Translation Service

A microservice that translates document titles from English to Spanish using Google's Gemini API, part of a document management system.

## Features

- Asynchronous processing using Kafka
- Integration with Google Gemini API for translations
- Retry mechanism for failed translations
- Dead Letter Queue (DLQ) for unprocessable messages
- Comprehensive logging and error handling

## Technology Stack

This Translation Service uses:

- **Spring Boot**: Consistent with our other microservices (Authentication, Document Service)
- **Spring Kafka**: For message processing
- **Spring Retry**: For retry mechanisms
- **Google Gemini API**: For translation functionality via HTTP client
- **Lombok**: To reduce boilerplate code
- **Logback**: For structured logging

## Prerequisites

- Java 17+
- Maven or Gradle
- Kafka
- Google Gemini API key

## Setup and Installation

1. Clone the repository
2. Configure application properties
3. Build the project:
   ```bash
   # If using Maven 
   mvn clean install
   
   # If using Gradle 
   gradle build
   ```
4. Run the service:
   ```bash
   # If using Maven 
   mvn spring-boot:run
   
   # If using Gradle 
   gradle bootRun
   ```

## Configuration Properties

| Property | Description | Default |
|----------|-------------|---------|
| `server.port` | Port the service runs on | 8083 |
| `spring.profiles.active` | Active profile | development |
| `spring.kafka.bootstrap-servers` | Kafka brokers | localhost:9092 |
| `spring.kafka.consumer.group-id` | Kafka consumer group ID | translation-service-group |
| `app.kafka.topics.request` | Topic for translation requests | document.translation.request |
| `app.kafka.topics.response` | Topic for translation responses | document.translation.response |
| `app.kafka.topics.dlq` | Topic for dead letter queue | document.translation.dlq |
| `app.gemini.api-key` | Google Gemini API key | - |
| `app.retry.max-attempts` | Maximum retry attempts | 3 |
| `app.retry.delay-ms` | Delay between retries in milliseconds | 5000 |

## Message Formats

### Translation Request

```json
{
  "documentId": "string",
  "title": "string",
  "sourceLanguage": "string (default: en)",
  "targetLanguage": "string (default: es)"
}
```

### Translation Response

```json
{
  "documentId": "string",
  "originalTitle": "string",
  "translatedTitle": "string",
  "sourceLanguage": "string",
  "targetLanguage": "string",
  "status": "COMPLETED | FAILED",
  "error": "string (only if status is FAILED)",
  "timestamp": "ISO date string"
}
```

## REST API Endpoints

The service provides the following REST endpoints for direct interaction:

### Translate Text

```
POST /api/translate
```

Example request:
```bash
curl -X POST http://localhost:8083/api/translate \
     -H "Content-Type: application/json" \
     -d '{"documentId":"doc123","title":"Hello World","sourceLanguage":"en","targetLanguage":"es"}'
```

### List Available Models (Debugging)

```
GET /api/translate/models
```

Returns the list of available Gemini models for your API key.
