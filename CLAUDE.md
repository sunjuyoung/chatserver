# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a real-time chat server built with Spring Boot 3.x, WebSocket (STOMP), JWT authentication, and Spring Data JPA. The application provides REST APIs for user management and WebSocket connections for real-time messaging.

## Architecture

### Core Technologies
- **Java 17** - Runtime environment
- **Spring Boot 3.4.10** - Main framework
- **Spring WebSocket (STOMP)** - Real-time messaging
- **Spring Security + JWT** - Authentication and authorization
- **Spring Data JPA** - Data persistence
- **MySQL** - Primary database
- **Redis** - Session/token storage
- **Gradle** - Build tool

### Package Structure
```
com.sun.chatserver
├── chat/                    # Chat functionality
│   ├── common/domain/       # Shared domain entities (BaseTimeEntity)
│   ├── config/             # WebSocket configuration (STOMP)
│   ├── controller/         # Chat REST and WebSocket controllers
│   ├── domain/             # Chat entities (ChatRoom, ChatMessage, etc.)
│   ├── dto/                # Data transfer objects
│   ├── repository/         # JPA repositories
│   └── service/            # Business logic
└── member/                 # User management
    ├── controller/         # Authentication REST controllers
    ├── domain/             # Member entity and Role enum
    ├── dto/                # Auth/member DTOs
    ├── repository/         # Member repository
    ├── security/           # Security configuration and JWT
    └── service/            # Member and token services
```

### Key Configuration
- **WebSocket Endpoint**: `/connect` (with SockJS fallback)
- **STOMP Destinations**:
  - Subscribe: `/topic/*` (receive messages)
  - Publish: `/publish/*` (send messages)
- **CORS**: Configured for `http://localhost:3001`
- **Authentication**: JWT-based with refresh tokens stored in Redis

## Development Commands

### Build and Run
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test
```

### Database Setup
- **MySQL**: Port 3377, database: `chat_server`
- **Redis**: Port 6379 (localhost)
- **JPA**: DDL mode set to `validate` (ensure database schema exists)

## Key Implementation Details

### Authentication Flow
1. User signs up/in via REST API (`/api/members/signup`, `/api/members/signin`)
2. JWT tokens issued with 1-hour expiration
3. Refresh tokens stored in Redis
4. WebSocket connections authenticated via `StompHandler` interceptor

### WebSocket Architecture
- **StompWebSocketConfig**: Main WebSocket configuration
- **StompHandler**: Authentication interceptor for WebSocket connections
- **StompEventListener**: Connection lifecycle management
- **StompController**: Handles incoming STOMP messages

### Domain Relationships
- **Member**: Base user entity with role-based access
- **ChatRoom**: Supports GROUP/PRIVATE types with max member limits
- **ChatRoomMember**: Junction entity for room membership
- **ChatMessage**: Messages within rooms
- **ReadStatus**: Tracks message read status per user

### Security Configuration
- JWT filter applied before UsernamePasswordAuthenticationFilter
- Stateless session management
- Public endpoints: signup, signin, refresh, WebSocket connect
- All other endpoints require authentication

## Common Development Patterns

### Entity Design
- All domain entities extend `BaseTimeEntity` for audit fields
- Use `@Builder` pattern for entity creation
- Database indexes defined at entity level for performance

### DTO Conventions
- Request DTOs: `*RequestDto` or `*Dto` for incoming data
- Response DTOs: `*ResponseDto` or `*ResDto` for outgoing data
- Use `@JsonProperty` for field mapping when needed

### Repository Layer
- Extend `JpaRepository<Entity, ID>`
- Custom queries use `@Query` annotations
- Projection interfaces for optimized queries (e.g., `ChatRoomListProjection`)

### Service Layer
- Business logic separated from controllers
- Transaction management via `@Transactional`
- Separate services for different domains (chat, member, tokens)

## Development Guidelines

### When Adding New Features
1. Follow existing package structure (`controller` → `service` → `repository`)
2. Create appropriate DTOs for request/response mapping
3. Add database indexes for new query patterns
4. Implement proper error handling and validation
5. Consider WebSocket message broadcasting for real-time features

### Security Considerations
- Never expose sensitive data in DTOs (e.g., passwords, internal IDs)
- Validate all incoming data at controller level
- Use proper JWT token validation for WebSocket connections
- Implement rate limiting for API endpoints if needed

### Database Migrations
- DDL mode is `validate` - schema changes require manual migration
- Add appropriate indexes for query performance
- Consider foreign key constraints and cascading operations

## Testing
- Use `@SpringBootTest` for integration tests
- `@WebMvcTest` for controller layer testing
- `@DataJpaTest` for repository testing
- Test WebSocket functionality with `@TestConfiguration`