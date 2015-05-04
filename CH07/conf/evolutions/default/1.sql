# --- !Ups

CREATE TABLE User (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    email varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    firstname varchar(255) NOT NULL,
    lastname varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO User VALUES (1, "bob@marley.org", PASSWORD("secret"), "Bob", "Marley");

# --- !Downs

DROP TABLE User;
