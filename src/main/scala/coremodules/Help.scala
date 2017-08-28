package coremodules

import irc.message.Message
import irc.server.ServerResponder
import ircbot._

import scala.collection.JavaConversions._


class Help extends BotModule{

  override val commands: Map[String, Array[String]] = Map("help" -> Array("Displays help information. Use .help <command> for more info"),
    "source" -> Array("Displays source code information"))

  override def parse(m: Message, b: BotCommand, r: ServerResponder): Unit = {
    val target = if(!m.params.first.startsWith("#")) m.sender.nickname else m.params.first

    val bAsDot = new BotCommand(m,".")
    if(bAsDot.command == "help"){
      if(bAsDot.hasParams){
        var allCommands: Map[String,Array[String]] = Map()
        for(module <- Modules.modules){
          allCommands = allCommands ++ module.commands
          if(m.sender.isAdmin) allCommands = allCommands ++ module.adminCommands
        }
        for(module <- Modules.coreModules){
          allCommands  = allCommands ++ module.commands
          if(m.sender.isAdmin) allCommands = allCommands ++ module.adminCommands
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
        var userCommands = ""
        var adminCommands = ""
        for(module <- Modules.modules){
          for((k,v) <- module.commands){
            userCommands += k + " "
          }

          for((k,v) <- module.adminCommands){
            adminCommands += k + " "
          }
        }
        for(module <- Modules.coreModules){
          for((k,v) <- module.commands){
            userCommands += k + " "
          }
          for((k,v) <- module.adminCommands){
            adminCommands += k + " "
          }
        }
        r.notice(m.sender.nickname, "\u0002Current commands available:")
        r.notice(m.sender.nickname, userCommands)
        if(m.sender.isAdmin){
          r.notice(m.sender.nickname, "\u0002Admin commands available to you:")
          r.notice(m.sender.nickname, adminCommands)
        }
        r.notice(m.sender.nickname, s"The current command prefix is: ${b.commandPrefix}")
        r.notice(m.sender.nickname, s"To use a command: \u0002${b.commandPrefix}<command>")
        r.notice(m.sender.nickname, s"Licensed under the terms of the \u0002AGPL\u0002. Full source code can be found at \u0002${Constants.REPO}")
      }
    }

    if(bAsDot.command == "source" || b.command == "source"){
      r.say(target, s"Source code can be found here: ${Constants.REPO}")
    }
  }
}
