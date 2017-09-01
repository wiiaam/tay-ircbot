package irc.utilities.urlparsers

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import irc.utilities.URLParser
import org.json.JSONArray

object RedditParser {

  def find(s: String): String = {
    var title = "none"
    var urlstring = s
    urlstring = urlstring + ".json"
    try {
      val jsonstring = URLParser.readUrl(urlstring)
      val json = new JSONArray(jsonstring)
      val first = json.getJSONObject(0)
      val data = first.getJSONObject("data")
      val children = data.getJSONArray("children")
      val info = children.getJSONObject(0)
      val infodata = info.getJSONObject("data")
      val numComments = infodata.getInt("num_comments")

      val date = new Date(infodata.getLong("created_utc") * 1000)
      val df = new SimpleDateFormat("dd MMM yyyy kk:mm:ss z")
      df.setTimeZone(TimeZone.getTimeZone("GMT"))
      val created = df.format(date)

      val subreddit = infodata.getString("subreddit")
      val postTitle = URLParser.makeClean(infodata.getString("title"))
      var link = ""
      if (!infodata.getString("domain").startsWith("self.")) {
        link = "URL: " + infodata.getString("url") + " | "
      }
      title = s"/r/$subreddit | 2$postTitle | ${link}Comments: $numComments | Created $created "
      return title
    } catch {
      case e: Exception => throw new ParserException
    }
    title
  }
}
