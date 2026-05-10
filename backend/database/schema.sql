CREATE DATABASE IF NOT EXISTS connect4;
USE connect4;

CREATE TABLE IF NOT EXISTS Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    elo_rating INT NOT NULL DEFAULT 1000,
    recovery_token VARCHAR(255) NULL
);

CREATE TABLE IF NOT EXISTS Match_History (
    match_id INT AUTO_INCREMENT PRIMARY KEY,
    player1_id INT NOT NULL,
    player2_id INT NOT NULL,
    outcome ENUM('P1_WIN', 'P2_WIN', 'DRAW') NOT NULL,
    time_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player1_id) REFERENCES Users(user_id),
    FOREIGN KEY (player2_id) REFERENCES Users(user_id)
);