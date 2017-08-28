package irc.utilities.urlparsers

import java.io.IOException
import java.text.NumberFormat
import java.time.LocalDateTime
import java.util.Locale
import java.util.regex.Pattern

import irc.config.UserConfig
import irc.utilities.URLParser
import org.json.JSONObject

object YoutubeParser {

  def find(s: String): String = {
    var videoid = "none"
    videoid = if (s.contains("youtu.be")) s.split("youtu.be/")(1).split("\\?")(0)
    else {
      if(s.contains("?v=")){
        s.split(".*\\?v=")(1).split("&")(0)
      }
      else if(s.contains("&v=")){
        s.split(".*&v=")(1).split("&")(0)
      }
      else throw new ParserException
    }
    findById(videoid)
  }

  def findById(s: String): String = {
    val videoid = s
    try {
      val url = "https://www.googleapis.com/youtube/v3/videos?key=" +
        UserConfig.getJson.getString("googleapikey") +
        "&part=snippet,statistics,contentDetails&id=" +
        videoid
      val jsonstring = URLParser.readUrl(url)
      val json = new JSONObject(jsonstring)
      val items = json.getJSONArray("items").getJSONObject(0)
      val snippet = items.getJSONObject("snippet")
      val contentDetails = items.getJSONObject("contentDetails")
      val statistics = items.getJSONObject("statistics")
      val title = "\u0002" + URLParser.makeClean(snippet.getString("title")) + "\u0002"
      val uploader = snippet.getString("channelTitle")
      val views = NumberFormat.getNumberInstance(Locale.US).format(statistics.getInt("viewCount"))
      val likes = NumberFormat.getNumberInstance(Locale.US).format(statistics.getInt("likeCount"))
      val dislikes = NumberFormat.getNumberInstance(Locale.US).format(statistics.getInt("dislikeCount"))
      var duration = contentDetails.getString("duration")
      var dur: String = null
      if (Pattern.matches("PT.*D.*H.*M.*S", duration)) {
        dur = duration.substring(2, duration.length)
        val days = dur.split("D")(0)
        dur = dur.split("D")(1)
        val hours = dur.split("H")(0)
        dur = dur.split("H")(1)
        val minutes = dur.split("M")(0)
        dur = dur.split("M")(1)
        val seconds = dur.split("S")(0)
        dur = s"${"%02d".format(days.toInt)}:${"%02d".format(hours.toInt)}:${"%02d".format(minutes.toInt)}:${"%02d".format(seconds.toInt)}"
      } else if (Pattern.matches("PT.*H.*M.*S", duration)) {
        dur = duration.substring(2, duration.length)
        val hours = dur.split("H")(0)
        dur = dur.split("H")(1)
        val minutes = dur.split("M")(0)
        dur = dur.split("M")(1)
        val seconds = dur.split("S")(0)
        dur = s"${"%02d".format(hours.toInt)}:${"%02d".format(minutes.toInt)}:${"%02d".format(seconds.toInt)}"
      } else if (Pattern.matches("PT.*M.*S", duration)) {
        dur = duration.substring(2, duration.length)
        val minutes = dur.split("M")(0)
        dur = dur.split("M")(1)
        val seconds = dur.split("S")(0)
        dur = s"${"%02d".format(minutes.toInt)}:${"%02d".format(seconds.toInt)}"
      } else {
        dur = duration.substring(2, duration.length - 1) + " seconds"
      }
      duration = dur
      val ldt = LocalDateTime.parse(snippet.getString("publishedAt").split("\\.")(0))
      val date = ldt.getDayOfMonth
      var dateString = ""
      date match {
        case 1 => dateString = "1st"
        case 2 => dateString = "2nd"
        case 3 => dateString = "3rd"
        case _ =>
      }
      if (dateString == "") dateString = date + "th"
      val uploaded = ldt.getMonth.name().charAt(0) + ldt.getMonth.name().toLowerCase.substring(1) +
        " " +
        dateString +
        " " +
        ldt.getYear
      var percentlike = statistics.getDouble("likeCount") / statistics.getDouble("dislikeCount")
      val likeQuartile = Math.ceil(percentlike * 10).toInt
      var likebar = "\u000303,03"
      var ratingchar = "^"
      for (i <- 0 until 10) {
        if (i == likeQuartile) {
          likebar += "\u000304,04"
          ratingchar = "v"
        }
        likebar += ratingchar
      }
      likebar += "\u0003"

      val likestatus = s"\u000303$likes \u0003likes\u00034 $dislikes \u0003dislikes"

      val live = snippet.getString("liveBroadcastContent") == "live"

      var uploadInfo = s"\u000311\u0002${uploader}\u0002\u0003 on \u0002${uploaded}\u0002"
      if(live){
        uploadInfo = "Stream started by " + uploadInfo
      }
      else uploadInfo = "Uploaded by " + uploadInfo + s" | Duration: \u0002${duration}\u0002 | \u0002${views}\u0002 views"

      val logo = "\u0002\u00031,0You\u00030,4Tube\u0003\u0002"



      s"$logo $title | $uploadInfo | $likestatus"
    } catch {
      case e: IOException =>
        e.printStackTrace()
        "Error reading YouTube API"
      case e: IndexOutOfBoundsException =>
        e.printStackTrace()
        throw new ParserException
    }
  }
}
