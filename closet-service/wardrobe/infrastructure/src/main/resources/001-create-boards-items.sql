CREATE TABLE boards (
                        id         UUID PRIMARY KEY,
                        user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                        title      VARCHAR(100) NOT NULL,
                        item_count INTEGER      NOT NULL DEFAULT 0,
                        UNIQUE (id, user_id)
);
CREATE INDEX ix_boards_user ON boards (user_id);

CREATE TABLE items (
                       id          UUID PRIMARY KEY,
                       board_id    UUID             NOT NULL,
                       user_id     UUID             NOT NULL,
                       title       VARCHAR(100),
                       description TEXT,
                       image_key   VARCHAR(255)     NOT NULL,
                       CONSTRAINT fk_items_board FOREIGN KEY (board_id, user_id)
                           REFERENCES boards (id, user_id) ON DELETE CASCADE
);
CREATE INDEX ix_items_board ON items (board_id);
CREATE INDEX ix_items_user ON items (user_id);
