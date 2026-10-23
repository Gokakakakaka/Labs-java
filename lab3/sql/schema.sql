
CREATE DATABASE IF NOT EXISTS java_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE java_app;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(190) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS items (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  about VARCHAR(500) NOT NULL,
  cndn VARCHAR(100) NOT NULL,
  CONSTRAINT fk_items_user FOREIGN KEY (user_id) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS lots (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  item_id BIGINT NOT NULL UNIQUE,
  pprice BIGINT NOT NULL,
  start_price BIGINT NOT NULL DEFAULT 0,
  step VARCHAR(50) NOT NULL,
  `date` DATE NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
  CONSTRAINT chk_lots_price CHECK (pprice >= 0 AND start_price >= 0),
  CONSTRAINT chk_lots_status CHECK (status IN ('OPEN','CLOSED','CANCELLED')),
  CONSTRAINT fk_lots_item FOREIGN KEY (item_id) REFERENCES items(id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS birds (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  lot_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  price BIGINT NOT NULL,
  `date` DATE NOT NULL,
  CONSTRAINT chk_birds_price CHECK (price > 0),
  CONSTRAINT fk_birds_lot FOREIGN KEY (lot_id) REFERENCES lots(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_birds_user FOREIGN KEY (user_id) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  INDEX idx_birds_lot_price (lot_id, price)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS deals (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  lot_id BIGINT NOT NULL,
  bird_id BIGINT NOT NULL UNIQUE,
  price BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
  CONSTRAINT chk_deals_price CHECK (price > 0),
  CONSTRAINT chk_deals_status CHECK (status IN ('PENDING','COMPLETED','CANCELLED')),
  CONSTRAINT fk_deals_lot FOREIGN KEY (lot_id) REFERENCES lots(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_deals_bid FOREIGN KEY (bird_id) REFERENCES birds(id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE USER IF NOT EXISTS 'auction_app'@'localhost' IDENTIFIED BY 'qwerty';
GRANT SELECT, INSERT, UPDATE, DELETE ON java_app.* TO 'auction_app'@'localhost';

INSERT IGNORE INTO users(id,name,email) VALUES
 (1,'Goka','goka@example.test'),
 (2,'Kom','kom@example.test'),
 (3,'Buyer Three','buyer3@example.test');

INSERT IGNORE INTO items(id,user_id,about,cndn) VALUES
 (1,1,'Vintage mechanical keyboard','Used - good'),
 (2,2,'Drawing tablet','Used - very good'),
 (3,1,'Desk lamp','New');

INSERT IGNORE INTO lots(id,item_id,pprice,start_price,step,`date`,status) VALUES
 (1,1,120,100,'10','2027-12-31','OPEN'),
 (2,2,250,250,'25','2027-12-31','OPEN'),
 (3,3,50,50,'5','2027-12-31','OPEN');

INSERT IGNORE INTO birds(id,lot_id,user_id,price,`date`) VALUES
 (1,1,2,110,'2026-10-20'),
 (2,1,3,120,'2026-10-21');

