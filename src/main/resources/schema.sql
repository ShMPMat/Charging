
CREATE TABLE IF NOT EXISTS Company
(
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_company_id BIGINT,
    CONSTRAINT fk_parent_id
        FOREIGN KEY(parent_company_id)
            REFERENCES Company(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Station
(
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    latitude double precision NOT NULL,
    longitude double precision NOT NULL,
    company_id BIGINT NOT NULL,
    CONSTRAINT fk_company_id
        FOREIGN KEY(company_id)
            REFERENCES Company(id)
        ON DELETE CASCADE
);

CREATE EXTENSION IF NOT EXISTS cube;
CREATE EXTENSION IF NOT EXISTS earthdistance;
