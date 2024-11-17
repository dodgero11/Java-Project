USE world;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    address TEXT,
    date_of_birth DATE,
    gender ENUM('Male', 'Female'),
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255),
    account_status ENUM('Active', 'Deactivated') DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE login_history (
    login_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE friends (
    user_id INT,
    friend_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (friend_id) REFERENCES users(user_id),
    PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE chat_groups (
    group_id INT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE group_members (
    group_id INT,
    user_id INT,
    role ENUM('Member', 'Admin'),
    FOREIGN KEY (group_id) REFERENCES chat_groups(group_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE spam_reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    reported_by INT,
    reported_user INT,
    report_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reported_by) REFERENCES users(user_id),
    FOREIGN KEY (reported_user) REFERENCES users(user_id)
);

CREATE TABLE user_activity (
    activity_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    activity_type ENUM('Open App', 'Chat User', 'Chat Group'),
    activity_count INT,
    activity_date DATE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);