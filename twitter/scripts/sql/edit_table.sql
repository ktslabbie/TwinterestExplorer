ALTER TABLE TWEETBASE.USERS ADD english_rate DOUBLE NOT NULL DEFAULT -1.0;

UPDATE TWEETBASE.ONTOLOGY SET concatenation_window = cardinality, cardinality = concatenation_window