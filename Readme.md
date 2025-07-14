# workSync Backend

A scalable, production-grade backend for **workSync** — a project management platform enabling users to create, join, and collaborate on projects with real-time chat, task tracking, notifications, and GitHub analytics.

---

## Features
- **JWT Authentication & RBAC**: Secure endpoints with role-based access control (Spring Security, JWT)
- **User & Project Management**: CRUD, join, star/save, and team membership workflows
- **Task System**: Project-based task CRUD, assignment, and status tracking
- **Notifications**: Real-time notifications for join requests, approvals, and task assignments
- **Real-Time Chat**: Kafka + WebSocket-powered team chat for each project
- **GitHub Analytics**: Fetch project insights via the GitHub API
- **Pagination & Filtering**: All list endpoints support pagination
- **Clean Architecture**: DTOs, mappers, services, repositories, and controllers
- **PostgreSQL**: Robust relational data storage
- **Dockerized**: Easy local development and deployment

---

## Tech Stack
- Java 17, Spring Boot 3
- Spring Security, JWT
- Spring Data JPA (PostgreSQL)
- MapStruct (DTO mapping)
- Spring WebSocket (STOMP)
- Apache Kafka
- Flyway (DB migrations)
- Docker & Docker Compose

---

## Getting Started

### Prerequisites
- Java 17
- Docker & Docker Compose

### Setup
1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-org/workSync-backend.git
   cd workSync-backend
   ```
2. **Configure environment variables:**
    - Copy `.env.example` to `.env` and set DB, Kafka, and JWT secrets as needed.
3. **Compile project:**
    ```bash
    chmod +x mvnw
    ./mvnw clean package -DskipTests
    ```
4. **Start services:**
   ```bash
   docker-compose up --build
   ```
   This will start PostgreSQL, Kafka, and the Spring Boot app.

### DB Migrations
To run database migrations, add sql file in ./src/main/resources/db/migration. Follow correct naming convention `V10__{name}.sql`. Then, restart docker compose. For local, `./mvnw flyway:migrate`

---

## API Overview

### Authentication
- `POST /api/auth/register` — Register a new user
- `POST /api/auth/login` — Login and receive JWT
- `POST /api/auth/logout` — Logout
- `GET /api/auth/admin-only` — Admin-only test endpoint
- `GET /api/auth/user-only` — User-only test endpoint

### Users
- `GET /api/users/me` — Get current user profile
- `PUT /api/users/me` — Update profile
- `GET /api/users` — List users
- `GET /api/users/{id}` — Get user by ID
- `PUT /api/users/{id}` — Update user by ID
- `DELETE /api/users/{id}` — Delete user by ID
- `GET /api/users/me/projects/owned` — List projects owned by current user
- `GET /api/users/me/projects/joined` — List projects joined by current user
- `GET /api/users/me/projects/starred` — List projects starred by current user

### Projects
- `GET /api/projects` — List/search projects
- `GET /api/projects/filtered` — List projects with filters (status, owned by me, member of, starred)
- `GET /api/projects/{id}` — Get project by ID
- `POST /api/projects` — Create project
- `PUT /api/projects/{id}` — Update project
- `DELETE /api/projects/{id}` — Delete project
- `POST /api/projects/{id}/join` — Request to join
- `POST /api/projects/{id}/leave` — Leave project
- `POST /api/projects/{id}/star` — Star/save project
- `POST /api/projects/{id}/unstar` — Unstar project
- `GET /api/projects/{id}/starred` — Check if current user has starred this project
- `GET /api/projects/{id}/membership` — Check membership status
- `GET /api/projects/{id}/members` — List members
- `POST /api/projects/{id}/members/{userId}/approve` — Approve join
- `DELETE /api/projects/{id}/members/{userId}/remove` — Remove member
- `GET /api/projects/{id}/tasks` — List tasks
- `GET /api/projects/{id}/github-analytics` — GitHub repo analytics
- `GET /api/projects/cache-stats` — Get cache stats

### Tasks
- `GET /api/projects/{projectId}/tasks` — List tasks
- `POST /api/projects/{projectId}/tasks` — Create task
- `PUT /api/projects/{projectId}/tasks/{taskId}` — Update task
- `DELETE /api/projects/{projectId}/tasks/{taskId}` — Delete task
- `POST /api/projects/{projectId}/tasks/{taskId}/assign/{userId}` — Assign task
- `POST /api/projects/{projectId}/tasks/{taskId}/status` — Update status

### Notifications
- `GET /api/notifications` — List notifications
- `GET /api/notifications/dismissed` — List dismissed notifications
- `GET /api/notifications/unread-count` — Get unread notification count
- `POST /api/notifications/{id}/read` — Mark as read
- `POST /api/notifications/{id}/dismiss` — Dismiss
- `POST /api/notifications/mark-all-read` — Mark all as read
- `POST /api/notifications/dismiss-all` — Dismiss all
- `GET /api/notifications/test` — Test endpoint

### Chat
- **WebSocket:** Connect to `/ws` (STOMP, JWT required)
- **Send message:** `/app/chat.send/{chatId}`
- **Subscribe:** `/topic/chat/{chatId}`
- **History:** `GET /api/chats/{chatId}/messages`

---

## Security
- All endpoints (except `/api/auth/*` and public project listing) require JWT authentication.
- Role-based access enforced via `@PreAuthorize` and service checks.
- WebSocket connections require JWT in the handshake.
