CREATE TABLE app_user (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    user_role VARCHAR(50) NOT NULL,
    user_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE virtual_server (
    server_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    server_name VARCHAR(150) NOT NULL,
    server_ip_address VARCHAR(50) NOT NULL,
    server_status VARCHAR(30) NOT NULL,
    server_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_virtual_server_user
        FOREIGN KEY (user_id)
        REFERENCES app_user(user_id)
        ON DELETE CASCADE
);

CREATE TABLE server_configuration (
    server_id BIGINT PRIMARY KEY,
    cpu INTEGER NOT NULL,
    ram INTEGER NOT NULL,
    configuration_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_server_configuration
        FOREIGN KEY (server_id)
        REFERENCES virtual_server(server_id)
        ON DELETE CASCADE
);

CREATE TABLE server_metric (
    metric_id BIGSERIAL PRIMARY KEY,
    server_id BIGINT NOT NULL,
    cpu_usage_percent INTEGER NOT NULL,
    ram_usage_mb INTEGER NOT NULL,
    network_load_percent INTEGER NOT NULL,
    metric_recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_server_metric_server
        FOREIGN KEY (server_id)
        REFERENCES virtual_server(server_id)
        ON DELETE CASCADE
);

CREATE TABLE server_log (
    log_id BIGSERIAL PRIMARY KEY,
    server_id BIGINT NOT NULL,
    log_level VARCHAR(20) NOT NULL,
    log_message TEXT NOT NULL,
    log_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_log_server
        FOREIGN KEY (server_id)
        REFERENCES virtual_server(server_id)
        ON DELETE CASCADE
);