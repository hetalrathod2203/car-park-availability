# Car Park Availability API

A Spring Boot API application that returns the closest car parks to a user with real-time availability information.

## Approach

The application follows a layered architecture with:
- **Entity Layer**: JPA entities for car park information and availability data
- **Repository Layer**: Data access using Spring Data JPA with custom queries
- **Service Layer**: Business logic for data processing and coordinate conversion
- **Controller Layer**: REST API endpoints with validation
- **Configuration**: WebClient for external API calls and global exception handling

Key features:
- Distance calculation using Haversine formula in SQL
- Coordinate conversion from SVY21 to WGS84 using OneMap API
- **Multithreaded CSV processing** for faster data imports (configurable thread pool)
- Pagination support for large datasets
- Input validation and error handling
- Upsert functionality for availability data updates
- Database schema management with Liquibase

## Database Schema

Database schema is managed by Liquibase with versioned migrations in `src/main/resources/db/changelog/`.

### car_parks
- `car_park_no` (PK): Unique identifier
- `address`: Car park location
- `x_coord`, `y_coord`: SVY21 coordinates
- `latitude`, `longitude`: WGS84 coordinates (indexed for performance)
- Additional metadata fields

### car_park_availability
- `id` (PK): Auto-generated ID
- `car_park_no`: Foreign key reference
- `total_lots`, `available_lots`: Parking capacity info
- `lot_type`: Type of parking lot
- `update_datetime`: Timestamp from API (indexed with car_park_no)
- `created_at`: Record creation time

## Setup Instructions

### Prerequisites
- Java 17+
- Docker & Docker Compose
- PostgreSQL (if running locally)

### Using Docker Compose (Recommended)

1. Build the application:
```bash
./gradlew build
```

2. Start services:
```bash
docker-compose up -d
```

3. Import car park data:
```bash
curl -X POST -F "file=@hdb-carpark-information.csv" http://localhost:8090/admin/import-carparks
```

4. Update availability data:
```bash
curl -X POST http://localhost:8090/admin/update-availability
```

### Local Development

1. Start PostgreSQL database
2. Update `application.yml` with your database credentials
3. Run the application:
```bash
./gradlew bootRun
```

## API Endpoints

### 🚗 Car Park Search

#### Get Nearest Car Parks
```bash
# Basic search
curl "http://localhost:8090/carparks/nearest?latitude=1.37326&longitude=103.897"

# With pagination
curl "http://localhost:8090/carparks/nearest?latitude=1.37326&longitude=103.897&page=1&per_page=5"
```

**Parameters:**
- `latitude` (required): User latitude (-90 to 90)
- `longitude` (required): User longitude (-180 to 180) 
- `page` (optional): Page number (default: 1)
- `per_page` (optional): Results per page (default: 10)

**Response:**
```json
[
  {
    "address": "BLK 401-413, 460-463 HOUGANG AVENUE 10",
    "latitude": 1.37429,
    "longitude": 103.896,
    "total_lots": 693,
    "available_lots": 182
  }
]
```

### 🔧 Admin Endpoints

#### Import Car Park Data
```bash
# Import CSV file with car park information
curl -X POST \
  -F "file=@hdb-carpark-information.csv" \
  http://localhost:8090/admin/import-carparks
```

**Features:**
- **Multithreaded processing** with configurable thread pool (default: 10 threads)
- Automatic coordinate conversion from SVY21 to WGS84 using OneMap API
- Only saves records with successful coordinate conversion
- Progress logging every 100 records
- Thread-safe atomic counters for accurate progress tracking

#### Update Availability Data
```bash
# Fetch latest availability from Singapore government API
curl -X POST http://localhost:8090/admin/update-availability
```

**Features:**
- Upserts availability data (updates existing, creates new)
- Only processes car parks that exist in database
- Logs skipped unknown car parks
- Handles multiple lot types per car park

## 📚 API Documentation

### Swagger UI
Interactive API documentation with testing capabilities:
```
http://localhost:8090/swagger-ui.html
```

### OpenAPI Specification
JSON specification for API integration:
```
http://localhost:8090/v3/api-docs
```

## 🧪 Testing Examples

### Complete Workflow
```bash
# 1. Start the application
docker-compose up -d

# 2. Import car park data
curl -X POST \
  -F "file=@hdb-carpark-information.csv" \
  http://localhost:8090/admin/import-carparks

# 3. Update availability data
curl -X POST http://localhost:8090/admin/update-availability

# 4. Search for nearest car parks
curl "http://localhost:8090/carparks/nearest?latitude=1.37326&longitude=103.897&per_page=3"
```

### Sample Locations for Testing
```bash
# Hougang area
curl "http://localhost:8090/carparks/nearest?latitude=1.37326&longitude=103.897"

# Orchard Road area  
curl "http://localhost:8090/carparks/nearest?latitude=1.3048&longitude=103.8318"

# Marina Bay area
curl "http://localhost:8090/carparks/nearest?latitude=1.2966&longitude=103.8547"
```

## Trade-offs & Observations

### Database Choice
**PostgreSQL** was chosen for:
- Excellent geospatial support for distance calculations
- ACID compliance for data consistency
- Mature ecosystem and performance optimization

### Performance Considerations
- **Distance calculation in SQL**: Reduces data transfer and leverages database indexing
- **Pagination**: Prevents memory issues with large datasets
- **Latest availability query**: Optimized to fetch most recent data efficiently
- **Multithreaded CSV import**: Parallel processing with configurable thread pool (10 threads default)
- **Concurrent API calls**: OneMap coordinate conversion happens in parallel

### Scalability
- **Stateless design**: Easy horizontal scaling
- **Database connection pooling**: Efficient resource utilization
- **Async external API calls**: Non-blocking operations using WebClient
- **Configurable threading**: Thread pool size adjustable via `app.threading.csv-import.pool-size`

## 🔍 Health Checks

```bash
# Application health
curl http://localhost:8090/actuator/health

# Database connectivity
curl http://localhost:8090/actuator/health/db
```

## 📊 Monitoring

```bash
# Application metrics
curl http://localhost:8090/actuator/metrics

# JVM info
curl http://localhost:8090/actuator/info
```

### Future Improvements
- Implement Redis caching for frequently accessed data
- Add database indexing on latitude/longitude for faster queries  
- Implement circuit breaker pattern for external API resilience
- Add comprehensive monitoring and metrics
- Implement rate limiting for API endpoints
- Add real-time WebSocket updates for availability changes
- Implement geofencing for location-based notifications