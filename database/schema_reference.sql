-- ============================================================
-- StockInfo Database - Reference Schema
-- ============================================================
-- NOTE: You do NOT need to run this manually. Spring Boot + Hibernate
-- will auto-create all these tables on first run (spring.jpa.hibernate.ddl-auto=update)
-- as long as the database `stockinfo_db` exists (or createDatabaseIfNotExist=true, already set).
--
-- This file is provided purely for documentation / viva / report purposes
-- so you can show the DB design without needing the app running.
-- ============================================================

CREATE DATABASE IF NOT EXISTS stockinfo_db;
USE stockinfo_db;

-- Users table (also stores Admins, differentiated by `role`)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',        -- USER or ADMIN
    wallet_balance DECIMAL(15,2) NOT NULL DEFAULT 100000.00,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME
);

-- Stocks table
CREATE TABLE stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(15) NOT NULL UNIQUE,
    company_name VARCHAR(100) NOT NULL,
    sector VARCHAR(50) NOT NULL,
    current_price DECIMAL(15,2) NOT NULL,
    previous_close DECIMAL(15,2) NOT NULL,
    day_high DECIMAL(15,2) NOT NULL,
    day_low DECIMAL(15,2) NOT NULL,
    volume BIGINT NOT NULL DEFAULT 0,
    change_percent DECIMAL(5,2) NOT NULL DEFAULT 0,
    last_updated DATETIME
);

-- Portfolio table (a user's current holdings)
CREATE TABLE portfolio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    average_buy_price DECIMAL(15,2) NOT NULL,
    updated_at DATETIME,
    UNIQUE KEY uq_user_stock (user_id, stock_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (stock_id) REFERENCES stocks(id)
);

-- Transactions table (buy/sell history)
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL,                        -- BUY or SELL
    quantity INT NOT NULL,
    price_per_unit DECIMAL(15,2) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    transaction_date DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (stock_id) REFERENCES stocks(id)
);

-- Watchlist table
CREATE TABLE watchlist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,
    added_at DATETIME,
    UNIQUE KEY uq_watch_user_stock (user_id, stock_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (stock_id) REFERENCES stocks(id)
);

-- Investment Suggestions table (latest Buy/Hold/Sell call per stock)
CREATE TABLE investment_suggestions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_id BIGINT NOT NULL UNIQUE,
    suggestion VARCHAR(10) NOT NULL,                  -- BUY, HOLD, SELL
    reason VARCHAR(255),
    generated_at DATETIME,
    FOREIGN KEY (stock_id) REFERENCES stocks(id)
);
