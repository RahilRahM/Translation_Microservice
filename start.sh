#!/bin/bash
# Load environment variables from .env file
export $(grep -v '^#' .env | xargs)
# Start the application
mvn spring-boot:run
