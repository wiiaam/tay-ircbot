package coremodules

import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}


class Ping extends Module{
  override val commands: Map[String, String] = Map()

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(m.command == MessageCommands.PING){
      r.send("PONG ")
    }
  }
}
