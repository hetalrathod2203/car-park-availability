# LLM Prompt History

This document contains representative prompts and responses that influenced the Car Park Availability API solution.

## P1: Database Schema Design
**Prompt:** "Design database schema for car park information and availability data with proper relationships"

**Response:** Proposed two-table design with car_parks (static data) and car_park_availability (dynamic data) with foreign key relationship.

**Validation:** Reviewed normalization principles and confirmed separation of concerns between static and dynamic data.

## P2: Distance Calculation Optimization
**Prompt:** "Can we make the haversine formula faster? Can you also add indexing on latitude and longitude"

**Response:** Suggested replacing Haversine with Euclidean distance using bounding box pre-filtering and composite B-tree indexes.

**Validation:** Tested performance improvement (~10x faster) and verified accuracy for Singapore's small geographic area.

## P3: Coordinate Conversion Implementation
**Prompt:** "Add coordinate conversion from SVY21 to WGS84 using OneMap API with authentication"

**Response:** Implemented OneMap API integration with token-based authentication and proper error handling.

**Validation:** Tested with real Singapore coordinates and verified conversion accuracy against known reference points.

## P4: Docker Build Optimization
**Prompt:** "Can we add ./gradlew build directly in docker itself so that one does docker-compose up and everything starts working"

**Response:** Modified Dockerfile to build application inside container and updated docker-compose for one-command deployment.

**Validation:** Tested complete build and deployment process from clean environment.

## P5: JUnit Test Cases Generation
**Prompt:** "Generate JUnit test cases for the CarParkService and CarParkController with proper mocking"

**Response:** Generated comprehensive JUnit 5 test cases including:
- Unit tests for CarParkService with MockBean annotations
- Controller tests using @WebMvcTest and MockMvc
- Test scenarios for valid/invalid coordinates, pagination, and error cases
- Proper mocking of repository layers and external dependencies
- Assertions for response structure and HTTP status codes

**Validation:** Reviewed test coverage, verified mocking strategies align with Spring Boot testing best practices, and ensured tests cover both happy path and edge cases.
---

## Code Validation Methods

1. **Manual Testing:** All API endpoints tested with curl commands and various input scenarios
2. **Performance Benchmarking:** Distance calculation optimization verified with timing comparisons
3. **Database Verification:** Schema creation and data integrity confirmed through PostgreSQL queries
4. **Integration Testing:** End-to-end workflow tested from CSV import to API queries
5. **Error Scenario Testing:** Invalid inputs and edge cases verified for proper error handling
6. **Documentation Review:** All implementations cross-referenced with official Spring Boot and PostgreSQL documentation