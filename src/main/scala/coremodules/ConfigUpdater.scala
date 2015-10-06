package coremodules

import irc.config.Configs
import irc.message.{MessageCommands, Message}
import irc.server.ServerResponder
import ircbot.{BotCommand, Module}


class ConfigUpdater extends Module{
  override val commands: Map[String, Array[String]] = Map()

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    if(m.command == MessageCommands.JOIN && m.sender.nickname == Configs.get(m.server).get.getNickname){
      val config = Configs.get(m.server).get
      if(!config.getChannels.contains(m.trailing)) config.addChannel(m.trailing)
    }
    if(m.command == MessageCommands.KICK){
      if(m.params.array(1) == Configs.get(m.server).get.getNickname) {
        val config = Configs.get(m.server).get
        config.removeChannel(m.trailing)
      }
    }

    if(m.command == MessageCommands.NICK && m.sender.nickname == m.config.getNickname){
      val config = m.config
      config.setNickname(m.trailing)
    }
  }
}
