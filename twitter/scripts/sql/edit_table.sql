--ALTER TABLE TWEETBASE.USERS ADD profile_image_url VARCHAR(255) DEFAULT 'http://abs.twimg.com/sticky/default_profile_images/default_profile_4_200x200.png';


CREATE INDEX i_screen_name ON TWEETBASE.USERS (screen_name);
CREATE INDEX i_user_id ON TWEETBASE.TWEETS (user_id);
ALTER TABLE TWEETBASE.ONTOLOGY DROP PRIMARY KEY;