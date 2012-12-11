CREATE TABLE TWEETBASE.HASHTAGS (
		tweet_id BIGINT NOT NULL,
		hashtag VARCHAR(150) NOT NULL,
			PRIMARY KEY (tweet_id, hashtag)
);

CREATE TABLE TWEETBASE.LOCATIONS (
		tweet_id BIGINT NOT NULL,
		full_name VARCHAR(100) NOT NULL,
			PRIMARY KEY (tweet_id, full_name)
);

CREATE TABLE TWEETBASE.USERMENTIONS (
		tweet_id BIGINT NOT NULL,
		user_mention VARCHAR(20) NOT NULL,
			PRIMARY KEY (tweet_id, user_mention)
);

CREATE TABLE TWEETBASE.URLS (
		tweet_id BIGINT NOT NULL,
		url VARCHAR(150) NOT NULL,
			PRIMARY KEY (tweet_id, url)
);

CREATE TABLE TWEETBASE.MEDIA (
		tweet_id BIGINT NOT NULL,
		media_url VARCHAR(150) NOT NULL,
			PRIMARY KEY (tweet_id, media_url)
);

CREATE TABLE TWEETBASE.ONTOLOGY (
		user_id BIGINT NOT NULL,
		ontology_type VARCHAR(150) NOT NULL,
		cardinality INTEGER NOT NULL,
		confidence DOUBLE NOT NULL,
		support INTEGER NOT NULL,
			PRIMARY KEY (user_id, ontology_type, confidence, support)
);


CREATE TABLE TWEETBASE.TWEETS (
		tweet_id BIGINT NOT NULL,
		user_id BIGINT NOT NULL,
		screen_name VARCHAR(30) NOT NULL,
		created_at DATE NOT NULL,
		content VARCHAR(150) NOT NULL, 
		isretweet SMALLINT NOT NULL,
			PRIMARY KEY (tweet_id)
)
