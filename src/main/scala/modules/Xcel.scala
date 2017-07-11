package modules

import irc.message.{Message, MessageCommands}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}


class Xcel extends BotModule{

  var timeout = System.currentTimeMillis()

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(m.config.networkName == "Rizon" && m.sender.isRegistered && m.sender.nickname.toLowerCase == "pr0wolf29" && m.command == MessageCommands.PRIVMSG && timeout <= System.currentTimeMillis()){
      val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first

      if(m.trailing.startsWith("<xcel> ") || m.trailing.startsWith("[xcel] ")){
        val oldnick = m.config.getNickname
        r.nick("xcel")
        r.say(target, "\u200B" + m.trailing.substring(7))
        r.nick(oldnick)
        timeout = System.currentTimeMillis() + 10000
      }
      if(m.trailing.startsWith("<xcelq> ") || m.trailing.startsWith("[xcelq] ")){
        val oldnick = m.config.getNickname
        r.nick("xcel")
        r.say(target, "\u200B" + m.trailing.substring(8))
        r.nick(oldnick)
        timeout = System.currentTimeMillis() + 10000
      }
    }
  }
}
