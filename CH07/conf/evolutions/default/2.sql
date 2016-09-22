# --- !Ups

CREATE TABLE "twitter_user" (
  id bigserial primary key,
  created_on timestamp with time zone NOT NULL,
  phone_number varchar NOT NULL,
  twitter_user_name varchar NOT NULL
);

CREATE TABLE "mentions" (
  id bigserial primary key,
  tweet_id varchar NOT NULL,
  user_id bigint NOT NULL,
  created_on timestamp with time zone NOT NULL,
  author_user_name varchar NOT NULL,
  text varchar NOT NULL
);

CREATE TABLE "mention_subscriptions" (
  id bigserial primary key,
  created_on timestamp with time zone NOT NULL,
  user_id bigint NOT NULL
);

# --- !Downs

DROP TABLE "twitter_user";
DROP TABLE "mentions";
DROP TABLE "mention_subscriptions";
