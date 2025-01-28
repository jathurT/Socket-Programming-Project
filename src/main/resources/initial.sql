CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(100) NOT NULL
);

CREATE TABLE hosts (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       hostname VARCHAR(255) NOT NULL,
                       type VARCHAR(10) NOT NULL,
                       user_id INTEGER REFERENCES users(id),
                       status VARCHAR(20),
                       notification TEXT
);

CREATE TABLE logs (
                      id SERIAL PRIMARY KEY,
                      host_id INTEGER REFERENCES hosts(id),
                      time TIMESTAMP NOT NULL,
                      status VARCHAR(20) NOT NULL,
                      response_time DOUBLE PRECISION,
                      old_status VARCHAR(20)
);

-- Create indexes for better performance
CREATE INDEX idx_hosts_user_id ON hosts(user_id);
CREATE INDEX idx_logs_host_id ON logs(host_id);
CREATE INDEX idx_logs_time ON logs(time);