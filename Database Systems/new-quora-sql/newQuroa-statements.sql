-- Your Name: Timothy Holland
-- Your Student Number: 697004 
-- By submitting, you declare that this work was completed entirely by yourself.
-- 
-- ___________________________________________________________________________________
-- ___________________________________________________________________________________
-- ______________________________________
-- BEGIN Q1

# List users who have sent friend request still pending

SELECT DISTINCT FO.user1
FROM friendof AS FO
WHERE WhenRejected IS NULL
AND WhenConfirmed IS NULL;


SELECT DISTINCT user1 AS userID 
	FROM friendof
    WHERE WhenRejected IS NULL
    AND WhenConfirmed IS NULL;
    
-- END Q1
-- 
-- ___________________________________________________________________________________
-- ___________________________________________________________________________________
-- ______________________________________
-- BEGIN Q2

# List forums with at least one subscription.

SELECT F.ID
	FROM forum AS F
    INNER JOIN subscribe AS S
    ON F.Id = S.forum;
	




# Assumes user cannot subscibe to forum more than once.
SELECT Id AS ForumID, Topic, COUNT(*) AS NumSubs
	# Inner Join necessarily excludes forums without subscriptions
    # This accounts for condition within question. No need for HAVING clause.
	FROM forum INNER JOIN subscribe
    ON forum.Id = subscribe.Forum
    GROUP BY forum.Id
    ORDER BY forum.Id;

-- END Q2
-- 
-- ___________________________________________________________________________________
-- ___________________________________________________________________________________
-- ______________________________________
--
-- BEGIN Q3

# Find forum with most recent post
# Assumes no ties

SELECT F.Id
	FROM forum AS F
    INNER JOIN post AS P
    ON F.Id = post.forum
    ORDER BY P.WhenPosted DESC
    LIMIT 1;


SELECT forum.Id AS ForumId, post.Id AS PostId, post.WhenPosted
	# Inner Join excludes comments from post
	FROM forum INNER JOIN post
    ON forum.Id = post.Forum
	# Most recent found through order clause
    ORDER BY post.WhenPosted DESC
    LIMIT 1;

-- END Q3
-- 
-- ___________________________________________________________________________________
-- ___________________________________________________________________________________
-- ______________________________________

-- BEGIN Q4

# For each user with at least one follower, list IDs of all their followers.
# Assumes user cannot follow someone else twice.

SELECT DISTINCT GU.Id
	FROM generaluser AS GU
	INNER JOIN following AS F
    ON GU.Id = F.following;




SELECT following.following AS UserId_of_followed, following.follower AS UserID_of_follower
	# Only need to consider following relation, as users with followers only of interest.
	FROM following
    ORDER BY userId_of_followed;

-- END Q4
-- 
-- ___________________________________________________________________________________
-- ___________________________________________________________________________________
-- ______________________________________
-- BEGIN Q5

# Which forum has highest number of upvotes on posts, which admin created that forum?
# Assumes no ties for first place, and at least one forum has post with at least one upvote.

SELECT F.Id, F.CreatedBy
	FROM forum AS F
    INNER JOIN post AS P
    ON F.id = P.forum
    INNER JOIN upvote AS U
    ON P.Id = U.post
    GROUP BY F.Id
    ORDER BY COUNT(U.WhenUpvoted) DESC
    LIMIT 1;
    



SELECT forum.CreatedBy AS AdminId, forum.Id AS ForumId, COUNT(*) AS NumberOfUpvotesInForum
	# Inner join excludes comments and forums without posts (which would have zero upvotes)
    FROM forum INNER JOIN post
    ON forum.Id = post.Forum
    INNER JOIN upvote
    # Inner join because question assumes at least one upvote on a forum
    ON post.Id = upvote.Post
    GROUP BY forum.Id
    ORDER BY NumberOfUpvotesInForum DESC
    LIMIT 1;

-- END Q5
-- 
-- ___________________________________________________________________________________
-- ___________________________________________________________________________________
-- ______________________________________
-- BEGIN Q6

# List all the users who have no “followers” (i.e., no other user is “following” them) and no friends. 
# Your query should return results of the form (userId, username).  

SELECT U.Id, U.username
	FROM user AS U
    WHERE U.Id NOT IN
		(SELECT following FROM following)
		AND U.ID NOT IN
        (SELECT FO.user1, FO.user2
        FROM friendof AS FO
        WHERE WhenRejected IS NULL
        AND WhenConfirmed IS NOT NULL
        AND WhenUnfriended IS NULL));

# Assumes admins not included
SELECT user.Id AS UserId, CONCAT(user.firstname, ' ', user.lastname) AS UserName
	# Removes admin users
    FROM user NATURAL JOIN generaluser
    WHERE user.Id NOT IN
		# Users with followers
		(SELECT DISTINCT Following FROM following)
	AND user.Id NOT IN
		# Users with friends
        (SELECT user1 AS userId
			FROM friendof
			WHERE WhenConfirmed IS NOT NULL
			AND WhenUnfriended IS NULL
		UNION
		SELECT user2 AS userId
			FROM friendof
			WHERE WhenConfirmed IS NOT NULL
			AND WhenUnfriended IS NULL)
	ORDER BY UserId;

-- END Q6
-- 
-- ___________________________________________________________________________________
-- ___________________________________________________________________________________
-- ______________________________________
-- BEGIN Q7

# A “high-quality contributor” is a user who has a mean average of at least one upvote for every post or comment they’ve made, 
# and has made at least one post or comment. Find all of the high- quality contributors and their average upvotes. 
# Return as (userId, avgUpvotes). (2 marks)

/** Creates two lists
		Number of posts per user
		Number of upvotes per post
	Joins lists on userId
    Aggregates to find average upvotes per post 
*/
SELECT UserId, posts_per_user/SUM(upvotes_per_post) AS avgUpvotes
	FROM 
		# Number of posts per user
		(SELECT user.Id AS UserId, COUNT(*) AS posts_per_user
		FROM user INNER JOIN post
		ON user.Id = post.PostedBy
		GROUP BY user.Id) AS user_posts
	INNER JOIN
		# Number of upvotes per post
		(SELECT post.Id, post.PostedBy, COUNT(upvote.WhenUpvoted) AS upvotes_per_post
		FROM post LEFT JOIN upvote
		ON post.Id = upvote.Post
		GROUP BY post.Id) AS post_upvotes
	ON user_posts.UserId = post_upvotes.PostedBy
    GROUP BY UserId
    HAVING avgUpvotes >= 1;

-- END Q7
-- 
-- ___________________________________________________________________________________
-- ___________________________________________________________________________________
-- ______________________________________
-- BEGIN Q8

# Find all comments or posts that have fewer likes than every comment that they are the parentpost of. 
# (I.e. every direct reply/comment to the parent post/comment has more upvotes than the parent). 
# Return as (PostOrCommentId)

/** Query finds the list of parents with more likes than at least one of their children
	This inner clause is negated to find the list of parents with fewer likes than ALL of their children.
    Assumes a post must have children to be considered. 
**/
SELECT parent.Id AS PostOrCommentId FROM 
	# initial join ensures only parents with children included
	post AS parent INNER JOIN post As child
	ON parent.Id = child.ParentPost
	WHERE parent.Id NOT IN (
		# Nested clause finds all parents with more likes than at least one child
        # Negated to find parents with fewer likes than ALL their children
		SELECT parent_likes.ParentPost FROM 
			# captures likes of parents
			(SELECT post.ParentPost, COUNT(upvote.WhenUpvoted) As likes
			FROM post LEFT JOIN upvote
			ON post.ParentPost = upvote.Post
			GROUP BY post.ParentPost) AS parent_likes
			RIGHT JOIN 
			# captures likes of child
			(SELECT post.Id as ChildPost, post.ParentPost, COUNT(upvote.WhenUpvoted) AS likes
			FROM post LEFT JOIN upvote 
			ON post.Id = upvote.Post
			GROUP BY post.ID) AS child_likes
			ON parent_likes.ParentPost = child_likes.ParentPost
			WHERE parent_likes.likes >= child_likes.likes
			ORDER BY parent_likes.ParentPost)
	ORDER BY parent.Id;

-- END Q8
-- 
-- ___________________________________________________________________________________
-- ___________________________________________________________________________________
-- ______________________________________
-- BEGIN Q9

# List all the ‘general’ users who have only upvoted posts or comments made by an admin OR someone who is currently their friend 
# (i.e. they don’t like any posts of someone who is not an admin and not currently their friend). 
# Users who have liked their own posts should not be returned. Your query should return results of the form (userID).

/** Query works by separating OR clause into two parts
		First part finds users who have upvoted post by someone other than admin
        Second part finds users who have only upvoted post by a friend
	UserIds are respectively checked against these two parts
**/
SELECT DISTINCT Id AS UserId
	FROM generaluser
    WHERE Id NOT IN 
		# Part 1: General users who have upvoted post by someone other than admin
		(SELECT DISTINCT upvote.User
			# Excludes posts without upvotes
			FROM upvote INNER JOIN post
			ON upvote.post = post.Id
			# Includes only posts of non-admins with upvotes
			INNER JOIN generaluser
			ON post.PostedBy = generaluser.Id
			ORDER BY upvote.User)
	OR Id IN
		# Part 2: Users in friendships who have only upvoted post by friend.
		(SELECT DISTINCT friend_users.Id
			# Ensures users without friends not included in negation
			FROM 
				# List of users in friendships
				(SELECT user1 AS Id FROM friendof
				UNION 
				SELECT user2 AS Id FROM friendof) AS friend_users
			WHERE friend_users.Id NOT IN
				(SELECT DISTINCT generaluser.Id
					# Excludes admins who have upvoted
					FROM generaluser INNER JOIN upvote
					ON generaluser.Id = upvote.User
					INNER JOIN post 
					ON upvote.Post = post.Id
					# Conditions list of upvoters on list of friendships
					WHERE (generaluser.Id, post.PostedBy) NOT IN 
						# List of each user and their friends
						(SELECT user1 AS userId, user2 AS friendId
							FROM friendof
						UNION
						SELECT user2 AS userId, user1 AS friendId
							FROM friendof)));
    
-- END Q9
-- 
-- ___________________________________________________________________________________
-- ___________________________________________________________________________________
-- ______________________________________
-- BEGIN Q10

# For each admin, show forum with highest number of subscriptions created.
# If ties, return all forums with this value

/** Query works by joining two derived tables together. 
		First, a list of number of max subscriptions for each admin.
		Second, a list of the number of subsriptions for each forum.
	They are joined based off the admin's ID
    Required fields are then projected 
**/
SELECT AdminId, ForumId, MaxSubs
	FROM
		(SELECT admin_forum_subs.AdminId, MAX(admin_forum_subs.Subscriptions) AS MaxSubs
		# Lists number of max subscriptions for each admin
			FROM 
			(SELECT admin.Id AS AdminId, forum.Id AS ForumId, COUNT(subscribe.WhenSubscribed) AS Subscriptions
				FROM admin LEFT JOIN forum 
				ON admin.Id = forum.CreatedBy
				LEFT JOIN subscribe
				ON forum.Id = subscribe.Forum
				GROUP BY AdminId, ForumId
				ORDER BY AdminId) AS admin_forum_subs
			GROUP BY admin_forum_subs.AdminId) AS admin_max_subs
	LEFT JOIN
		# Lists number of subscriptions for each forum
		(SELECT forum.Id AS ForumId, forum.CreatedBy, COUNT(subscribe.WhenSubscribed) AS NumSubs
			# Left join accounts for forums without subscriptions
			FROM forum LEFT JOIN subscribe
			ON forum.Id = subscribe.Forum
			GROUP BY ForumId
			ORDER BY ForumId) AS forum_subs
	# Joined through admin Id
	ON admin_max_subs.AdminId = forum_subs.CreatedBy
    # Condition removes forums where subscriptions not equal to admin's max subscription number
    WHERE admin_max_subs.MaxSubs = forum_subs.NumSubs
    # Logical disjunction allows for exception where admin has no posts.
    OR forum_subs.forumId IS NULL
    ORDER BY AdminId;

-- END Q10
-- 
-- ___________________________________________________________________________________
-- ___________________________________________________________________________________
-- ______________________________________
-- END OF ASSIGNMENT Do not write below this line