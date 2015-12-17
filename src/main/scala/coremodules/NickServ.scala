package coremodules

import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}


class NickServ extends BotModule{

  override val adminCommands: Map[String, Array[String]] = Map("auth" -> Array("Auth with NickServ",
    "Only works if nickserv is on in config"))

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(b.command == "auth" && m.sender.isAdmin){
      val config = m.config
      if(config.useNickServ){
        r.pm("NickServ", "IDENTIFY " + config.getPassword)
      }
    }

    if(m.command == MessageCommands.NOTICE && m.sender.nickname.toLowerCase == "nickserv"){
      if(m.config.useNickServ){
        if(m.trailing.toLowerCase.contains("nickserv identify")){
          r.pm("NickServ", "IDENTIFY " + m.config.getPassword)
        }
      }
    }
  }
}
