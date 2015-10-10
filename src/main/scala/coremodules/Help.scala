package coremodules

import irc.config.Configs
import irc.message.Message
import irc.server.ServerResponder
import ircbot.{Constants, Modules, Module, BotCommand}
import out.Out
import scala.collection.JavaConversions._


class Help extends Module{

  override val commands: Map[String, Array[String]] = Map("help" -> Array("Displays help information. Use .help <command> for more info") )

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {

    val bAsDot = new BotCommand(m,".")
    if(bAsDot.command == "help"){
      if(bAsDot.hasParams){
        var allCommands: Map[String,Array[String]] = Map()
        for(module <- Modules.modules){
          allCommands = allCommands ++ module.commands
        }
        for(module <- Modules.coreModules){
          allCommands  = allCommands ++ module.commands
        }
        val command = bAsDot.paramsArray(0)
        for((k,v) <- allCommands){
        }
        if(allCommands.contains(command)){
          for(message <- allCommands(command))
          r.notice(m.sender.nickname, s"[$command] ${message.replace("%p",b.commandPrefix)}")
        }
        else{
          r.notice(m.sender.nickname, s"$command is not a valid command")
        }
      }
      else{
        var commands = ""
        for(module <- Modules.modules){
          for((k,v) <- module.commands){
            commands += k + " "
          }
        }
        for(module <- Modules.coreModules){
          for((k,v) <- module.commands){
            commands += k + " "
          }
        }
        r.notice(m.sender.nickname, "Current commands available:")
        r.notice(m.sender.nickname, commands)
        r.notice(m.sender.nickname, s"The current command prefix is: ${b.commandPrefix}")
        r.notice(m.sender.nickname, s"To use a command: ${b.commandPrefix}<command>")
        r.notice(m.sender.nickname, s"Licensed under the terms of the AGPL. Full source code can be found at ${Constants.REPO}")
      }
    }
  }
}
