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

CREATE TABLE TWEETBASE.FOLLOWERS (
		user_id BIGINT NOT NULL,
		follower_id BIGINT NOT NULL,
			PRIMARY KEY (user_id, follower_id)
);

CREATE TABLE TWEETBASE.FRIENDS (
		user_id BIGINT NOT NULL,
		friend_id BIGINT NOT NULL,
			PRIMARY KEY (user_id, friend_id)
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
		concatenation_window INTEGER NOT NULL,
		confidence DOUBLE NOT NULL,
		support INTEGER NOT NULL,
			PRIMARY KEY (user_id, ontology_type, concatenation_window, confidence, support)
);

CREATE TABLE TWEETBASE.TWEETS (
		tweet_id BIGINT NOT NULL,
		user_id BIGINT NOT NULL,
		screen_name VARCHAR(255) NOT NULL,
		created_at DATE NOT NULL,
		content VARCHAR(255) NOT NULL, 
		isretweet SMALLINT NOT NULL,
		lang VARCHAR(3) NOT NULL,
			PRIMARY KEY (tweet_id)
);

CREATE TABLE TWEETBASE.USERS (
		user_id BIGINT NOT NULL,
		screen_name VARCHAR(255) NOT NULL,
		name VARCHAR(64) NOT NULL,
		description VARCHAR(255),
		location VARCHAR(255),
		followers_count INTEGER NOT NULL,
		friends_count INTEGER NOT NULL,
		statuses_count INTEGER NOT NULL,
		created_at DATE NOT NULL,
		protected SMALLINT NOT NULL,
			PRIMARY KEY (user_id)
);
