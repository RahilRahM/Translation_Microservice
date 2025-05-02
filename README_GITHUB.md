# Translation Microservice

A Spring Boot microservice that translates document titles using Google's Gemini AI, part of a document management system.

## Key Features

- **AI-Powered Translation**: Leverages Google Gemini 1.5 Flash for high-quality translations
- **Asynchronous Processing**: Uses Kafka for reliable message-based communication
- **Flexible Deployment**: Run with or without Kafka for development/production
- **Error Handling**: Built-in retry mechanism and Dead Letter Queue
- **REST API**: Direct translation endpoint for immediate results

## Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/RahilRahM/Translation_Microservice.git
   cd Translation_Microservice
   ```

2. **Set up environment variables**
   ```bash
   cp .env.example .env
   # Edit .env with your Gemini API key
   ```

3. **Run the service**
   ```bash
   mvn spring-boot:run
   ```

4. **Test the translation endpoint**
   ```bash
   curl -X POST http://localhost:8083/api/translate \
     -H "Content-Type: application/json" \
     -d '{"documentId":"doc123","title":"Hello World","sourceLanguage":"en","targetLanguage":"es"}'
   ```

## Configuration

See `application.yml` for configurable options including:
- Port settings
- Kafka configuration
- Retry parameters
- Logging levels

## API Reference

### Translate Document Title
