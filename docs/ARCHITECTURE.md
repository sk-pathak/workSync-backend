# workSync Backend Architecture

## Overview

Collaborative project & task management backend featuring:
- **Authentication**: Stateless JWT (BCrypt passwords)
- **Real-time**: WebSockets + STOMP for chat & notifications
- **Event-Driven**: Kafka (3 topics, 7 consumer groups)
- **Persistence**: PostgreSQL + Flyway migrations
- **Architecture**: Layered monolith (Controller → Service → Repository)

## Domain Model

**Core Entities** (UUID primary keys):
- **User**: Authentication principal, project owner/member, task assignee
- **Project**: Team workspace with members, tasks, chat room, notifications
- **Task**: Belongs to project, status workflow (TODO → IN_PROGRESS → DONE), priority, assignee
- **Chat**: One per project, contains messages
- **Message**: Chat messages with sender, timestamp, content
- **Notification**: Typed events (assignments, invitations) with JSONB payload, status tracking

**Enums**: Project/Task/Notification status, roles, priorities

## Event-Driven Architecture (Kafka)

Kafka decouples real-time delivery from persistence/analytics/notifications. Users see instant WebSocket responses while background consumers handle heavy processing independently.

### Topics & Consumers

| Topic | Producers | Consumer Groups | Purpose |
|-------|-----------|-----------------|---------|
| `chat-events` | ChatService | **3 groups** | ChatPersistenceConsumer (save DB), ChatAnalyticsConsumer (metrics), ChatNotificationConsumer (@mentions) |
| `task-events` | TaskService | **2 groups** | TaskAuditLogConsumer (compliance), TaskAnalyticsConsumer (metrics) |
| `notification-events` | NotificationService | **2 groups** | NotificationPersistenceConsumer (save DB), WebSocketNotificationConsumer (real-time push) |

### Chat Flow Example

```
Client → ChatController → ChatService
                             │
                    ┌────────┼────────┐
                    ↓                 ↓
              WebSocket           Kafka Topic
             (Immediate)         "chat-events"
                    │                  │
                    │         ┌────────┼────────┐
                    │         ↓        ↓        ↓
              Users see   Persistence Analytics Notifications
             message NOW    (save DB)  (metrics) (@mentions)
```

## Key Components

### Controllers
REST & WebSocket endpoints: validate DTOs, map entities ↔ DTOs, delegate to services.

### Services  
Business logic: ownership checks (cached), membership validation, task assignment rules, notification generation, Kafka publishing.

### Repositories
Spring Data JPA: CRUD + derived queries for all entities.

### WebSocket
- Endpoint: `/ws` (SockJS fallback)
- Prefixes: `/app` (client→server), `/topic` (broadcast), `/user` (user-specific queues)

## Persistence

- **Database**: PostgreSQL with JSONB for notification payloads
- **Migrations**: Flyway versioned SQL scripts (`V1__init_schema.sql`, etc.)
- **Validation**: `ddl-auto: validate` prevents schema drift

## Future Enhancements

**Short-term**:
- Add database indexes (`tasks(project_id, status)`, `notifications(recipient_id, status)`)
- API rate limiting

**Medium-term**:
- Redis for distributed caching (multi-node consistency)
- WebSocket message delivery receipts

**Long-term**:
- Microservice extraction (Chat, Notifications, Analytics)

**Environment Variables**:
- `JWT_SECRET`: Signing key for tokens
- `SPRING_DATASOURCE_URL`: PostgreSQL connection
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`: Kafka broker
