package coremodules

import irc.message.{MessageCommands, Message}
import irc.server.{ConnectionManager, Priorities, ServerResponder}
import ircbot.{BotCommand, BotModule}


class Ping extends BotModule{

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(m.command == MessageCommands.PING){
      r.send("PONG :" + m.trailing, Priorities.HIGH_PRIORITY)
    }
    if(m.command == MessageCommands.PONG){
      ConnectionManager.pings += (m.server -> true)
    }
  }
}
