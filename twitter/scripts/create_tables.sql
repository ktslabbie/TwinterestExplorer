CREATE TABLE TWEETBASE.HASHTAGS (
		tweet_id BIGINT NOT NULL,
		hashtag VARCHAR(150) NOT NULL,
			PRIMARY KEY (tweet_id)
);

CREATE TABLE TWEETBASE.LOCATIONS (
		tweet_id BIGINT NOT NULL,
		full_name VARCHAR(100) NOT NULL,
			PRIMARY KEY (tweet_id)
);


CREATE TABLE TWEETBASE.TWEETS (
		tweet_id BIGINT NOT NULL,
		user_id BIGINT NOT NULL,
		screen_name VARCHAR(30) NOT NULL,
		created_at DATE NOT NULL,
		content VARCHAR(150) NOT NULL, 
			PRIMARY KEY (tweet_id)
)

