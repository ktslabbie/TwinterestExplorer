--select users.user_id,
--       followers.follower_id
--from users, followers
--where followers.user_id = users.user_id and users.user_id = ?;

SELECT * FROM Followers WHERE user_id = ?