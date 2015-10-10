package modules

import java.io._
import java.net.URISyntaxException
import java.util.Scanner

import irc.config.Configs
import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import irc.utilities.URLParser
import ircbot.{BotCommand, Module}
import org.json.{JSONArray, JSONObject}
import scala.collection.JavaConversions._


class TitleReporting extends Module{
  override val commands: Map[String, Array[String]] = Map()

  var json: JSONObject = new JSONObject()
  val jsonfile = new File(this.getClass.getResource("files/titles.json").toURI)

  var channels: Map[String, Set[String]] = Map()

  try {
    val scan = new Scanner(new FileInputStream(jsonfile))
    var jsonstring = ""
    while (scan.hasNext) {
      jsonstring += scan.next() + " "
    }
    scan.close()
    json = new JSONObject(jsonstring)
  } catch {
    case e: IOException => e.printStackTrace()
    case e: URISyntaxException => e.printStackTrace()
  }

  for(server <- json.keySet()){

    var channelset: Set[String] = Set()
    val array = json.getJSONArray(server + "")
    for(i: Int <- 0 until array.length()){
      channelset = channelset + array.getString(i)
    }
    channels = channels ++ Map(server + "" -> channelset)
  }


  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first


    if(b.command == "urltitles" && m.sender.isAdmin){
      val usage = s"Usage: ${b.commandPrefix}urltitles <on/off> <channel>"
      if(b.hasParams){
        if(b.paramsArray.length > 1){
          b.paramsArray(0) match  {
            case "on" =>
              var set: Set[String] = if(channels.contains(m.server)) {
                channels(m.server)
              }
              else Set()
              if(set.contains(b.paramsArray(1))) {
                r.say(target, s"${m.sender.nickname}: Title reporting is already on for ${b.paramsArray(1)}")
              }
              else {
                set = set + m.params.first
                channels = channels ++ Map(m.server -> set)
                save()
                r.say(target, s"${m.sender.nickname}: Title reporting is now on for ${b.paramsArray(1)}")
              }
            case "off" =>
              var set: Set[String] = if(channels.contains(m.server)) {
                channels(m.server)
              }
              else Set()
              if(set.contains(b.paramsArray(1))) {
                set = set - m.params.first
                channels = channels ++ Map(m.server -> set)
                save()
                r.say(target, s"${m.sender.nickname}: Title reporting is now off for ${b.paramsArray(1)}")
              }
              else {
                r.say(target, s"${m.sender.nickname}: Title reporting was already off for ${b.paramsArray(1)}")
              }
            case _ =>
              r.say(target, usage)
          }
        }
        else r.say(target, usage)
      }
    }

    if(m.command == MessageCommands.PRIVMSG && !m.trailing.contains("Reporting in!")){
      if(checkChannel(m.params.first, m.server)) {
        if (m.trailing.contains("http://") || m.trailing.contains("https://")) {
          val messageSplit: Array[String] = m.trailing.split("\\s+")
          for (i <- 0 until messageSplit.length) {
            if (messageSplit(i).startsWith("http://") || messageSplit(i).startsWith("https://")) {
              var title = URLParser.find(messageSplit(i))
              if (title != null) {
                title = title.replace("http://", "").replace("https://", "")
                r.say(target, title)
              }
            }
          }
        }
      }
    }
  }

  private def checkChannel(channel: String, server: String): Boolean = {
    if(channels.contains(server)){
      channels(server).contains(channel)
    }
    else false
  }

  private def save() {
    for((server,channellist) <- channels){
      val jsonArray = new JSONArray()
      for(channel: String <- channellist){
        jsonArray.put(channel)
      }
      json.put(server, jsonArray)
    }
    try {
      val writer = new PrintWriter(jsonfile)
      writer.println(json.toString)
      writer.close()
    } catch {
      case e: FileNotFoundException => e.printStackTrace()
    }
  }
}
