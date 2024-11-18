CREATE DATABASE USERS
GO

USE USERS;
GO

-- ACCOUNT Table
CREATE TABLE ACCOUNT (
    username VARCHAR(50) PRIMARY KEY,
    fullname VARCHAR(100) NOT NULL,
    phonenumber VARCHAR(15) NOT NULL,
    userpassword VARCHAR(255) NOT NULL,
    listfriend VARCHAR(10) DEFAULT 'public',
    gender VARCHAR(6) DEFAULT 'no',
    createdate DATE NOT NULL,
    CONSTRAINT chk_list_account CHECK (listfriend IN ('public', 'friendonly', 'no')),
    CONSTRAINT chk_gender_account CHECK (gender IN ('male', 'female', 'no'))
);
GO

-- FRIEND Table
CREATE TABLE FRIEND (
    username VARCHAR(50),
    friendwith VARCHAR(50),
    frienddate DATE NOT NULL,
    PRIMARY KEY (username, friendwith),
    FOREIGN KEY (username) REFERENCES ACCOUNT(username),
    FOREIGN KEY (friendwith) REFERENCES ACCOUNT(username),
    CONSTRAINT chk_different_friends CHECK (username <> friendwith)
);
GO

-- NOTIFICATION Table
CREATE TABLE NOTIFICATION (
    notiid INT PRIMARY KEY,
    username VARCHAR(50),
    content TEXT NOT NULL,
    fromwhom VARCHAR(50),
    notidate DATE NOT NULL,
    notitime TIME NOT NULL,
    FOREIGN KEY (username) REFERENCES ACCOUNT(username)
);
GO

-- FRIENDREQUEST Table
CREATE TABLE FRIENDREQUEST (
    username VARCHAR(50),
    towhom VARCHAR(50),
    requestdate DATE NOT NULL,
    requesttime TIME NOT NULL,
    PRIMARY KEY (username, towhom),
    FOREIGN KEY (username) REFERENCES ACCOUNT(username),
    FOREIGN KEY (towhom) REFERENCES ACCOUNT(username),
    CONSTRAINT chk_different_request CHECK (username <> towhom)
);
GO

-- MESSAGE Table
CREATE TABLE MESSAGE (
    messid INT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    towhom VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    sentdate DATE NOT NULL,
    senttime TIME NOT NULL,
    FOREIGN KEY (username) REFERENCES ACCOUNT(username),
    FOREIGN KEY (towhom) REFERENCES ACCOUNT(username),
    CONSTRAINT chk_different_message CHECK (username <> towhom)
);
GO