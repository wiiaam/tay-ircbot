package modules

import java.io._
import java.net.URISyntaxException
import java.util.Scanner

import irc.config.Configs
import irc.info.Info
import irc.message.{MessageCommands, Message}

import irc.server.ServerResponder
import ircbot.{BotCommand, Module}
import org.json.{JSONArray}
import scala.collection.JavaConversions._

class Administration extends Module{
  override val commands: Map[String, Array[String]] = Map("kme" -> Array("Kick yourself from the channel. Only work when administration is turned on in that channel",
  "Command is always .kme"),
    "banme" -> Array("Ban yourself from the channel. Only work when administration is turned on in that channel",
      "Command is always .kme"),
    "tkbme" -> Array("Time ban yourself from the channel. Only work when administration is turned on in that channel",
      "Command is always .kme Usage: .tkbme <seconds>"))


  var channels: Set[String] = Set()

  val jsonfile = new File(this.getClass.getResource("files/administration.json").toURI)

  var json: JSONArray = new JSONArray()

  try {
    val scan = new Scanner(new FileInputStream(jsonfile))
    var jsonstring = ""
    while (scan.hasNext) {
      jsonstring += scan.next() + " "
    }
    scan.close()
    json = new JSONArray(jsonstring)
  } catch {
    case e: IOException => e.printStackTrace()
    case e: URISyntaxException => e.printStackTrace()
  }

  for(i <- 0 until json.length()){
    channels += json.getString(i)
  }

  var antispams: Map[String, Map[String, Int]] = Map()

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first



    if((m.command == MessageCommands.PRIVMSG || m.command == MessageCommands.NOTICE) && channels.contains(m.server + ":" + m.params.first)){

      // ANTISPAM
      var isop = false
      var ishop = false

      if(Info.get(m.server).isDefined){
        if(Info.get(m.server).get.findChannel(m.params.first).isDefined ){
          if(Info.get(m.server).get.findChannel(m.params.first).get.getRank(m.sender.nickname) > 2){
            isop = true
          }
          if(Info.get(m.server).get.findChannel(m.params.first).get.getRank(m.sender.nickname) > 1){
            ishop = true
          }
        }
      }

      if(!isop) {
        val spams = {
          if (antispams.contains(m.server + ":" + m.params.first)) {
            if (antispams(m.server + ":" + m.params.first).contains(m.sender.nickname)) {
              antispams(m.server + ":" + m.params.first)(m.sender.nickname)
            }
            else 0
          }
          else 0
        }

        val existingSpams = {
          if (antispams.contains(m.server + ":" + m.params.first)) {
            antispams(m.server + ":" + m.params.first)
          }
          else Map()
        }

        antispams ++= Map(m.server + ":" + m.params.first -> (existingSpams ++ Map(m.sender.nickname -> (spams + 1))))
        removeAntispam(m.server + ":" + m.params.first, m.sender.nickname, 10000)
        if (spams > 4) {
          r.kick(m.params.first, m.sender.nickname, "Stop flooding!")
          return
        }
      }

      val bAsDot = new BotCommand(m, ".")

      // SELF BAN COMMANDS

      if(bAsDot.command == "kme") {
        r.kick(m.params.first, m.sender.nickname, "There you go")
      }

      if(bAsDot.command == "banme") {
        r.ban(m.params.first, "@" + m.sender.host)
        r.kick(m.params.first, m.sender.nickname, "There you go")
      }

      if(bAsDot.command == "tkbme") {
        if(bAsDot.hasParams) {
          var time = 0
          try {
            if(bAsDot.paramsArray(0) == "gentoo") time = 2147483647
            else time = Integer.parseInt(bAsDot.paramsArray(0))
            if(time <= 0) throw new NumberFormatException()
            r.ban(m.params.first, "@" + m.sender.host)
            r.kick(m.params.first, m.sender.nickname, s"See you in $time seconds")
            removeBan(r, m.params.first, m.sender.host,time)
          }
          catch {
            case e: NumberFormatException =>
              r.say(target, s"${m.sender.nickname}: ${bAsDot.paramsArray(1)} is not a valid timeout")
              return
          }

        }
      }

      // ADMINISTRATION COMMANDS
      // HALFOP ONLY

      if(ishop){

        // KICKBAN
        if(bAsDot.command == "kb"){
          if(bAsDot.hasParams) {
            val currentChannel = Info.get(m.server).get.findChannel(m.params.first).get
            try {
              if(m.sender.nickname == bAsDot.paramsArray(0)) {
                r.say(target, s"${m.sender.nickname}: To kickban yourself, use the .banme command")
              }
              else if(currentChannel.getRank(m.sender.nickname) == 2 && currentChannel.getRank(b.paramsArray(0)) == 2){
                r.say(target, s"${m.sender.nickname}: You cannot kick another hop")
              }
              else if(currentChannel.getRank(m.sender.nickname) > currentChannel.getRank(b.paramsArray(0))){
                val host = "@" + Info.get(m.server).get.findUser(bAsDot.paramsArray(0)).get.host

                r.ban(m.params.first, host)
                if(bAsDot.paramsArray.length > 1) r.kick(m.params.first, bAsDot.paramsArray(0), bAsDot.paramsString.substring(bAsDot.paramsArray(0).length + 1))
                else r.kick(m.params.first, bAsDot.paramsArray(0))
              }
              else r.say(target, s"${m.sender.nickname}: They are a higher rank than you")
            }
            catch{
              case e: Exception =>
                r.say(target, s"${m.sender.nickname}: Could not find user, ${m.params.first}")
            }
          }
        }
        //TIME KICKBAN
        if(bAsDot.command == "tkb"){
          if(bAsDot.paramsArray.length > 1) {


            var time = 0
            try {
              if(bAsDot.paramsArray(1) == "gentoo") time = 2147483647
              else time = Integer.parseInt(bAsDot.paramsArray(1))
              if(time <= 0) throw new NumberFormatException()
            }
            catch {
              case e: NumberFormatException =>
                r.say(target, s"${m.sender.nickname}: ${bAsDot.paramsArray(1)} is not a valid timeout")
                return
            }

            val currentChannel = Info.get(m.server).get.findChannel(m.params.first).get
            try {
              if(m.sender.nickname == bAsDot.paramsArray(0)) {
                r.say(target, s"${m.sender.nickname}: To time ban yourself, use the .tkbme command")
              }
              else if(currentChannel.getRank(m.sender.nickname) == 2 && currentChannel.getRank(b.paramsArray(0)) == 2){
                r.say(target, s"${m.sender.nickname}: You cannot kick another hop")
              }
              else if(currentChannel.getRank(m.sender.nickname) > currentChannel.getRank(b.paramsArray(0))){

                val host = "@" + Info.get(m.server).get.findUser(bAsDot.paramsArray(0)).get.host

                r.ban(m.params.first, host)
                if(bAsDot.paramsArray.length > 2) {
                  val reason = bAsDot.paramsString.substring(bAsDot.paramsArray(0).length + bAsDot.paramsArray(1).length + 2)
                  r.kick(m.params.first, bAsDot.paramsArray(0), reason)
                }
                else r.kick(m.params.first, bAsDot.paramsArray(0), s"Come back in $time seconds")
                removeBan(r, m.params.first, host, time)
              }
              else r.say(target, s"${m.sender.nickname}: They are a higher rank than you")
            }
            catch{
              case e: Exception =>
                r.say(target, s"${m.sender.nickname}: Could not find user, ${m.params.first}")
            }
          }
        }

        // KICK
        if(bAsDot.command == "k") {
          if (bAsDot.hasParams) {
            val currentChannel = Info.get(m.server).get.findChannel(m.params.first).get
            try {
              if(m.sender.nickname == bAsDot.paramsArray(0)) {
                r.say(target, s"${m.sender.nickname}: To kick yourself, use the .kme command")
              }
              else if (currentChannel.getRank(m.sender.nickname) == 2 && currentChannel.getRank(b.paramsArray(0)) == 2) {
                r.say(target, s"${m.sender.nickname}: You cannot kick another hop")
              }
              else if (currentChannel.getRank(m.sender.nickname) > currentChannel.getRank(b.paramsArray(0))) {
                if (bAsDot.paramsArray.length > 1) r.kick(m.params.first, bAsDot.paramsArray(0), bAsDot.paramsString.substring(bAsDot.paramsArray(0).length + 1))
                else r.kick(m.params.first, bAsDot.paramsArray(0))
              }
              else r.say(target, s"${m.sender.nickname}: They are a higher rank than you")
            }
            catch {
              case e: Exception =>
                r.say(target, s"${m.sender.nickname}: Could not find user, ${m.params.first}")
            }
          }
        }

        if(bAsDot.command == "ub"){
          if(bAsDot.hasParams){
            try{
              r.unban(m.params.first, "@" + Info.get(m.server).get.findUser(bAsDot.paramsArray(0)).get.host)
            }
            catch{
              case e: Exception =>
                r.say(target, s"${m.sender.nickname}: Could not find user, ${m.params.first}")
            }
          }
        }
      }
    }

    // ADMIN TOGGLE

    if(b.command == "administration" && m.sender.isAdmin){
      val usage = s"Usage: ${b.commandPrefix}administration <on/off> <channel>"
      if(b.hasParams){
        val channel = {
          if(b.paramsArray.length > 1) b.paramsArray(1)
          else m.params.first
        }
        b.paramsArray(0) match {
          case "on" =>
            if (channels.contains(m.server + ":" + channel)) {
              r.say(target, s"${m.sender.nickname}: Administration is already on for $channel")
            }
            else {
              channels = channels + (m.server + ":" + channel)
              save()
              r.say(target, s"${m.sender.nickname}: Administration is now on for $channel")
            }
          case "off" =>
            if (channels.contains(m.server + ":" + channel)) {
              channels -= (m.server + ":" + channel)
              save()
              r.say(target, s"${m.sender.nickname}: Administration is now off for $channel")
            }
            else {
              r.say(target, s"${m.sender.nickname}: Administration was already off for $channel")
            }
          case _ =>
            r.say(target, usage)
        }
      }
      else r.say(target, usage)
    }
  }
  private def save() {
    val jsonArray = new JSONArray()
    for(channel: String <- channels){
      jsonArray.put(channel)
    }
    json = jsonArray
    try {
      val writer = new PrintWriter(jsonfile)
      writer.println(json.toString)
      writer.close()
    } catch {
      case e: FileNotFoundException => e.printStackTrace()
    }
  }

  private def removeAntispam(channel: String, user: String, timeoutMillis: Long): Unit ={
    new Thread(new Runnable {
      override def run(): Unit = {
        Thread.sleep(timeoutMillis)

        val spams = antispams(channel)(user)

        if(antispams.contains(channel)){
          if(antispams(channel).contains(user)){
            antispams ++= Map(channel -> (antispams(channel) ++ Map(user -> (spams - 1))))
          }
        }

      }
    }).start()
  }

  private def removeBan(responder: ServerResponder, channel: String, ban: String, timeoutSeconds: Long): Unit ={
    new Thread(new Runnable {
      override def run(): Unit = {
        Thread.sleep(timeoutSeconds*1000)
        responder.unban(channel, ban)
      }
    }).start()
  }
}
