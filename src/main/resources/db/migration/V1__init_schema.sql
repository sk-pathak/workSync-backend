CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE project_status AS ENUM ('PLANNED', 'ACTIVE', 'COMPLETED', 'ON_HOLD', 'CANCELLED');
CREATE TYPE task_status AS ENUM ('TODO', 'IN_PROGRESS', 'DONE', 'BLOCKED');
CREATE TYPE notification_type AS ENUM ('JOIN_REQUEST', 'TASK_ASSIGNED', 'PROJECT_UPDATED');
CREATE TYPE notification_status AS ENUM ('PENDING', 'READ', 'DISMISSED');

CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(50) UNIQUE NOT NULL,
                       description TEXT
);

CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_profiles (
                               user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                               full_name VARCHAR(255),
                               avatar_url TEXT,
                               bio TEXT,
                               location VARCHAR(255),
                               created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_roles (
                            user_id UUID REFERENCES users(id) ON DELETE CASCADE,
                            role_id INT REFERENCES roles(id) ON DELETE CASCADE,
                            assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                            PRIMARY KEY(user_id, role_id)
);

CREATE TABLE projects (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          status project_status NOT NULL DEFAULT 'PLANNED',
                          is_public BOOLEAN NOT NULL DEFAULT FALSE,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE project_stars (
                               user_id UUID REFERENCES users(id) ON DELETE CASCADE,
                               project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
                               starred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                               PRIMARY KEY(user_id, project_id)
);

CREATE TABLE project_members (
                                 project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
                                 user_id UUID REFERENCES users(id) ON DELETE CASCADE,
                                 role_id INT REFERENCES roles(id),   -- e.g. MEMBER role
                                 joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 PRIMARY KEY(project_id, user_id)
);

CREATE TABLE notifications (
                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               sender_id UUID REFERENCES users(id) ON DELETE SET NULL,
                               project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
                               type notification_type NOT NULL,
                               status notification_status NOT NULL DEFAULT 'PENDING',
                               payload JSONB,  -- extra data (e.g. message, task id, etc.)
                               created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE tasks (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
                       creator_id UUID NOT NULL REFERENCES users(id) ON DELETE SET NULL,
                       assignee_id UUID REFERENCES users(id) ON DELETE SET NULL,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       status task_status NOT NULL DEFAULT 'TODO',
                       due_date DATE,
                       priority INT,   -- e.g. 1 (high)–5 (low)
                       created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE chats (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
                       name VARCHAR(255) NOT NULL,       -- e.g. “Team Chat”
                       created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE messages (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          chat_id UUID NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
                          sender_id UUID NOT NULL REFERENCES users(id) ON DELETE SET NULL,
                          content TEXT NOT NULL,
                          sent_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_projects_owner          ON projects(owner_id);
CREATE INDEX idx_tasks_project           ON tasks(project_id);
CREATE INDEX idx_tasks_assignee          ON tasks(assignee_id);
CREATE INDEX idx_notifications_recipient ON notifications(recipient_id);
CREATE INDEX idx_messages_chat           ON messages(chat_id);


CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;


DO $$
DECLARE
tbl TEXT;
BEGIN
FOR tbl IN
SELECT table_name FROM information_schema.columns
WHERE column_name = 'updated_at'
  AND table_schema = 'public'
    LOOP
    EXECUTE format('
      DROP TRIGGER IF EXISTS set_%1$s_updated_at ON %1$s;
      CREATE TRIGGER set_%1$s_updated_at
      BEFORE UPDATE ON %1$s
      FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();',
      tbl);
END LOOP;
END;
$$;
