package coremodules

import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotModule, BotCommand}


class ConfigUpdater extends BotModule{

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(m.command == MessageCommands.JOIN && m.sender.nickname == m.config.getNickname){
      val config = m.config
      if(!config.getChannels.contains(m.trailing)) config.addChannel(m.trailing)
    }
    if(m.command == MessageCommands.KICK){
      val config = m.config
      if(m.params.array(1) == config.getNickname) config.removeChannel(m.trailing)
    }
  }
}
