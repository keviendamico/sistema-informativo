CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL    PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    role          VARCHAR(30)  NOT NULL CHECK (role IN ('EMPLOYEE', 'ADMIN')),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS projects (
    id          BIGSERIAL    PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255),
    internal    BOOLEAN      NOT NULL DEFAULT FALSE,
    absence     BOOLEAN      NOT NULL DEFAULT FALSE,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_projects (
    user_id    BIGINT NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, project_id)
);

CREATE TABLE IF NOT EXISTS timesheets (
    id         BIGSERIAL   PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users(id),
    year       SMALLINT    NOT NULL,
    month      SMALLINT    NOT NULL CHECK (month BETWEEN 1 AND 12),
    status       VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED')),
    activities   TEXT,
    notes        TEXT,
    validated_by BIGINT      REFERENCES users(id),
    submitted_at TIMESTAMP,
    validated_at TIMESTAMP,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, year, month)
);

CREATE TABLE IF NOT EXISTS timesheet_rows (
    id           BIGSERIAL    PRIMARY KEY,
    timesheet_id BIGINT       NOT NULL REFERENCES timesheets(id) ON DELETE CASCADE,
    project_id   BIGINT       NOT NULL REFERENCES projects(id),
    day          SMALLINT     NOT NULL CHECK (day BETWEEN 1 AND 31),
    hours        SMALLINT     NOT NULL DEFAULT 0 CHECK (hours >= 0 AND hours <= 24)
);

CREATE TABLE IF NOT EXISTS timesheet_absence_rows (
    id           BIGSERIAL PRIMARY KEY,
    timesheet_id BIGINT    NOT NULL REFERENCES timesheets(id) ON DELETE CASCADE,
    day          SMALLINT  NOT NULL CHECK (day BETWEEN 1 AND 31),
    hours        SMALLINT  CHECK (hours >= 0 AND hours <= 24),
    project_id   BIGINT    REFERENCES projects(id),
    UNIQUE (timesheet_id, day)
);

CREATE TABLE IF NOT EXISTS document_templates (
    id           BIGSERIAL    PRIMARY KEY,
    display_name VARCHAR(255) NOT NULL,
    filename     VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    data         BYTEA        NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);
