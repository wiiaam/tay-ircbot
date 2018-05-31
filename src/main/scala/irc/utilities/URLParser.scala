package irc.utilities


import java.io.{BufferedReader, InputStreamReader}
import java.net.{HttpURLConnection, URL}
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl._

import irc.utilities.urlparsers._
import org.jsoup.Jsoup

object URLParser {

  def find(s: String): String = {
    var url: URL = null
    var title = "Title not found"
    var host = ""
    try {
      url = new URL(s)
      val urlc = {
        if(s.startsWith("https")){
          setAllowAllCerts()
          url.openConnection().asInstanceOf[HttpsURLConnection]
        }
        else url.openConnection()
      }
      urlc.addRequestProperty("Accept-Language", "en-US,en;q=0.8")
      urlc.addRequestProperty("User-Agent", "Mozilla")
      urlc.connect()
      host = urlc.getURL.getHost
      if (!urlc.getContentType.startsWith("text/html")) {
        try {
          val title = FileParser.find(urlc)
          return title
        }
        catch {
          case e: Exception =>
        }
      }
      if (s.contains("youtube.com/watch?") || s.contains("youtu.be/")) {
        try {
          val title = YoutubeParser.find(s)
          return title
        }
        catch {
          case e: Exception =>
            e.printStackTrace()
        }
      }
      if ((s.contains("boards.4chan.org/") || s.contains("//8ch.net")) &&
        (s.contains("/thread/") || s.contains("/res/"))) {
        try {
          val title = ChanParser.find(s)
          return title
        }
        catch {
          case e: Exception =>
        }
      }
      if (s.contains("steamcommunity.com") && (s.contains("/id/") || s.contains("/profiles/"))) {
        try {
          val title = SteamParser.find(s)
          return title
        }
        catch {
          case e: Exception =>
        }
      }
      if (s.contains("/comments/") && s.contains("reddit.com/r/")) {
        try {
          val title = RedditParser.find(s)
          return title
        }
        catch {
          case e: Exception =>
        }
      }
      val doc = Jsoup.parse(readUrl(s))
      val ps = doc.select("title")
      title = ps.text().replaceAll("\r", "").replaceAll("\n", "")
    } catch {
      case e: SSLHandshakeException =>
        title = "Title not found (SSL handshake error)"
      case e: SSLException =>
        title = "Title not found (SSL Error)"
      case e: Exception => e.printStackTrace()
    }
    if (title.length > 50) title = title.substring(0, 50) + "..."

    title = s"[URL] ${title.trim()} ($host)"
    title
  }

  def readUrl(urlString: String): String = {
    val url = new URL(urlString)

    val urlc = if (urlString.startsWith("https")) {
      setAllowAllCerts()
      val urlc = url.openConnection().asInstanceOf[HttpsURLConnection]
      urlc.setInstanceFollowRedirects(true)
      urlc.addRequestProperty("Accept-Language", "en-US,en;q=0.8")
      urlc.addRequestProperty("User-Agent", "Mozilla")
      urlc.connect()
      urlc
    }
    else {
      val urlc = url.openConnection().asInstanceOf[HttpURLConnection]
      urlc.setInstanceFollowRedirects(true)
      urlc.addRequestProperty("Accept-Language", "en-US,en;q=0.8")
      urlc.addRequestProperty("User-Agent", "Mozilla")
      urlc.connect()
      urlc
    }
    val reader = new BufferedReader(new InputStreamReader(urlc.getInputStream))
    val buffer = new StringBuffer()
    val chars = new Array[Char](1024)
    var reading = true
    while (reading) {
      val read = reader.read(chars)
      if (read != -1) buffer.append(chars, 0, read)
      else reading = false
    }
    buffer.toString
  }

  def makeClean(htmlString: String): String = {
    Jsoup.parse(htmlString).text().replaceAll("\r", "")
      .replaceAll("\n", "")
  }

  def setAllowAllCerts(): Unit ={
    val tm = new X509TrustManager {
      override def getAcceptedIssuers: Array[X509Certificate] = null

      override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}

      override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}
    }
    val tmarray: Array[TrustManager] = Array(tm)
    val context = SSLContext.getInstance("SSL")
    context.init(new Array[KeyManager](0), tmarray, new SecureRandom())
    val sslfact: SSLSocketFactory = context.getSocketFactory
    HttpsURLConnection.setDefaultSSLSocketFactory(sslfact)
  }
}
