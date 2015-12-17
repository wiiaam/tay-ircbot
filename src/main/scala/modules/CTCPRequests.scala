package modules

import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotModule, BotCommand}


class CTCPRequests extends BotModule{

  override val commands: Map[String, Array[String]] = Map("ver" -> Array("Sends a CTCP version to a user and shows the response"),
    "ping" -> Array("Sends a CTCP ping to a user and calculates the time"))

  var versionRequests: Set[(String,String)] = Set()

  var pingRequests: Set[(String,String)] = Set()


  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first
    if(b.command == "ver" || b.command == "version"){
      if(b.hasParams){
        val user = b.paramsArray(0)
        val tup = (user, target)
        versionRequests += tup
        r.CTCP(user,"VERSION")
      }
    }

    if(b.command == "ping"){
      if(b.hasParams){
        val user = b.paramsArray(0)
        val tup = (user, target)
        pingRequests += tup
        r.CTCP(user,s"PING ${System.currentTimeMillis()}")
      }
    }

    if(m.trailing.startsWith("\u0001") && m.trailing.endsWith("\u0001") && m.command == MessageCommands.NOTICE){
      if(m.trailing.startsWith("\u0001VERSION")){
        for(t <- versionRequests){
          if(t._1 == m.sender.nickname){
            val version = m.trailing.replace("\u0001","").substring(8).replace("WeeChat","WeebChat")
            r.say(t._2,s"Version for \u0002${t._1}\u0002: $version")
            versionRequests -= t
          }
        }
      }
      if(m.trailing.startsWith("\u0001PING")){
        for(t <- pingRequests){
          if(t._1 == m.sender.nickname){
            val time = m.trailing.replace("\u0001","").substring(5).toLong
            r.say(t._2,s"Ping for \u0002${t._1}\u0002: ${System.currentTimeMillis() - time} ms")
            pingRequests -= t
          }
        }
      }
    }

  }
}
