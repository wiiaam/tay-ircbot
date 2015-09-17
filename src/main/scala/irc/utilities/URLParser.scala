package irc.utilities


import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import irc.utilities.urlparsers._
import org.jsoup.Jsoup

object URLParser {

  def find(s: String): String = {
    var url: URL = null
    var title = "Title not found"
    var host = ""
    try {
      url = new URL(s)
      val urlc = url.openConnection()
      urlc.addRequestProperty("Accept-Language", "en-US,en;q=0.8")
      urlc.addRequestProperty("User-Agent", "Mozilla")
      urlc.connect()
      host = urlc.getURL.getHost
      println(urlc.getContentType)
      if (!urlc.getContentType.startsWith("text/html")) {
        return FileParser.find(urlc)
      }
      if (s.contains("youtube.com/watch?") || s.contains("youtu.be/")) {
        return YoutubeParser.find(s)
      }
      if ((s.contains("boards.4chan.org/") || s.contains("//8ch.net")) &&
        (s.contains("/thread/") || s.contains("/res/"))) {
        return ChanParser.find(s)
      }
      if (s.contains("steamcommunity.com") && (s.contains("/id/") || s.contains("/profiles/"))) {
        return SteamParser.find(s)
      }
      if (s.contains("/comments/") && s.contains("reddit.com/r/")) {
        return RedditParser.find(s)
      }
      val doc = Jsoup.connect(s).followRedirects(true).get
      val ps = doc.select("title")
      title = ps.text().replaceAll("\r", "").replaceAll("\n", "")
    } catch {
      case e: MalformedURLException => e.printStackTrace()
      case e: IOException => {
        e.printStackTrace()
        title = "Title not found"
      }
    }
    title = String.format("[URL] %s (%s)", title.trim(), host)
    title
  }

  def readUrl(urlString: String): String = {
    val url = new URL(urlString)
    val urlc = url.openConnection()
    urlc.addRequestProperty("Accept-Language", "en-US,en;q=0.8")
    urlc.addRequestProperty("User-Agent", "Mozilla")
    urlc.connect()
    val reader = new BufferedReader(new InputStreamReader(urlc.getInputStream))
    val buffer = new StringBuffer()
    val chars = new Array[Char](1024)
    var reading = true
    while (reading) {
      val read = reader.read(chars)
      if(read != -1) buffer.append(chars, 0, read)
      else reading = false
    }

    buffer.toString
  }

  def makeClean(htmlString: String): String = {
    Jsoup.parse(htmlString).text().replaceAll("\r", "")
      .replaceAll("\n", "")
  }
}
