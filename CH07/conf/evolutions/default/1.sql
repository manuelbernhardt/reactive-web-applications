# --- !Ups

CREATE TABLE "user" (
    id bigserial primary key,
    email varchar NOT NULL,
    password varchar NOT NULL,
    firstname varchar NOT NULL,
    lastname varchar NOT NULL
);

# --- !Downs

DROP TABLE "user";
