package coremodules

import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}


class Admin extends Module{
  override val commands: Map[String, Array[String]] = Map()

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(m.command == MessageCommands.PRIVMSG){
      if(m.sender.isAdmin){
        if(b.command == "join"){
          for(room <- b.paramsArray){
            r.join(room)
          }
        }
      }
    }
  }
}
