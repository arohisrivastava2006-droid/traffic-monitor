-- Urban Traffic Monitoring and Analytics Engine : MySQL schema
CREATE DATABASE IF NOT EXISTS traffic_db;
USE traffic_db;

DROP TABLE IF EXISTS alerts;
DROP TABLE IF EXISTS traffic_data;
DROP TABLE IF EXISTS junction;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id  INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(64)  NOT NULL,          -- SHA-256 hash
    role     VARCHAR(20)  NOT NULL           -- ADMIN or OPERATOR
);

CREATE TABLE junction (
    junction_id   INT AUTO_INCREMENT PRIMARY KEY,
    junction_name VARCHAR(100) NOT NULL,
    location      VARCHAR(200) NOT NULL
);

CREATE TABLE traffic_data (
    record_id     INT AUTO_INCREMENT PRIMARY KEY,
    junction_id   INT NOT NULL,
    vehicle_count INT NOT NULL,
    density       DOUBLE NOT NULL,           -- 0 to 100 (%)
    average_speed DOUBLE NOT NULL,           -- km/h
    timestamp     DATETIME NOT NULL,
    FOREIGN KEY (junction_id) REFERENCES junction(junction_id) ON DELETE CASCADE,
    INDEX idx_ts (timestamp)
);

CREATE TABLE alerts (
    alert_id    INT AUTO_INCREMENT PRIMARY KEY,
    junction_id INT NOT NULL,
    alert_type  VARCHAR(50)  NOT NULL,
    severity    VARCHAR(20)  NOT NULL,
    message     VARCHAR(255) NOT NULL,
    timestamp   DATETIME NOT NULL,
    resolved    TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (junction_id) REFERENCES junction(junction_id) ON DELETE CASCADE
);

-- Default logins:  admin / admin123   and   operator / operator123
INSERT INTO users (name, username, password, role) VALUES
 ('System Admin',     'admin',    SHA2('admin123', 256),    'ADMIN'),
 ('Traffic Operator', 'operator', SHA2('operator123', 256), 'OPERATOR');

INSERT INTO junction (junction_name, location) VALUES
 ('Central Square',  'MG Road & 1st Cross'),
 ('Tech Park Gate',  'Outer Ring Road, Phase 2'),
 ('Railway Station', 'Station Road'),
 ('City Mall',       'Mall Avenue & 5th Main'),
 ('Airport Link',    'NH-44 Airport Exit'),
 ('Silk Board Junction', 'Hosur Road & Outer Ring Road'),
 ('Hebbal Flyover', 'Bellary Road & Outer Ring Road'),
 ('Marathahalli Bridge', 'Old Airport Road'),
 ('KR Puram Bridge', 'Old Madras Road'),
 ('Majestic Bus Stand', 'Kempegowda Road');
