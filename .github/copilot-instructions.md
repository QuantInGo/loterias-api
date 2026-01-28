# Copilot Instructions for Loterias API

## Overview

The **Loterias API** is a Java-based application that provides results for various lottery games from CAIXA. The architecture is designed around a microservices approach, with clear boundaries between components such as consumers, services, and REST controllers.

## Architecture

- **Main Components**:
  - **Consumer**: Responsible for fetching data from external sources (e.g., CAIXA API).
    - Key file: `src/main/java/com/gutotech/loteriasapi/consumer/Consumer.java`
  - **Service**: Contains business logic and interacts with repositories.
    - Key file: `src/main/java/com/gutotech/loteriasapi/service/ResultadoService.java`
  - **REST Controllers**: Handle incoming API requests and responses.
    - Key file: `src/main/java/com/gutotech/loteriasapi/rest/ApiRestController.java`
  
- **Data Flow**: 
  - The application fetches lottery results from the CAIXA API, processes the data, and serves it through its own REST endpoints.

## Developer Workflows

- **Building the Project**: Use Maven commands to build the project.
  - Command: `./mvnw clean install`
  
- **Running Tests**: Execute unit tests to ensure code quality.
  - Command: `./mvnw test`
  
- **Debugging**: Utilize your IDE's debugging tools to set breakpoints in the service or consumer classes.

## Project-Specific Conventions

- **Logging**: Use SLF4J for logging throughout the application.
  - Example: 
    ```java
    logger.info("Fetching from CAIXA: {}", url);
    ```

- **Error Handling**: Throw specific exceptions for different error scenarios.
  - Example:
    ```java
    throw new RuntimeException("CAIXA returned non-JSON response");
    ```

- **Configuration**: Application properties are managed in `src/main/resources/application.properties` and its variants for different environments.

## Integration Points

- **External Dependencies**: The application relies on the CAIXA API for lottery data.
  - Base URL: `https://servicebus2.caixa.gov.br/portaldeloterias/api/`
  
- **Cross-Component Communication**: 
  - The consumer classes communicate with external APIs and pass data to services for processing.

## Examples

- **Fetching Latest Lottery Results**:
  - Endpoint: `GET /api/<loteria>/latest`
  - Example response:
    ```json
    [
      "maismilionaria",
      "megasena",
      "lotofacil",
      ...
    ]
    ```

## Conclusion

This document serves as a foundational guide for AI agents to navigate and contribute effectively to the Loterias API project. For further details, refer to the specific classes and methods mentioned above.
