package messages

case class ComputeReach(tweet_id: BigInt)
case class TweetReach(tweet_id: BigInt, score: Int)

case class FetchFollowerCount(tweet_id: BigInt, userId: BigInt)
case class FollowerCount(tweet_id: BigInt, userId: BigInt, followersCount: Int)

case class StoreReach(tweet_id: BigInt, score: Int)
case class ReachStored(tweet_id: BigInt)