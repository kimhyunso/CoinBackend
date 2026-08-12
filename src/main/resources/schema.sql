CREATE TABLE member (
                        id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                        email      VARCHAR(100) NOT NULL UNIQUE,
                        name       VARCHAR(50)  NOT NULL,
                        provider   VARCHAR(20)  NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE coin (
                      id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                      symbol     VARCHAR(20)  NOT NULL UNIQUE,
                      name       VARCHAR(50)  NOT NULL,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE favorite (
                          id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                          member_id  BIGINT NOT NULL,
                          coin_id    BIGINT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
                          FOREIGN KEY (coin_id)   REFERENCES coin(id)   ON DELETE CASCADE
);

CREATE TABLE price_history (
                               id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                               coin_id     BIGINT         NOT NULL,
                               price       DECIMAL(20, 8) NOT NULL,
                               volume      DECIMAL(30, 8) NOT NULL,
                               recorded_at TIMESTAMP      NOT NULL,
                               FOREIGN KEY (coin_id) REFERENCES coin(id) ON DELETE CASCADE
);

CREATE TABLE price_stat (
                            id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                            coin_id    BIGINT         NOT NULL UNIQUE,
                            high_price DECIMAL(20, 8) NOT NULL,
                            low_price  DECIMAL(20, 8) NOT NULL,
                            high_at    TIMESTAMP      NOT NULL,
                            low_at     TIMESTAMP      NOT NULL,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (coin_id) REFERENCES coin(id) ON DELETE CASCADE
);

CREATE INDEX idx_price_history_coin_time
    ON price_history (coin_id, recorded_at DESC);

INSERT INTO coin (symbol, name) VALUES
                                    ('BTCUSDT', 'Bitcoin'),
                                    ('ETHUSDT', 'Ethereum'),
                                    ('BNBUSDT', 'BNB'),
                                    ('SOLUSDT', 'Solana'),
                                    ('XRPUSDT', 'XRP');