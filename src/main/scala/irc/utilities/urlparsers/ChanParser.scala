package irc.utilities.urlparsers


import java.text.{DateFormat, SimpleDateFormat}
import java.util.{Date, TimeZone}

import irc.utilities.URLParser
import org.json.JSONObject

object ChanParser {

  def find(url: String): String = {
    var title = "none"
    val s = url.replace("thread", "res")
    val ssplit = s.split("/")
    ssplit(5) = ssplit(5).split("\\.")(0).split("#")(0)
    val urlstring = s.split("res/?")(0) + "res/" + ssplit(5) + ".json"
    try {
      val jsonstring = URLParser.readUrl(urlstring)
      val json = new JSONObject(jsonstring)
      val posts = json.getJSONArray("posts")
      val op = posts.getJSONObject(0)
      val board = ssplit(3)
      var subject: String = null
      subject = if (op.has("com")) URLParser.makeClean(op.getString("com") + "") else "No Subject"
      val no = op.getInt("no")
      val replies = posts.length() - 1
      if (op.has("sub")) {
        subject = "12" + URLParser.makeClean(op.getString("sub")) +
          ""
      }
      if (subject.length > 50) {
        subject = subject.substring(0, 49).trim() + "..."
      }
      val date = new Date(op.getLong("time") * 1000)
      val df = new SimpleDateFormat("dd MMM yyyy kk:mm:ss z")
      df.setTimeZone(TimeZone.getTimeZone("GMT"))
      val created = df.format(date)
      title = s"/$board/ - $subject | Thread no $no | Created $created | $replies replies"
    } catch {
      case e: Exception => {
        e.printStackTrace()
        throw new ParserException
      }
    }
    title
  }
}

