package modules

import java.net.URL
import javax.net.ssl.HttpsURLConnection

import irc.config.UserConfig
import irc.message.Message
import irc.server.{ConnectionManager, ServerResponder}
import irc.utilities.URLParser.setAllowAllCerts
import ircbot.{BotCommand, BotModule}
import org.apache.commons.io.IOUtils

import scala.util.Random
import scala.xml.XML

class Pidgin extends BotModule{

  startRssMonitor()

  private val configKey = "pidgin"
  private var newsItem: Option[NewsItem] = None
  private var newsItems: Array[NewsItem] = Array()
  private var cooldown = System.currentTimeMillis()
  private var cooldownTime = 3000 // milliseconds

  override val commands: Map[String, Array[String]] = Map("pidgin" -> Array("Show current pidgin headline and turn on or off pidgin news announcements",
  "Turn on and off with pidgin <on,off>. View an article with pidgin <1-10>"))

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(b.command == "pidgin"){
      var channels = UserConfig.getArrayAsType[String](configKey).getOrElse(Array[String]())
      if(!m.target.startsWith("#")) {
        r.reply("This command can only be used within a channel")
        return
      }

      val key = m.server + "/" + m.target

      if(b.paramsArray.length == 0){
        if(cooldown < System.currentTimeMillis()){
          cooldown = System.currentTimeMillis() + cooldownTime
          announceNewsItemAt(newsItems(Random.nextInt(newsItems.length)), m.server, m.target)
        }
        else{
          r.notice(m.sender.nickname, "This command is currently on cooldown")
        }


      }
      else b.paramsArray(0) match {

        case "on" =>
          if(!channels.contains(key)){
            channels = channels :+ key
            UserConfig.setArray(configKey, channels)
            r.reply(s"Pidgin rss is now on for this channel, to disable, use ${b.commandPrefix}pidgin off")
          }

        case "off" =>
          if(channels.contains(key)) {
            channels = channels.filter(_ != key)
            UserConfig.setArray(configKey, channels)
            r.reply(s"Pidgin rss is now disabled for this channel, to reenable, use ${b.commandPrefix}pidgin on")
          }

        case _ =>
          var item = 0
          try{
            item = Integer.parseInt(b.paramsArray(0))
          }
          catch {
            case e: Exception =>
              r.notice(m.sender.nickname, s"Usage: ${b.commandPrefix}pidgin <on/off/number>")
              return
          }
          if(item < 11 && item > 0){
            if(cooldown < System.currentTimeMillis()){
              cooldown = System.currentTimeMillis() + cooldownTime
              announceNewsItemAt(newsItems(item - 1), m.server, m.target)
            }
            else{
              r.notice(m.sender.nickname, "This command is currently on cooldown")
            }
          }
          else r.notice(m.sender.nickname, s"Usage: ${b.commandPrefix}pidgin <on/off/number>")
      }
    }
  }


  private def startRssMonitor(): Unit ={
    val rssUrl = "http://feeds.bbci.co.uk/pidgin/rss.xml"
    val thread = new Thread(new Runnable {
      override def run(): Unit = {
        while(true) {
          val url = new URL(rssUrl)
          val urlc = {
            if (rssUrl.startsWith("https")) {
              setAllowAllCerts()
              url.openConnection().asInstanceOf[HttpsURLConnection]
            }
            else url.openConnection()
          }
          urlc.addRequestProperty("Accept-Language", "en-US,en;q=0.8")
          urlc.addRequestProperty("User-Agent", "Mozilla")
          urlc.connect()
          var encoding = urlc.getContentEncoding
          if (encoding == null) encoding = "UTF-8"
          val xmlString = IOUtils.toString(urlc.getInputStream, encoding)

          val xml = XML.loadString(xmlString)
          val item = (xml \ "channel" \ "item").head
          val headline = (item \ "title").text
          val desc = (item \ "description").text
          val link = (item \ "link").text
          val newNews = NewsItem(headline, desc, link)
          if(newsItem.isDefined){
            if(newsItem.get.headline != newNews.headline){
              newsItem = Some(newNews)
              announceNewsItem(newsItem.get)
              val items = xml \ "channel" \ "item"
              var newArray: Array[NewsItem] = Array()
              for(item <- items){
                val headline = (item \ "title").text
                val desc = (item \ "description").text
                val link = (item \ "link").text
                val newsItem = NewsItem(headline, desc, link)
                newArray = newArray :+ newsItem
              }
              newsItems = newArray
            }
          }
          else{
            newsItem = Some(newNews)
            val items = xml \ "channel" \ "item"
            var newArray: Array[NewsItem] = Array()
            for(item <- items){
              val headline = (item \ "title").text
              val desc = (item \ "description").text
              val link = (item \ "link").text
              val newsItem = NewsItem(headline, desc, link)
              newArray = newArray :+ newsItem
            }
            newsItems = newArray
          }

        }
        Thread.sleep(100000)
      }
    })
    thread.setName("Pidgin rss thread")
    thread.start()

  }

  protected def announceNewsItem(newsItem: NewsItem): Unit ={
    val channels = UserConfig.getArrayAsType[String](configKey)
    if(channels.isDefined){
      for(key <- channels.get){
        val split = key.split("\\/")
        val server = split(0)
        val channel = split(1)
        announceNewsItemAt(newsItem, server, channel)
      }
    }
  }

  protected def announceNewsItemAt(newsItem: NewsItem, server: String, channel: String): Unit ={
    val ircServer = ConnectionManager.servers(server)
    val r = new ServerResponder(ircServer, "")
    r.say(channel, s"[\u0002Pidgin News\u0002] ${newsItem.headline}")
    r.say(channel, newsItem.desc)
    r.say(channel, s"Brought to you by our glorious BBC. Url: ${newsItem.link}")
  }

  private case class NewsItem(headline: String, desc: String, link: String)


}



