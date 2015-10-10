package modules

import irc.message.Message
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}


class Random extends Module{
  override val commands: Map[String, Array[String]] = Map("slap" -> Array("Slap some sense into a user"))

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first

    if(b.command == "slap"){
      if(b.hasParams) r.action(target,s"slaps ${b.paramsString}")
    }

    if(m.trailing.startsWith("\u0001ACTION")){
      val action = m.trailing.substring("\u0001ACTION".length).replace("\u0001","")
      r.action(target, "also" + action)
    }

    if(b.command == "bang"){
      r.kick("#pasta","wiiaam","bang")
    }
  }
}
