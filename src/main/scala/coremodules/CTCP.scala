package coremodules

import irc.message.Message
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}
import out.Out


class CTCP extends Module{

  override val commands: Map[String, String] = Map()

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {

    if(m.trailing.equals("\u0001VERSION\u0001")){
      r.notice(m.sender.nickname, "\u0001VERSION go away\u0001")
    }

    if(m.trailing.equals("\u0001PING\u0001")){
      r.notice(m.sender.nickname, m.trailing)
    }
  }

}
