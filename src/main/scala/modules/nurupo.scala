package modules

import irc.info.Info
import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}


class nurupo extends Module{
  override val commands: Map[String, Array[String]] = Map()

  var channels: Set[String] = Set()
  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first

    if(b.command == "nurupo" && m.sender.isAdmin){
      if(channels.contains(m.server + ":" + m.params.first)){
        r.say(target, "nurupo is no longer moderating this channel")
        channels -= m.server + ":" + m.params.first
      }
      else{
        r.say(target, "nurupo is now moderating this channel")
        channels += m.server + ":" + m.params.first
      }
    }

    if(m.command == MessageCommands.PRIVMSG && channels.contains(m.server + ":" + m.params.first) && m.sender.nickname != "topkek_2000"){
      if(m.trailing.startsWith(">") ||
        m.trailing.contains(", >") ||
        m.trailing.contains(": >") ||
        m.trailing.contains("\u0003") ||
        m.trailing.contains("mr bones wild ride") ||
        m.trailing.contains(".webm") ||
        m.trailing.contains(".gif") ) {
        if (Info.get(m.server).get.findChannel(m.params.first).get.getRank(m.config.getNickname) > 1) {
          r.send(s"KICK ${m.params.first} ${m.sender.nickname} :No fun allowed")
        }
        else{
          r.pm(target, m.sender.nickname + ": No fun allowed")
        }

      }
    }
  }
}
