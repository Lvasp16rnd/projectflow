CREATE TABLE requests (
    id           UUID         PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    requester_id VARCHAR(255) NOT NULL,
    category     VARCHAR(255) NOT NULL,
    priority     VARCHAR(255) NOT NULL,
    status       VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL,
    version      BIGINT       NOT NULL
);
