package modules

import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}


class nurupo extends Module{
  override val commands: Map[String, Array[String]] = Map()

  var on = false
  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first

    if(b.command == "funpolice" && m.sender.isAdmin){
      if(on){
        r.say(target, "Fun Police is now off")
        on = false
      }
      else{
        r.say(target, "Fun Police is now on")
        on = true
      }
    }

    if(m.command == MessageCommands.PRIVMSG && m.params.first == "#pasta" && on){
      if(m.trailing.startsWith(">") ||
        m.trailing.contains(", >") ||
        m.trailing.contains(": >") ||
        m.trailing.contains("\u0003") ||
        m.trailing.contains("mr bones wild ride")){
        r.send("MODE #pasta +b " + m.sender.host)
        r.send(s"KICK #pasta ${m.sender.nickname} :No fun allowed")
      }
    }
  }
}
