package coremodules

import irc.message.{Message, MessageCommands}
import irc.server.ServerResponder
import ircbot.{BotCommand, BotModule}


class CTCP extends BotModule{


  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {

    if(m.trailing.equals("\u0001VERSION\u0001") && m.command == MessageCommands.PRIVMSG){
      r.notice(m.sender.nickname, "\u0001VERSION im daddy's kitten\uD83D\uDE3D\uD83D\uDC7C\uD83D\uDCA6\u0001")
    }

    if(m.trailing.startsWith("\u0001PING") && m.command == MessageCommands.PRIVMSG){
      r.notice(m.sender.nickname, m.trailing)
    }
  }

}
