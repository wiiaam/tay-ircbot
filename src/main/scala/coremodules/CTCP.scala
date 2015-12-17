package coremodules

import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotModule, BotCommand}


class CTCP extends BotModule{


  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {

    if(m.trailing.equals("\u0001VERSION\u0001") && m.command == MessageCommands.PRIVMSG){
      r.notice(m.sender.nickname, "\u0001VERSION go away\u0001")
    }

    if(m.trailing.startsWith("\u0001PING") && m.command == MessageCommands.PRIVMSG){
      r.notice(m.sender.nickname, m.trailing)
    }
  }

}
