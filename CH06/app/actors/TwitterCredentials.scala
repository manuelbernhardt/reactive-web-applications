package actors

import play.api.Play
import play.api.libs.oauth.{RequestToken, ConsumerKey}

trait TwitterCredentials {

  import play.api.Play.current

  protected def credentials = for {
    apiKey <- Play.configuration.getString("twitter.apiKey")
    apiSecret <- Play.configuration.getString("twitter.apiSecret")
    token <- Play.configuration.getString("twitter.accessToken")
    tokenSecret <- Play.configuration.getString("twitter.accessTokenSecret")
  } yield (ConsumerKey(apiKey, apiSecret), RequestToken(token, tokenSecret))

}
