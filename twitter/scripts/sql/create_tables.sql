--CREATE TABLE HASHTAGS (
--		id SERIAL PRIMARY KEY,
--		tweet_id BIGINT NOT NULL,
--		hashtag VARCHAR(150) NOT NULL
--);

--CREATE TABLE LOCATIONS (
--		id SERIAL PRIMARY KEY,
--		tweet_id BIGINT NOT NULL,
--		full_name VARCHAR(100) NOT NULL
--);

--CREATE TABLE USERMENTIONS (
--		id SERIAL PRIMARY KEY,
--		tweet_id BIGINT NOT NULL,
--		user_mention VARCHAR(20) NOT NULL
--);

CREATE TABLE FOLLOWERS (
		user_id BIGINT NOT NULL,
		follower_id BIGINT NOT NULL,
			PRIMARY KEY (user_id, follower_id)

);

CREATE TABLE FRIENDS (
		user_id BIGINT NOT NULL,
		friend_id BIGINT NOT NULL,
			PRIMARY KEY (user_id, friend_id)
);

--CREATE TABLE URLS (
--		id SERIAL PRIMARY KEY,
--		tweet_id BIGINT NOT NULL,
--		url VARCHAR(150) NOT NULL
--);

--CREATE TABLE MEDIA (
--		id SERIAL PRIMARY KEY,
--		tweet_id BIGINT NOT NULL,
--		media_url VARCHAR(150) NOT NULL
--);

CREATE TABLE ONTOLOGY (
		ontology_key VARCHAR(32),
		user_ontology JSON,
			PRIMARY KEY (ontology_key)
);

--		user_id BIGINT NOT NULL,
--		ontology_type VARCHAR(150) NOT NULL,
--		cardinality INTEGER NOT NULL,
--		concatenation_window SMALLINT NOT NULL,
--		confidence FLOAT(2) NOT NULL,
--		support INTEGER NOT NULL,
--			PRIMARY KEY (user_id, ontology_type, concatenation_window, confidence, support)

--CREATE TABLE TWEETS (
--		tweet_id BIGINT NOT NULL,
--		user_id BIGINT NOT NULL,
--		created_at BIGINT NOT NULL,
--		content VARCHAR(255) NOT NULL, 
--		isretweet BOOLEAN NOT NULL,
--		language VARCHAR(3) NOT NULL,
--			PRIMARY KEY (tweet_id)
--);

CREATE TABLE USERS (
		user_id BIGINT NOT NULL,
		screen_name VARCHAR(255) NOT NULL,
		properties JSON,
		tweets JSON,
		--name VARCHAR(64) NOT NULL,
		--description VARCHAR(255),
		--location VARCHAR(255),
		--followers_count INTEGER NOT NULL,
		--friends_count INTEGER NOT NULL,
		--statuses_count INTEGER NOT NULL,
		--tweets JSON,
		--created_at DATE NOT NULL,
		--protected BOOLEAN NOT NULL,
		english_rate FLOAT(2) NOT NULL,
		--profile_image_url VARCHAR(255),
			PRIMARY KEY (user_id)
);

CREATE INDEX i_screen_name
  ON users
  USING btree
  (screen_name COLLATE pg_catalog."default");

CREATE INDEX i_ontology_key
  ON ontology
  USING btree
  (ontology_key COLLATE pg_catalog."default");
  
--CREATE INDEX i_hashtags_tweet_id
--  ON hashtags
--  USING btree
--  (tweet_id);
  
--CREATE INDEX i_locations_tweet_id
--  ON locations
--  USING btree
--  (tweet_id);
  
--CREATE INDEX i_urls_tweet_id
--  ON urls
--  USING btree
--  (tweet_id);

--CREATE INDEX i_mentions_tweet_id
--  ON usermentions
--  USING btree
--  (tweet_id);
  
--CREATE INDEX i_media_tweet_id
--  ON media
--  USING btree
--  (tweet_id);
  
--CREATE INDEX i_user_id
--  ON tweets
--  USING btree
--  (user_id);
