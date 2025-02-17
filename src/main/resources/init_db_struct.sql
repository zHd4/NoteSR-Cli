CREATE TABLE IF NOT EXISTS notes(
    id varchar(255) PRIMARY KEY,
    name text NOT NULL,
    data text NOT NULL,
    updated_at varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS files_info(
    id varchar(255) PRIMARY KEY,
    note_id varchar(255) NOT NULL,
    name text NOT NULL,
    type varchar(255),
    thumbnail blob,
    size bigint NOT NULL,
    created_at varchar(255) NOT NULL,
    updated_at varchar(255) NOT NULL,
    FOREIGN KEY(note_id) REFERENCES notes(id)
);

CREATE TABLE IF NOT EXISTS data_blocks(
    id varchar(255) PRIMARY KEY,
    file_id varchar(255) NOT NULL,
    block_order bigint NOT NULL,
    data blob NOT NULL,
    FOREIGN KEY(file_id) REFERENCES files_info(id)
);