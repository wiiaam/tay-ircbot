package coremodules

import java.util.regex.Pattern

import irc.config.Configs
import irc.message.{MessageCommands, Message}
import irc.server.{ConnectionManager, ServerResponder}
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
    if(m.command == MessageCommands.PART && m.sender.nickname == m.config.getNickname){
      val config = m.config
      config.removeChannel(m.params.array(0))
    }
    if(m.command == MessageCommands.n005){
      m.params.array.foreach(param => {
        if(param.startsWith("NETWORK=")){
          println(s"UPADTING NETWORK NAME: ${m.server}")
          val name = m.server
          val config = m.config
          val newName = param.split("=")(1).trim
          config.networkName = newName
          Configs.configs.put(name, config)
          val server = ConnectionManager.servers.get(name)
          server.serverName = newName
          ConnectionManager.servers.put(name, server)

        }
      })
    }
  }
}
