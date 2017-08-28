package modules

import irc.info.{Info, Rank}
import irc.message.{Message, MessageCommands}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}


class nurupo extends BotModule{
  override val adminCommands: Map[String, Array[String]] = Map("nurupo" -> Array("Toggles nurupo® moderation in the channel"))

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
        m.trailing.contains(":^)") ||
        m.trailing.contains(".gif") ) {
        if (Info.get(m.server).get.findChannel(m.params.first).get.getRank(m.config.getNickname) >= Rank.HOP ) {
          r.kick(m.params.first,m.sender.nickname,"No fun allowed")
        }
        else{
          r.pm(target, m.sender.nickname + ": No fun allowed")
        }

      }
    }
  }
}
